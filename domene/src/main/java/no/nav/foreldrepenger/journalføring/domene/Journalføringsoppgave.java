package no.nav.foreldrepenger.journalføring.domene;

import java.util.List;
import java.util.Set;

public interface Journalføringsoppgave {

    String opprettJournalføringsoppgaveFor(String journalpostId,
                                           String enhetId,
                                           String aktørId,
                                           String saksref,
                                           String behandlingTema,
                                           String beskrivelse);

    boolean finnesÅpeneJournalføringsoppgaverFor(String journalpostId);

    void ferdigstillAlleÅpneJournalføringsoppgaverFor(String journalpostId);

    Oppgave hentOppgaveFor(String oppgaveId);

    void reserverOppgaveFor(String oppgaveId, String reserverFor);

    void avreserverOppgaveFor(String oppgaveId);

    List<Oppgave> finnÅpneOppgaverFor(Set<String> enhet);
}
