package no.nav.foreldrepenger.journalføring.domene;

import java.util.List;
import java.util.Set;

public interface JournalføringsOppgave {

    String opprettJournalføringsOppgave(String journalpostId,
                                        String enhetId,
                                        String aktørId,
                                        String saksref,
                                        String behandlingTema,
                                        String beskrivelse);

    boolean finnesÅpenJournalføringsoppgaveForJournalpost(String oppgaveId);

    void ferdigstillÅpneJournalføringsOppgaver(String oppgaveId);

    Oppgave hentOppgave(String oppgaveId);

    void reserverOppgave(String oppgaveId, String reserverFor);

    void avreserverOppgave(String oppgaveId);

    List<Oppgave> finnÅpneOppgaverFor(Set<String> enhet);
}
