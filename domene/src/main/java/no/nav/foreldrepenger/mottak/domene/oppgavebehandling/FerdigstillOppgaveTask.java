package no.nav.foreldrepenger.mottak.domene.oppgavebehandling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.journalføring.domene.JournalpostId;
import no.nav.foreldrepenger.journalføring.oppgave.Journalføringsoppgave;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@Dependent
@ProsessTask(value = "integrasjon.gsak.ferdigstillOppgave", maxFailedRuns = 2)
public class FerdigstillOppgaveTask implements ProsessTaskHandler {
    public static final String JOURNALPOSTID_KEY = "journalpostId";

    private static final Logger LOG = LoggerFactory.getLogger(FerdigstillOppgaveTask.class);

    private final Journalføringsoppgave oppgaver;

    @Inject
    public FerdigstillOppgaveTask(Journalføringsoppgave oppgaver) {
        this.oppgaver = oppgaver;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        var journalpostId = prosessTaskData.getPropertyValue(JOURNALPOSTID_KEY);
        oppgaver.ferdigstillAlleÅpneJournalføringsoppgaverFor(JournalpostId.fra(journalpostId));
        LOG.info("Ferdigstilte lokalt oppgave med id {}", journalpostId);
    }
}
