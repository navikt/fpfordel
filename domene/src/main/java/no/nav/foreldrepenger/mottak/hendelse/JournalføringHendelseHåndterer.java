package no.nav.foreldrepenger.mottak.hendelse;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.function.Supplier;

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.foreldrepenger.konfig.KonfigVerdi;

import no.nav.foreldrepenger.mottak.hendelse.test.VtpKafkaAvroDeserializer;
import no.nav.vedtak.felles.integrasjon.kafka.KafkaMessageHandler;

import no.nav.vedtak.felles.integrasjon.kafka.KafkaProperties;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.MottakKanal;
import no.nav.foreldrepenger.fordel.kodeverdi.Tema;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentRepository;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.task.joark.HentDataFraJoarkTask;
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;
import no.nav.vedtak.log.mdc.MDCOperations;

import static io.confluent.kafka.serializers.KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG;

/*
 * Dokumentasjon https://confluence.adeo.no/pages/viewpage.action?pageId=315215917
 */

@Transactional
@ActivateRequestContext
@ApplicationScoped
public class JournalføringHendelseHåndterer implements KafkaMessageHandler<String, JournalfoeringHendelseRecord> {

    private static final Logger LOG = LoggerFactory.getLogger(JournalføringHendelseHåndterer.class);
    private static final Environment ENV = Environment.current();
    private static final Map<String, Object> SCHEMA_MAP = getSchemaMap();

    private static final String HENDELSE_MIDL = "JournalpostMottatt";
    private static final String HENDELSE_MIDL_LEGACY = "MidlertidigJournalført";
    private static final String HENDELSE_ENDRET = "TemaEndret";
    private static final String TEMA_FOR = Tema.FORELDRE_OG_SVANGERSKAPSPENGER.getOffisiellKode();

    private static final String EESSI = MottakKanal.EESSI.getKode();

    private ProsessTaskTjeneste taskTjeneste;
    private DokumentRepository dokumentRepository;
    private String topicName;
    private int journalføringDelay;

    JournalføringHendelseHåndterer() {
        // CDI
    }

    @Inject
    public JournalføringHendelseHåndterer(ProsessTaskTjeneste taskTjeneste,
                                          DokumentRepository dokumentRepository,
                                          @KonfigVerdi("kafka.topic.journal.hendelse") String topicName,
                                          @KonfigVerdi(value="journalføring.timer.delay", defaultVerdi = "2") int journalføringDelay) {
        this.taskTjeneste = taskTjeneste;
        this.dokumentRepository = dokumentRepository;
        this.journalføringDelay = journalføringDelay;
        this.topicName = topicName;
    }

    private static void setCallIdForHendelse(JournalfoeringHendelseRecord payload) {
        var hendelsesId = payload.getHendelsesId();
        if (hendelsesId == null || hendelsesId.isEmpty() || hendelsesId.isBlank()) {
            MDCOperations.putCallId();
        } else {
            MDCOperations.putCallId(hendelsesId);
        }
    }

    @Override
    @SuppressWarnings("unused")
    public void handleRecord(String key, JournalfoeringHendelseRecord value) {
        if (TEMA_FOR.equals(value.getTemaNytt()) && hendelseSkalHåndteres(value)) {
            handleMessage(value);
        }
    }

    private static boolean hendelseSkalHåndteres(JournalfoeringHendelseRecord payload) {
        var hendelse = payload.getHendelsesType();
        return HENDELSE_MIDL.equalsIgnoreCase(hendelse) || HENDELSE_ENDRET.equalsIgnoreCase(hendelse) || HENDELSE_MIDL_LEGACY.equalsIgnoreCase(
            hendelse);
    }

