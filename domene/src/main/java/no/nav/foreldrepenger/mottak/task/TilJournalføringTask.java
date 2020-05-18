package no.nav.foreldrepenger.mottak.task;

import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.fordel.kodeverdi.MottakKanal;
import no.nav.foreldrepenger.mottak.behandlendeenhet.EnhetsTjeneste;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentRepository;
import no.nav.foreldrepenger.mottak.domene.oppgavebehandling.OpprettGSakOppgaveTask;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingFeil;
import no.nav.foreldrepenger.mottak.felles.WrappedProsessTaskHandler;
import no.nav.foreldrepenger.mottak.felles.kafka.HendelseProdusent;
import no.nav.foreldrepenger.mottak.felles.kafka.SøknadFordeltOgJournalførtHendelse;
import no.nav.foreldrepenger.mottak.journal.ArkivTjeneste;
import no.nav.foreldrepenger.mottak.journal.dokumentforsendelse.DokumentforsendelseResponse;
import no.nav.foreldrepenger.mottak.journal.dokumentforsendelse.JournalTilstand;
import no.nav.foreldrepenger.mottak.tjeneste.TilJournalføringTjeneste;
import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseStatus;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumerMedCache;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

/**
 * <p>
 * ProssessTask som utleder journalføringsbehov og forsøker rette opp disse.
 * </p>
 */
@Dependent
@ProsessTask(TilJournalføringTask.TASKNAME)
public class TilJournalføringTask extends WrappedProsessTaskHandler {

    public static final String TASKNAME = "fordeling.tilJournalforing";
    public static final String JOURNALMANGLER_EXCEPTION_KODE = "FP-453958";

    private static final Logger LOG = LoggerFactory.getLogger(TilJournalføringTask.class);
    private static final String AUTOMATISK_ENHET = "9999";

    private final TilJournalføringTjeneste journalføring;
    private final ArkivTjeneste arkivTjeneste;
    private final EnhetsTjeneste enhetsidTjeneste;
    private final DokumentRepository dokumentRepository;
    private final AktørConsumerMedCache aktør;
    private final HendelseProdusent hendelseProdusent;

    @Inject
    public TilJournalføringTask(ProsessTaskRepository prosessTaskRepository,
                                TilJournalføringTjeneste journalføringTjeneste,
                                ArkivTjeneste arkivTjeneste,
                                EnhetsTjeneste enhetsidTjeneste,
                                HendelseProdusent hendelseProdusent,
                                DokumentRepository dokumentRepository,
                                AktørConsumerMedCache aktørConsumer) {
        super(prosessTaskRepository);
        this.journalføring = journalføringTjeneste;
        this.arkivTjeneste = arkivTjeneste;
        this.enhetsidTjeneste = enhetsidTjeneste;
        this.dokumentRepository = dokumentRepository;
        this.aktør = aktørConsumer;
        this.hendelseProdusent = hendelseProdusent;
    }

    @Override
    public void precondition(MottakMeldingDataWrapper dataWrapper) {
        if (dataWrapper.getAktørId().isEmpty()) {
            throw MottakMeldingFeil.FACTORY.prosesstaskPreconditionManglerProperty(TASKNAME,
                    MottakMeldingDataWrapper.AKTØR_ID_KEY, dataWrapper.getId()).toException();
        }
        if (dataWrapper.getSaksnummer().isEmpty()) {
            throw MottakMeldingFeil.FACTORY.prosesstaskPreconditionManglerProperty(TASKNAME,
                    MottakMeldingDataWrapper.SAKSNUMMER_KEY, dataWrapper.getId()).toException();
        }
    }

    @Transactional
    @Override
    public MottakMeldingDataWrapper doTask(MottakMeldingDataWrapper w) {
        Optional<String> fnr = w.getAktørId().flatMap(aktør::hentPersonIdentForAktørId);
        Optional<String> fnrAnnenPart = finnFnrAnnenPart(w);
        if (fnr.isEmpty()) {
            throw MottakMeldingFeil.FACTORY.fantIkkePersonidentForAktørId(TASKNAME, w.getId()).toException();
        }

        if (w.getArkivId() == null) {
            // Dokument fra selvbetjening, ikke journalført ennå.
            UUID forsendelseId = w.getForsendelseId().orElseThrow(IllegalStateException::new);
            DokumentforsendelseResponse response = journalføring.journalførDokumentforsendelse(forsendelseId,
                    w.getSaksnummer(), w.getAvsenderId(), true,
                    w.getRetryingTask());
            w.setArkivId(response.getJournalpostId());
            // Hvis endelig journalføring feiler (fx pga doktype annet), send til manuell
            // journalføring (journalpost er opprettet).
            if (!JournalTilstand.ENDELIG_JOURNALFØRT.equals(response.getJournalTilstand())) {
                MottakMeldingFeil.FACTORY.feilJournalTilstandForventetTilstandEndelig(response.getJournalTilstand())
                        .log(LOG);
                dokumentRepository.oppdaterForsendelseMedArkivId(forsendelseId, w.getArkivId(), ForsendelseStatus.GOSYS);
                // Det kommer en midlertidig-hendelse på Kafka om et par sekunder. Unngå å reagere på den.
                dokumentRepository.lagreJournalpostLokal(w.getArkivId(), MottakKanal.SELVBETJENING.getKode(), "MIDLERTIDIG", forsendelseId.toString());
                return w.nesteSteg(OpprettGSakOppgaveTask.TASKNAME);
            }
        } else {
            // Annet dokument fra dokumentmottak (scanning, altinn). Kan skippe unntakshåndtering. Bør feile.
            try {
                arkivTjeneste.ferdigstillJournalføring(w.getArkivId(), w.getSaksnummer().get(), w.getJournalførendeEnhet().orElse(AUTOMATISK_ENHET));
            } catch (Exception e) {
                MottakMeldingFeil.FACTORY.feilJournalTilstandForventetTilstandEndelig(JournalTilstand.MIDLERTIDIG_JOURNALFØRT).log(LOG);
                return w.nesteSteg(OpprettGSakOppgaveTask.TASKNAME);
            }
        }
        Optional<UUID> forsendelseId = w.getForsendelseId();
        if (forsendelseId.isPresent()) {
            var id = forsendelseId.get();
            dokumentRepository.oppdaterForsendelseMetadata(id, w.getArkivId(), w.getSaksnummer().get(),
                    ForsendelseStatus.PENDING);
            try {
                hendelseProdusent.send(
                        new SøknadFordeltOgJournalførtHendelse(w.getArkivId(), id, fnr.get(), w.getSaksnummer()),
                        id.toString());
            } catch (Exception e) {
                LOG.warn("fpfordel kafka hendelsepublisering feilet for forsendelse {}", id.toString(), e);
            }
        }
        return w.nesteSteg(KlargjorForVLTask.TASKNAME);
    }

    private Optional<String> finnFnrAnnenPart(MottakMeldingDataWrapper w) {
        try {
            return w.getAnnenPartId().flatMap(aktør::hentPersonIdentForAktørId);
        } catch (Exception e) {
            return Optional.empty();
        }

    }
}
