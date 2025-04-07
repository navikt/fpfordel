package no.nav.foreldrepenger.mottak.task;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.journalføring.domene.JournalpostId;
import no.nav.foreldrepenger.journalføring.oppgave.Journalføringsoppgave;
import no.nav.foreldrepenger.mottak.task.sikkerhetsnett.SikkerhetsnettJournalpost;
import no.nav.foreldrepenger.mottak.task.sikkerhetsnett.SikkerhetsnettKlient;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskGruppe;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;

@Dependent
@ProsessTask(value = "vedlikehold.once.sikkerhetsnett", maxFailedRuns = 1)
public class SikkerhetsnettOnceTask implements ProsessTaskHandler {

    private static final Logger LOG = LoggerFactory.getLogger(SikkerhetsnettOnceTask.class);

    private final Journalføringsoppgave journalføringsoppgave;
    private final SikkerhetsnettKlient sikkerhetsnettKlient;
    private final ProsessTaskTjeneste prosessTaskTjeneste;

    @Inject
    public SikkerhetsnettOnceTask(Journalføringsoppgave journalføringsoppgave,
                                  SikkerhetsnettKlient sikkerhetsnettKlient,
                                  ProsessTaskTjeneste prosessTaskTjeneste) {
        this.journalføringsoppgave = journalføringsoppgave;
        this.sikkerhetsnettKlient = sikkerhetsnettKlient;
        this.prosessTaskTjeneste = prosessTaskTjeneste;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        var åpneJournalposterUtenOppgave = sikkerhetsnettKlient.hentÅpneJournalposterEldreEnn(0).stream()
            .filter(jp -> jp.mottaksKanal() == null || !"EESSI".equals(jp.mottaksKanal()))
            .filter(jp -> !journalføringsoppgave.finnesÅpeneJournalføringsoppgaverFor(JournalpostId.fra(jp.journalpostId())))
            .toList();
        var åpneJournalposterTekst = åpneJournalposterUtenOppgave.stream()
            .map(SikkerhetsnettJournalpost::journalpostId)
            .collect(Collectors.joining(","));
        LOG.info("FPFORDEL SIKKERHETSNETT fant {} journalposter uten oppgave: {}", åpneJournalposterUtenOppgave.size(), åpneJournalposterTekst);
        var tasks = åpneJournalposterUtenOppgave.stream().map(SikkerhetsnettTask::opprettTask).toList();
        if (!tasks.isEmpty()) {
            var gruppe = new ProsessTaskGruppe().addNesteParallell(tasks);
            prosessTaskTjeneste.lagre(gruppe);
        }
    }
}
