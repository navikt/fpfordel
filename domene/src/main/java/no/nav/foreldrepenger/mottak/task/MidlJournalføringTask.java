package no.nav.foreldrepenger.mottak.task;

import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.transaction.Transactional;

import no.nav.foreldrepenger.fordel.kodeverdi.MottakKanal;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentRepository;
import no.nav.foreldrepenger.mottak.domene.oppgavebehandling.OpprettGSakOppgaveTask;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingFeil;
import no.nav.foreldrepenger.mottak.felles.WrappedProsessTaskHandler;
import no.nav.foreldrepenger.mottak.journal.ArkivTjeneste;
import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseStatus;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

/**
 * <p>
 * ProssessTask som utleder journalføringsbehov og forsøker rette opp disse.
 * </p>
 */
@Dependent
@ProsessTask(MidlJournalføringTask.TASKNAME)
public class MidlJournalføringTask extends WrappedProsessTaskHandler {

    public static final String TASKNAME = "fordeling.midlJournalforing";

    private ArkivTjeneste arkivTjeneste;
    private final DokumentRepository repo;

    @Inject
    public MidlJournalføringTask(ProsessTaskRepository prosessTaskRepository,
            ArkivTjeneste arkivTjeneste,
            DokumentRepository repo) {
        super(prosessTaskRepository);
        this.arkivTjeneste = arkivTjeneste;
        this.repo = repo;
    }

    @Override
    public void precondition(MottakMeldingDataWrapper dataWrapper) {
        if (dataWrapper.getAktørId().isEmpty()) {
            throw MottakMeldingFeil.FACTORY.prosesstaskPreconditionManglerProperty(TASKNAME,
                    MottakMeldingDataWrapper.AKTØR_ID_KEY, dataWrapper.getId()).toException();
        }
    }

    @Transactional
    @Override
    public MottakMeldingDataWrapper doTask(MottakMeldingDataWrapper dataWrapper) {
        Optional<UUID> forsendelseId = dataWrapper.getForsendelseId();
        if ((dataWrapper.getArkivId() == null) && forsendelseId.isPresent()) { // Vi har ikke journalpostID - journalfør
            var opprettetJournalpost = arkivTjeneste.opprettJournalpost(forsendelseId.get(),
                    dataWrapper.getAvsenderId()
                            .orElseGet(() -> dataWrapper.getAktørId().orElseThrow(() -> new IllegalStateException("Hvor ble det av brukers id?"))));
            dataWrapper.setArkivId(opprettetJournalpost.getJournalpostId());
        }
        forsendelseId.ifPresent(fid -> repo.oppdaterForsendelseMedArkivId(fid, dataWrapper.getArkivId(), ForsendelseStatus.GOSYS));
        forsendelseId.ifPresent(
                fid -> repo.lagreJournalpostLokal(dataWrapper.getArkivId(), MottakKanal.SELVBETJENING.getKode(), "MIDLERTIDIG", fid.toString()));
        return dataWrapper.nesteSteg(OpprettGSakOppgaveTask.TASKNAME);
    }
}
