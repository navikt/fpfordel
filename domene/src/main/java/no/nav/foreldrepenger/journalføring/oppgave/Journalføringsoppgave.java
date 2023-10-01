package no.nav.foreldrepenger.journalføring.oppgave;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import no.nav.foreldrepenger.journalføring.domene.JournalpostId;
import no.nav.foreldrepenger.journalføring.oppgave.domene.NyOppgave;
import no.nav.foreldrepenger.journalføring.oppgave.domene.Oppgave;

public interface Journalføringsoppgave {

    String opprettJournalføringsoppgaveFor(NyOppgave nyOppgave);

    String opprettGosysJournalføringsoppgaveFor(NyOppgave nyOppgave);

    boolean finnesÅpeneJournalføringsoppgaverFor(JournalpostId journalpostId);

    void ferdigstillAlleÅpneJournalføringsoppgaverFor(JournalpostId journalpostId);

    Oppgave hentOppgaveFor(JournalpostId journalpostId);

    Optional<Oppgave> hentLokalOppgaveFor(JournalpostId journalpostId);

    void ferdigstillLokalOppgaveFor(JournalpostId journalpostId);

    void reserverOppgaveFor(Oppgave oppgave, String saksbehandlerId);

    void avreserverOppgaveFor(Oppgave oppgave);

    List<Oppgave> finnÅpneOppgaverFor(Set<String> enhet);

    void flyttLokalOppgaveTilGosys(JournalpostId journalpostId);
}
