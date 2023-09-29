package no.nav.foreldrepenger.mottak.domene.oppgavebehandling;

import no.nav.foreldrepenger.journalføring.domene.JournalpostId;
import no.nav.vedtak.felles.integrasjon.oppgave.v1.Oppgaver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.journalføring.oppgave.Journalføringsoppgave;
import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;

@Dependent
@ProsessTask(value = "integrasjon.gsak.ferdigstillOppgave", maxFailedRuns = 2)
public class FerdigstillOppgaveTask implements ProsessTaskHandler {

    public static final String OPPGAVEID_KEY = "oppgaveId";
    public static final String JOURNALPOSTID_KEY = "journalpostId";

    private static final Logger LOG = LoggerFactory.getLogger(FerdigstillOppgaveTask.class);

    private final Journalføringsoppgave oppgaver;

    private final Oppgaver oppgaverKlient;

    @Inject
    public FerdigstillOppgaveTask(Journalføringsoppgave oppgaver, Oppgaver oppgaverKlient) {
        this.oppgaver = oppgaver;
        this.oppgaverKlient = oppgaverKlient;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        var oppgaveId = prosessTaskData.getPropertyValue(OPPGAVEID_KEY);
        var journalpostId = prosessTaskData.getPropertyValue(JOURNALPOSTID_KEY);
        if (oppgaveId != null) {
            oppgaverKlient.ferdigstillOppgave(oppgaveId);
            LOG.info("Ferdigstilte eksterne oppgave med id {}", oppgaveId);
        }
        if (journalpostId != null) {
            oppgaver.ferdigstillAlleÅpneJournalføringsoppgaverFor(JournalpostId.fra(journalpostId));
            LOG.info("Ferdigstilte lokalt oppgave med id {}", journalpostId);
        }
    }
}
