package no.nav.foreldrepenger.mottak.task;

import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.foreldrepenger.fordel.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentRepository;
import no.nav.foreldrepenger.mottak.domene.oppgavebehandling.OpprettGSakOppgaveTask;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingFeil;
import no.nav.foreldrepenger.mottak.felles.WrappedProsessTaskHandler;
import no.nav.foreldrepenger.mottak.journal.dokumentforsendelse.DokumentforsendelseResponse;
import no.nav.foreldrepenger.mottak.tjeneste.TilJournalføringTjeneste;
import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseStatus;
import no.nav.vedtak.felles.jpa.Transaction;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

/**
 * <p>ProssessTask som utleder journalføringsbehov og forsøker rette opp disse.</p>
 */
@Dependent
@ProsessTask(MidlJournalføringTask.TASKNAME)
public class MidlJournalføringTask extends WrappedProsessTaskHandler {

    public static final String TASKNAME = "fordeling.midlJournalforing";


    private final TilJournalføringTjeneste journalføringTjeneste;
    private final DokumentRepository dokumentRepository;

    @Inject
    public MidlJournalføringTask(ProsessTaskRepository prosessTaskRepository,
                                 TilJournalføringTjeneste journalføringTjeneste,
                                 KodeverkRepository kodeverkRepository,
                                 DokumentRepository dokumentRepository) {
        super(prosessTaskRepository, kodeverkRepository);
        this.journalføringTjeneste = journalføringTjeneste;
        this.dokumentRepository = dokumentRepository;
    }

    @Override
    public void precondition(MottakMeldingDataWrapper dataWrapper) {
        if (!dataWrapper.getAktørId().isPresent()) {
            throw MottakMeldingFeil.FACTORY.prosesstaskPreconditionManglerProperty(TASKNAME, MottakMeldingDataWrapper.AKTØR_ID_KEY, dataWrapper.getId()).toException();
        }
    }

    @Transaction
    @Override
    public MottakMeldingDataWrapper doTask(MottakMeldingDataWrapper dataWrapper) {
        Optional<UUID> forsendelseId = dataWrapper.getForsendelseId();
        if (dataWrapper.getArkivId() == null && forsendelseId.isPresent()) { // Vi har ikke journalpostID - journalfør
            DokumentforsendelseResponse response = journalføringTjeneste.journalførDokumentforsendelse(forsendelseId.get(), dataWrapper.getSaksnummer(), dataWrapper.getAvsenderId(), false, dataWrapper.getRetryingTask());
            dataWrapper.setArkivId(response.getJournalpostId());
        }
        forsendelseId.ifPresent(fid -> dokumentRepository.oppdaterForseldelseMedArkivId(fid, dataWrapper.getArkivId(), ForsendelseStatus.GOSYS));
        return dataWrapper.nesteSteg(OpprettGSakOppgaveTask.TASKNAME);
    }
}
