package no.nav.foreldrepenger.mottak.task;

import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.journalføring.domene.JournalføringsOppgave;
import no.nav.foreldrepenger.mottak.domene.oppgavebehandling.OpprettGSakOppgaveTask;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.felles.WrappedProsessTaskHandler;
import no.nav.foreldrepenger.mottak.journal.ArkivTjeneste;
import no.nav.foreldrepenger.mottak.person.PersonInformasjon;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;
import no.nav.vedtak.felles.prosesstask.api.TaskType;

/**
 * <p>
 * ProssessTask som utleder journalføringsbehov og forsøker rette opp disse.
 * </p>
 */
@ApplicationScoped
@ProsessTask(TilJournalføringTask.TASKNAME)
public class TilJournalføringTask extends WrappedProsessTaskHandler {

    static final String TASKNAME = "fordeling.tilJournalforing";

    private static final Logger LOG = LoggerFactory.getLogger(TilJournalføringTask.class);
    private static final String AUTOMATISK_ENHET = "9999";
    private static final String PROSESSERING_AV_PRECONDITIONS_FOR_S_MANGLER_S_TASK_ID_S = "Prosessering av preconditions for %s mangler %s. TaskId: %s";

    private ArkivTjeneste arkivTjeneste;
    private PersonInformasjon aktør;
    private JournalføringsOppgave journalføringsOppgave;

    public TilJournalføringTask() {

    }

    @Inject
    public TilJournalføringTask(ProsessTaskTjeneste taskTjeneste, ArkivTjeneste arkivTjeneste, PersonInformasjon aktørConsumer,
                                JournalføringsOppgave journalføringsOppgave) {
        super(taskTjeneste);
        this.arkivTjeneste = arkivTjeneste;
        this.aktør = aktørConsumer;
        this.journalføringsOppgave = journalføringsOppgave;
    }

    @Override
    public void precondition(MottakMeldingDataWrapper dataWrapper) {
        if (dataWrapper.getAktørId().isEmpty()) {
            throw new TekniskException("FP-941984",
                String.format(PROSESSERING_AV_PRECONDITIONS_FOR_S_MANGLER_S_TASK_ID_S, TASKNAME, MottakMeldingDataWrapper.AKTØR_ID_KEY,
                    dataWrapper.getId()));
        }
        if (dataWrapper.getSaksnummer().isEmpty()) {
            throw new TekniskException("FP-941985",
                String.format(PROSESSERING_AV_PRECONDITIONS_FOR_S_MANGLER_S_TASK_ID_S, TASKNAME, MottakMeldingDataWrapper.SAKSNUMMER_KEY,
                    dataWrapper.getId()));
        }
        if (dataWrapper.getArkivId() == null || dataWrapper.getArkivId().isEmpty()) {
            throw new TekniskException("FP-941986",
                String.format(PROSESSERING_AV_PRECONDITIONS_FOR_S_MANGLER_S_TASK_ID_S, TASKNAME, MottakMeldingDataWrapper.ARKIV_ID_KEY,
                    dataWrapper.getId()));
        }
    }

    @Transactional
    @Override
    public MottakMeldingDataWrapper doTask(MottakMeldingDataWrapper w) {
        Optional<String> fnr = w.getAktørId().flatMap(aktør::hentPersonIdentForAktørId);
        if (fnr.isEmpty()) {
            throw new TekniskException("FP-254631", String.format("Fant ikke personident for aktørId i task %s.  TaskId: %s", TASKNAME, w.getId()));
        }
        var saksnummer = w.getSaksnummer().orElseThrow(() -> new IllegalStateException("Utviklerfeil: Mangler saksnummer"));
        try {
            var journalpost = arkivTjeneste.hentArkivJournalpost(w.getArkivId());
            arkivTjeneste.settTilleggsOpplysninger(journalpost, w.getDokumentTypeId().orElse(DokumentTypeId.UDEFINERT), false);
        } catch (Exception e) {
            LOG.info("Feil ved setting av tilleggsopplysninger for journalpostId {}", w.getArkivId());
        }
        // Annet dokument fra dokumentmottak (scanning, altinn). Kan skippe
        // unntakshåndtering. Bør feile.
        try {
            var innkommendeSaksnummer = w.getInnkommendeSaksnummer();
            if (innkommendeSaksnummer.isPresent()) {
                LOG.info("FORDEL OPPRETT/FERDIG presatt saksnummer {} for journalpost {}", innkommendeSaksnummer.get(), w.getArkivId());
            } else {
                arkivTjeneste.oppdaterMedSak(w.getArkivId(), saksnummer, w.getAktørId().orElseThrow());
            }
            arkivTjeneste.ferdigstillJournalføring(w.getArkivId(), w.getJournalførendeEnhet().orElse(AUTOMATISK_ENHET));
        } catch (Exception e) {
            LOG.info("Feil journaltilstand. Forventet tilstand: endelig, fikk Midlertidig");
            return w.nesteSteg(TaskType.forProsessTask(OpprettGSakOppgaveTask.class));
        }
        try {
            journalføringsOppgave.ferdigstillÅpneJournalføringsOppgaver(w.getArkivId());
        } catch (Exception e) {
            LOG.info("FPFORDEL JFR-OPPGAVE: feil ved ferdigstilling av åpne oppgaver for journalpostId: {}", w.getArkivId());
        }
        return w.nesteSteg(TaskType.forProsessTask(VLKlargjørerTask.class));
    }

}
