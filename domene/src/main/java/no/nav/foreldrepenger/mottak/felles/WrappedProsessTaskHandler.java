package no.nav.foreldrepenger.mottak.felles;

import static no.nav.foreldrepenger.fordel.MDCUtils.ensureCallId;

import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;

public abstract class WrappedProsessTaskHandler implements ProsessTaskHandler, Conditions {

    protected abstract MottakMeldingDataWrapper doTask(MottakMeldingDataWrapper w);

    protected ProsessTaskTjeneste prosessTaskTjeneste;

    public WrappedProsessTaskHandler() {

    }

    public WrappedProsessTaskHandler(ProsessTaskTjeneste prosessTaskTjeneste) {
        this.prosessTaskTjeneste = prosessTaskTjeneste;
    }

    @Override
    public void doTask(ProsessTaskData data) {
        ensureCallId();
        var w = new MottakMeldingDataWrapper(data);
        precondition(w);
        var neste = doTask(w);
        if (neste != null) {
            postcondition(neste);
            var taskdata = neste.getProsessTaskData();
            taskdata.setCallIdFraEksisterende();
            prosessTaskTjeneste.lagre(taskdata);
        }
    }

}
