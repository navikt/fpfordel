package no.nav.foreldrepenger.mottak.felles;

import static no.nav.foreldrepenger.fordel.MDCUtils.ensureCallId;

import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;

public abstract class WrappedProsessTaskHandler implements ProsessTaskHandler, Conditions {

    protected ProsessTaskTjeneste prosessTaskTjeneste;

    protected WrappedProsessTaskHandler() {
    }

    protected WrappedProsessTaskHandler(ProsessTaskTjeneste prosessTaskTjeneste) {
        this.prosessTaskTjeneste = prosessTaskTjeneste;
    }

    protected abstract MottakMeldingDataWrapper doTask(MottakMeldingDataWrapper w);

    @Override
    public void doTask(ProsessTaskData data) {
        ensureCallId();
        var w = new MottakMeldingDataWrapper(data);
        precondition(w);
        var neste = doTask(w);
        if (neste != null) {
            postcondition(neste);
            var taskdata = neste.getProsessTaskData();
            prosessTaskTjeneste.lagre(taskdata);
        }
    }

}
