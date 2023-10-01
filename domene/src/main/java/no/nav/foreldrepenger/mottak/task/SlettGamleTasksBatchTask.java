package no.nav.foreldrepenger.mottak.task;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;

@Dependent
@ProsessTask(value = "vedlikehold.tasks.slettgamle", cronExpression = "0 45 1 * * *", maxFailedRuns = 1)
public class SlettGamleTasksBatchTask implements ProsessTaskHandler {

    private static final Logger LOG = LoggerFactory.getLogger(SlettGamleTasksBatchTask.class);

    private final ProsessTaskTjeneste prosessTaskTjeneste;
    private final DokumentRepository dokumentRepository;

    @Inject
    public SlettGamleTasksBatchTask(ProsessTaskTjeneste prosessTaskTjeneste,
                                    DokumentRepository dokumentRepository) {
        this.prosessTaskTjeneste = prosessTaskTjeneste;
        this.dokumentRepository = dokumentRepository;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        var slettetTask = prosessTaskTjeneste.slettÅrsgamleFerdige();
        LOG.info("Slettet {} tasks som er over ett år gamle.", slettetTask);
        var slettetJournalpost = dokumentRepository.slettJournalpostLokalEldreEnn(LocalDate.now().minusYears(1));
        LOG.info("Slettet {} journalposter som er over ett år gamle.", slettetJournalpost);
    }
}
