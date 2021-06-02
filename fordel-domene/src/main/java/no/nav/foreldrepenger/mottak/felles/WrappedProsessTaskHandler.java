package no.nav.foreldrepenger.mottak.felles;

import static no.nav.vedtak.log.mdc.MDCOperations.ensureCallId;

import io.micrometer.core.instrument.Metrics;
import no.nav.vedtak.exception.VLException;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

public abstract class WrappedProsessTaskHandler implements ProsessTaskHandler, Conditions {
    private static final String TYPE = "type";
    private static final String FORDEL_TASK = "fordel.task";

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

        Metrics.counter(FORDEL_TASK, TYPE, data.getTaskType()).increment();
        try {
            var neste = Metrics.timer(FORDEL_TASK, TYPE, data.getTaskType()).recordCallable(() -> doTask(w));
            if (neste != null) {
                postcondition(neste);
                var taskdata = neste.getProsessTaskData();
                taskdata.setCallIdFraEksisterende();
                prosessTaskRepository.lagre(taskdata);
            }
        } catch (VLException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
}
