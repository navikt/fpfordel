package no.nav.foreldrepenger.mottak.task;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;

@Dependent
@ProsessTask(value = "vedlikehold.tasks.retryfeilet", cronExpression = "0 1 7 * * *", maxFailedRuns = 1)
public class RekjørFeiledeTasksBatchTask implements ProsessTaskHandler {

    private static final Logger LOG = LoggerFactory.getLogger(RekjørFeiledeTasksBatchTask.class);

    private final ProsessTaskTjeneste prosessTaskTjeneste;

    @Inject
    public RekjørFeiledeTasksBatchTask(ProsessTaskTjeneste prosessTaskTjeneste) {
        this.prosessTaskTjeneste = prosessTaskTjeneste;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        var rekjørAlleFeiledeTasks = prosessTaskTjeneste.restartAlleFeiledeTasks();
        LOG.info("Rekjører alle feilede tasks. {} tasks ble oppdatert.", rekjørAlleFeiledeTasks);
    }
}
