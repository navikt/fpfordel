package no.nav.foreldrepenger.mottak.domene.oppgavebehandling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.journalføring.domene.JournalføringsOppgave;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@Dependent
@ProsessTask(value = "integrasjon.gsak.ferdigstillOppgave", maxFailedRuns = 2)
public class FerdigstillOppgaveTask implements ProsessTaskHandler {

    public static final String OPPGAVEID_KEY = "oppgaveId";

    private static final Logger LOG = LoggerFactory.getLogger(FerdigstillOppgaveTask.class);

    private final JournalføringsOppgave oppgaver;

    @Inject
    public FerdigstillOppgaveTask(JournalføringsOppgave oppgaver) {
        this.oppgaver = oppgaver;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        var oppgaveId = prosessTaskData.getPropertyValue(OPPGAVEID_KEY);
        if (oppgaveId != null) {
            oppgaver.ferdigstillÅpneJournalføringsOppgaver(oppgaveId);
            LOG.info("Ferdigstilte oppgave med id {}", oppgaveId);
        }
    }
}
