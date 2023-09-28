package no.nav.foreldrepenger.journalføring.oppgave;

import no.nav.foreldrepenger.journalføring.domene.JournalpostId;
import no.nav.foreldrepenger.journalføring.oppgave.domene.Oppgave;
import no.nav.foreldrepenger.journalføring.oppgave.domene.OppgaveSystem;

import java.util.List;
import java.util.Set;

public interface Journalføringsoppgave {

    String opprettJournalføringsoppgaveFor(JournalpostId journalpostId,
                                           String enhetId,
                                           String aktørId,
                                           String saksref,
                                           String behandlingTema,
                                           String beskrivelse,
                                           OppgaveSystem oppgaveSystem);

    boolean finnesÅpeneJournalføringsoppgaverFor(JournalpostId journalpostId);

    void ferdigstillAlleÅpneJournalføringsoppgaverFor(JournalpostId journalpostId);

    Oppgave hentOppgaveFor(String oppgaveId);

    void reserverOppgaveFor(String oppgaveId, String saksbehandlerId);

    void avreserverOppgaveFor(String oppgaveId);

    List<Oppgave> finnÅpneOppgaverFor(Set<String> enhet);
}
