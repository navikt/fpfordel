package no.nav.foreldrepenger.mottak.task;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;

@Dependent
@ProsessTask(value = "vedlikehold.tasks.slettgamle", cronExpression = "0 45 1 * * *", maxFailedRuns = 1)
public class SlettGamleTasksBatchTask implements ProsessTaskHandler {

    private static final Logger LOG = LoggerFactory.getLogger(SlettGamleTasksBatchTask.class);

    private final ProsessTaskTjeneste prosessTaskTjeneste;

    @Inject
    public SlettGamleTasksBatchTask(ProsessTaskTjeneste prosessTaskTjeneste) {
        this.prosessTaskTjeneste = prosessTaskTjeneste;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        var slettet = prosessTaskTjeneste.slettÅrsgamleFerdige();
        LOG.info("Slettet {} tasks som er over ett år gamle.", slettet);
    }
}