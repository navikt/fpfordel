package no.nav.foreldrepenger.mottak.domene.oppgavebehandling;

import static no.nav.foreldrepenger.mottak.domene.oppgavebehandling.OpprettGSakOppgaveTask.TASKNAME;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.ANNEN_PART_ID_KEY;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.ARKIV_ID_KEY;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.BEHANDLINGSTEMA_KEY;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.DOKUMENTTYPE_ID_KEY;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.FORSENDELSE_ID_KEY;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.JOURNAL_ENHET;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.SAKSNUMMER_KEY;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverdi.Tema;
import no.nav.foreldrepenger.fordel.kodeverdi.Temagrupper;
import no.nav.foreldrepenger.mottak.behandlendeenhet.EnhetsTjeneste;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.task.SlettForsendelseTask;
import no.nav.foreldrepenger.mottak.tjeneste.ArkivUtil;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumerMedCache;
import no.nav.vedtak.felles.integrasjon.oppgave.v1.OppgaveRestKlient;
import no.nav.vedtak.felles.integrasjon.oppgave.v1.OpprettOppgave;
import no.nav.vedtak.felles.integrasjon.oppgave.v1.Prioritet;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

/**
 * <p>
 * ProsessTask som oppretter en oppgave i GSAK for manuell behandling av
 * tilfeller som ikke kan håndteres automatisk av vedtaksløsningen.
 * <p>
 * </p>
 */
@Dependent
@ProsessTask(TASKNAME)
public class OpprettGSakOppgaveTask implements ProsessTaskHandler {

    public static final String TASKNAME = "integrasjon.gsak.opprettOppgave";

    private static final Logger LOG = LoggerFactory.getLogger(OpprettGSakOppgaveTask.class);

    static final String OPPGAVETYPER_JFR = "JFR"; // Fra offisielt kodeverk

    private final EnhetsTjeneste enhetsidTjeneste;
    private final AktørConsumerMedCache aktørConsumer;
    private final ProsessTaskRepository prosessTaskRepository;
    private final OppgaveRestKlient restKlient;

    @Inject
    public OpprettGSakOppgaveTask(ProsessTaskRepository prosessTaskRepository,
                                  EnhetsTjeneste enhetsidTjeneste,
                                  AktørConsumerMedCache aktørConsumer,
                                  OppgaveRestKlient restKlient) {
        this.enhetsidTjeneste = enhetsidTjeneste;
        this.aktørConsumer = aktørConsumer;
        this.prosessTaskRepository = prosessTaskRepository;
        this.restKlient = restKlient;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        BehandlingTema behandlingTema = BehandlingTema.fraKodeDefaultUdefinert(prosessTaskData.getPropertyValue(BEHANDLINGSTEMA_KEY));
        DokumentTypeId dokumentTypeId = Optional.ofNullable(prosessTaskData.getPropertyValue(DOKUMENTTYPE_ID_KEY))
                .map(DokumentTypeId::fraKodeDefaultUdefinert).orElse(DokumentTypeId.UDEFINERT);
        behandlingTema = ArkivUtil.behandlingTemaFraDokumentType(behandlingTema, dokumentTypeId);

        String oppgaveId = opprettOppgave(prosessTaskData, behandlingTema, dokumentTypeId);

        LOG.info("Oppgave opprettet i Gosys med nummer: {}", oppgaveId);

        String forsendelseIdString = prosessTaskData.getPropertyValue(FORSENDELSE_ID_KEY);
        if (forsendelseIdString != null && !forsendelseIdString.isEmpty()) {
            opprettSletteTask(prosessTaskData);
        }
    }

    private void opprettSletteTask(ProsessTaskData prosessTaskData) {
        ProsessTaskData nesteStegProsessTaskData = new ProsessTaskData(SlettForsendelseTask.TASKNAME);
        // Gi selvbetjening tid til å polle ferdig + Kafka-hendelse tid til å nå fram (og bli ignorert)
        nesteStegProsessTaskData.setNesteKjøringEtter(LocalDateTime.now().plusHours(6));
        long nesteSekvens = prosessTaskData.getSekvens() == null ? 1L
                : Long.parseLong(prosessTaskData.getSekvens()) + 1;
        nesteStegProsessTaskData.setSekvens(Long.toString(nesteSekvens));
        nesteStegProsessTaskData.setProperties(prosessTaskData.getProperties());
        nesteStegProsessTaskData.setPayload(prosessTaskData.getPayload());
        nesteStegProsessTaskData.setGruppe(prosessTaskData.getGruppe());
        prosessTaskRepository.lagre(nesteStegProsessTaskData);
    }


