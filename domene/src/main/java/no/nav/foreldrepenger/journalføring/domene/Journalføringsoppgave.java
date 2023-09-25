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

    Oppgave hentOppgaveFor(String journalpostId);

    void reserverOppgaveFor(String journalpostId, String reserverFor);

    void avreserverOppgaveFor(String journalpostId);

    List<Oppgave> finnÅpneOppgaverFor(Set<String> enhet);
}
