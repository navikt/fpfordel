package no.nav.foreldrepenger.mottak.felles;

import static no.nav.vedtak.log.mdc.MDCOperations.ensureCallId;

import io.micrometer.core.instrument.Metrics;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

public abstract class WrappedProsessTaskHandler implements ProsessTaskHandler, Conditions {
    protected abstract MottakMeldingDataWrapper doTask(MottakMeldingDataWrapper w);

    protected ProsessTaskRepository prosessTaskRepository;

    public WrappedProsessTaskHandler() {

    }

    public WrappedProsessTaskHandler(ProsessTaskRepository repo) {
        this.prosessTaskRepository = repo;
    }

    @Override
    public void doTask(ProsessTaskData data) {
        ensureCallId();
        var w = new MottakMeldingDataWrapper(data);
        precondition(w);
        time(data, w);
        Object neste;
        if (neste != null) {
            postcondition(neste);
            var taskdata = neste.getProsessTaskData();
            taskdata.setCallIdFraEksisterende();
            prosessTaskRepository.lagre(taskdata);
        }
    }

    private MottakMeldingDataWrapper time(ProsessTaskData data, MottakMeldingDataWrapper w) {
        final MottakMeldingDataWrapper neste;
        Metrics.timer("task.time", "type", data.getTaskType()).record(() -> {
            neste = doTask(w);
        });
    }
}
