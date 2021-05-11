package no.nav.foreldrepenger.mottak.task;

import java.util.Optional;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.mottak.domene.oppgavebehandling.OpprettGSakOppgaveTask;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.felles.WrappedProsessTaskHandler;
import no.nav.foreldrepenger.mottak.journal.ArkivTjeneste;
import no.nav.foreldrepenger.mottak.person.PersonInformasjon;
import no.nav.vedtak.exception.TekniskException;
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

    private static final Logger LOG = LoggerFactory.getLogger(TilJournalføringTask.class);
    private static final String AUTOMATISK_ENHET = "9999";

    private final ArkivTjeneste arkivTjeneste;
    private final PersonInformasjon aktør;

    @Inject
    public TilJournalføringTask(ProsessTaskRepository prosessTaskRepository,
            ArkivTjeneste arkivTjeneste,
            PersonInformasjon aktørConsumer) {
        super(prosessTaskRepository);
        this.arkivTjeneste = arkivTjeneste;
        this.aktør = aktørConsumer;
    }

    @Override
    public void precondition(MottakMeldingDataWrapper dataWrapper) {
        if (dataWrapper.getAktørId().isEmpty()) {
            throw new TekniskException("FP-941984",
            String.format("Prosessering av preconditions for %s mangler %s. TaskId: %s", TASKNAME, MottakMeldingDataWrapper.AKTØR_ID_KEY, dataWrapper.getId()));
        }
        if (dataWrapper.getSaksnummer().isEmpty()) {
            throw new TekniskException("FP-941984",
            String.format("Prosessering av preconditions for %s mangler %s. TaskId: %s", TASKNAME, MottakMeldingDataWrapper.SAKSNUMMER_KEY, dataWrapper.getId()));
        }
        if (dataWrapper.getArkivId() == null || dataWrapper.getArkivId().isEmpty()) {
            throw new TekniskException("FP-941984",
            String.format("Prosessering av preconditions for %s mangler %s. TaskId: %s", TASKNAME, MottakMeldingDataWrapper.ARKIV_ID_KEY, dataWrapper.getId()));
        }
    }

    @Transactional
    @Override
    public MottakMeldingDataWrapper doTask(MottakMeldingDataWrapper w) {
        Optional<String> fnr = w.getAktørId().flatMap(aktør::hentPersonIdentForAktørId);
        if (fnr.isEmpty()) {
            throw new TekniskException("FP-254631",
            String.format("Fant ikke personident for aktørId i task %s.  TaskId: %s", TASKNAME, w.getId()));
        }
        var saksnummer = w.getSaksnummer().orElseThrow(() -> new IllegalStateException("Utviklerfeil: Mangler saksnummer"));
        // Annet dokument fra dokumentmottak (scanning, altinn). Kan skippe unntakshåndtering. Bør feile.
        try {
            if (w.getInnkommendeSaksnummer().isEmpty()) {
                arkivTjeneste.oppdaterMedSak(w.getArkivId(), saksnummer, w.getAktørId().orElseThrow());
            } else {
                LOG.info("FORDEL OPPRETT/FERDIG presatt saksnummer {} for journalpost {}", w.getInnkommendeSaksnummer().get(), w.getArkivId());
            }
            arkivTjeneste.ferdigstillJournalføring(w.getArkivId(), w.getJournalførendeEnhet().orElse(AUTOMATISK_ENHET));
        } catch (Exception e) {
            LOG.info("Feil journaltilstand. Forventet tilstand: endelig, fikk Midlertidig");
            return w.nesteSteg(OpprettGSakOppgaveTask.TASKNAME);
        }
        return w.nesteSteg(KlargjorForVLTask.TASKNAME);
    }

}