    void handleMessage(JournalfoeringHendelseRecord payload) {
        setCallIdForHendelse(payload);

        var arkivId = String.valueOf(payload.getJournalpostId());
        var hendelseType = payload.getHendelsesType();
        var mottaksKanal = payload.getMottaksKanal();
        var eksternReferanseId =
            (payload.getKanalReferanseId() == null) || payload.getKanalReferanseId().isEmpty() ? null : payload.getKanalReferanseId();

        // De uten kanalreferanse er "klonet" av SBH og journalført fra Gosys.
        // Normalt blir de journalført, men det feiler av og til pga tilgang.
        // Håndterer disse journalpostene senere i tilfelle SBH skal ha klart å ordne ting selv
        var delay = eksternReferanseId == null && !mottaksKanal.equals(MottakKanal.SELVBETJENING.getKode()) ? Duration.ofHours(journalføringDelay) : Duration.ZERO;

        if (HENDELSE_ENDRET.equalsIgnoreCase(payload.getHendelsesType())) {
            // Hendelsen kan komme før arkivet er oppdatert .....
            delay = Duration.ofSeconds(30);
            var gammeltTema = payload.getTemaGammelt() != null ? payload.getTemaGammelt() : null;
            LOG.info("FPFORDEL Tema Endret fra {} journalpost {} kanal {} referanse {}", gammeltTema, arkivId, mottaksKanal, eksternReferanseId);
        }

        // EESSI har egen mottaksprosess m/BEH_SED-oppgaver.
        if (EESSI.equals(mottaksKanal)) {
            LOG.info("FPFORDEL Mottatt Journalføringhendelse ignorerer journalpost {} kanal {}", arkivId, mottaksKanal);
            return;
        }

        LOG.info("FPFORDEL Mottatt Journalføringhendelse type {} journalpost {} referanse {}", hendelseType, arkivId, eksternReferanseId);

        // All journalføring av innsendinger fra SB gir en Midlertidig-hendelse. De skal
        // vi ikke reagere på før evt full refaktorering
        if (eksternReferanseId != null && dokumentRepository.erLokalForsendelse(eksternReferanseId)) {
            LOG.info("FPFORDEL Mottatt Hendelse egen journalføring callid {}", arkivId);
            return;
        }
        if (!dokumentRepository.hentJournalposter(arkivId).isEmpty()) {
            LOG.info("FPFORDEL Mottatt Hendelse egen journalføring journalpost {}", arkivId);
            return;
        }

        lagreJoarkTask(payload, arkivId, eksternReferanseId, delay);
    }

    private void lagreJoarkTask(JournalfoeringHendelseRecord payload, String arkivId, String eksternReferanse, Duration delay) {
        var taskdata = ProsessTaskData.forProsessTask(HentDataFraJoarkTask.class);
        taskdata.setCallIdFraEksisterende();
        MottakMeldingDataWrapper melding = new MottakMeldingDataWrapper(taskdata);
        melding.setArkivId(arkivId);
        melding.setTema(Tema.fraOffisiellKode(payload.getTemaNytt()));
        melding.setBehandlingTema(BehandlingTema.fraOffisiellKode(payload.getBehandlingstema()));
        if (eksternReferanse != null) {
            melding.setEksternReferanseId(eksternReferanse);
        }
        var oppdatertTaskdata = melding.getProsessTaskData();
        oppdatertTaskdata.setNesteKjøringEtter(LocalDateTime.now().plus(delay));
        taskTjeneste.lagre(oppdatertTaskdata);
    }

    @Override
    public String topic() {
        return topicName;
    }

    @Override
    public String groupId() { // Keep stable (or it will read from autoOffsetReset()
        return  "fpfordel"; // Hold konstant pga offset commit !!
    }

    @Override
    public Supplier<Deserializer<String>> keyDeserializer() {
        return () -> {
            var s = new StringDeserializer();
            s.configure(SCHEMA_MAP, true);
            return s;
        };
    }

    @Override
    public Supplier<Deserializer<JournalfoeringHendelseRecord>> valueDeserializer() {
        return () -> {
            var s = getDeserializer();
            s.configure(SCHEMA_MAP, false);
            return s;
        };
    }

    private static Deserializer<JournalfoeringHendelseRecord> getDeserializer() {
        return ENV.isProd() || ENV.isDev() ? new WrappedAvroDeserializer<>() : new WrappedAvroDeserializer<>(new VtpKafkaAvroDeserializer());
    }

    private static Map<String, Object> getSchemaMap() {
        var schemaRegistryUrl = KafkaProperties.getAvroSchemaRegistryURL();
        if (schemaRegistryUrl != null && !schemaRegistryUrl.isEmpty()) {
            return Map.of(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl,
                AbstractKafkaSchemaSerDeConfig.BASIC_AUTH_CREDENTIALS_SOURCE, "USER_INFO",
                AbstractKafkaSchemaSerDeConfig.USER_INFO_CONFIG, KafkaProperties.getAvroSchemaRegistryBasicAuth(),
                SPECIFIC_AVRO_READER_CONFIG, true);
        } else {
            return Map.of();
        }
    }
}