    private String opprettOppgave(ProsessTaskData prosessTaskData, BehandlingTema behandlingTema,
            DokumentTypeId dokumentTypeId) {
        final Optional<String> fødselsnr = hentPersonidentifikatorFraTaskData(prosessTaskData.getAktørId());
        final Optional<String> annenpartFnr = hentAnnenPartFraTaskData(prosessTaskData);
        final String enhetInput = prosessTaskData.getPropertyValue(JOURNAL_ENHET);
        // Oppgave har ikke mapping for alle undertyper fødsel/adopsjon
        final String brukBT = BehandlingTema.forYtelseUtenFamilieHendelse(behandlingTema).getOffisiellKode();

        String arkivId = prosessTaskData.getPropertyValue(ARKIV_ID_KEY);

        // Overstyr saker fra NFP+NK, deretter egen logikk hvis fødselsnummer ikke er
        // oppgitt
        final String enhetId = enhetsidTjeneste.hentFordelingEnhetId(Tema.FORELDRE_OG_SVANGERSKAPSPENGER, behandlingTema,
                Optional.ofNullable(enhetInput), fødselsnr, annenpartFnr);
        final String beskrivelse = lagBeskrivelse(behandlingTema, dokumentTypeId, prosessTaskData);


        var request = OpprettOppgave.getBuilder()
                .medAktoerId(prosessTaskData.getAktørId())
                .medSaksreferanse(prosessTaskData.getPropertyValue(SAKSNUMMER_KEY))
                .medTildeltEnhetsnr(enhetId)
                .medOpprettetAvEnhetsnr(enhetId)
                .medJournalpostId(arkivId)
                .medAktivDato(LocalDate.now())
                .medFristFerdigstillelse(helgeJustertFrist(LocalDate.now().plusDays(1L)))
                .medBeskrivelse(beskrivelse)
                .medTemagruppe(Temagrupper.FAMILIEYTELSER.getKode())
                .medTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER.getOffisiellKode())
                .medBehandlingstema(brukBT)
                .medOppgavetype(OPPGAVETYPER_JFR)
                .medPrioritet(Prioritet.NORM);
        var oppgave = restKlient.opprettetOppgave(request);
        LOG.info("FPFORDEL GOSYS opprettet oppgave {}", oppgave);
        return oppgave.getId().toString();
    }

    private String lagBeskrivelse(BehandlingTema behandlingTema, DokumentTypeId dokumentTypeId, ProsessTaskData data) {
        if (DokumentTypeId.UDEFINERT.equals(dokumentTypeId)) {
            return BehandlingTema.UDEFINERT.equals(behandlingTema) ? "Journalføring" : behandlingTema.getTermNavn();
        }
        String beskrivelse = dokumentTypeId.getTermNavn();
        if (DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL.equals(dokumentTypeId)
                && data.getPropertyValue(MottakMeldingDataWrapper.FØRSTE_UTTAKSDAG_KEY) != null) {
            String uttakStart = data.getPropertyValue(MottakMeldingDataWrapper.FØRSTE_UTTAKSDAG_KEY);
            beskrivelse = beskrivelse + " (" + uttakStart + ")";
        }
        if (DokumentTypeId.INNTEKTSMELDING.equals(dokumentTypeId)) {
            if (data.getPropertyValue(MottakMeldingDataWrapper.INNTEKTSMELDING_YTELSE) != null) {
                beskrivelse = beskrivelse + " ("
                        + data.getPropertyValue(MottakMeldingDataWrapper.INNTEKTSMELDING_YTELSE) + ")";
            }
            if (data.getPropertyValue(MottakMeldingDataWrapper.INNTEKSTMELDING_STARTDATO_KEY) != null) {
                beskrivelse = beskrivelse + " ("
                        + (data.getPropertyValue(MottakMeldingDataWrapper.INNTEKSTMELDING_STARTDATO_KEY)) + ")";
            }
        }
        return beskrivelse;
    }

    // Sett frist til mandag hvis fristen er i helgen.
    private LocalDate helgeJustertFrist(LocalDate dato) {
        if (dato.getDayOfWeek().getValue() > DayOfWeek.FRIDAY.getValue()) {
            return dato.plusDays(1L + DayOfWeek.SUNDAY.getValue() - dato.getDayOfWeek().getValue());
        }
        return dato;
    }

    private Optional<String> hentPersonidentifikatorFraTaskData(String aktørId) {
        if (aktørId == null) {
            return Optional.empty();
        }
        return aktørConsumer.hentPersonIdentForAktørId(aktørId);
    }

    private Optional<String> hentAnnenPartFraTaskData(ProsessTaskData prosessTaskData) {
        var annenpart = prosessTaskData.getPropertyValue(ANNEN_PART_ID_KEY);
        if (annenpart == null) {
            return Optional.empty();
        }
        try {
            return aktørConsumer.hentPersonIdentForAktørId(annenpart);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
