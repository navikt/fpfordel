package no.nav.foreldrepenger.mottak.felles;

import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.log.mdc.MDCOperations;

public abstract class WrappedProsessTaskHandler implements ProsessTaskHandler, Conditions {
    protected abstract MottakMeldingDataWrapper doTask(MottakMeldingDataWrapper w);

    protected ProsessTaskRepository prosessTaskRepository;

    public WrappedProsessTaskHandler(ProsessTaskRepository repo) {
        this.prosessTaskRepository = repo;
    }

    @Override
    public void doTask(ProsessTaskData data) {
        MDCOperations.ensureCallId();
        var w = new MottakMeldingDataWrapper(data);
        precondition(w);
        var neste = doTask(w);
        if (neste != null) {
            postcondition(neste);
            var taskdata = neste.getProsessTaskData();
            taskdata.setCallIdFraEksisterende();
            prosessTaskRepository.lagre(taskdata);
        }
    }
}
