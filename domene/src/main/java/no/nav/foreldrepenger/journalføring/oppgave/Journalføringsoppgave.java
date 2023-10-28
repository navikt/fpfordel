package no.nav.foreldrepenger.journalføring.oppgave;

import java.util.List;
import java.util.Optional;

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

    /**
     * Returnerer ingenting om innlogget saksbehandler ikke har enheter i Los
     * Filtrerer alltid oppgaver tildelt til Klage enhet 4292
     * Filtrerer Skjermet oppgaver om saksbehandler ikke er med i 4883 enhet i Los
     * Filtrerer K6 oppgaver og saksbehandler ikke er med i 2103 enhet i Los
     * Filtrerer K6 utland oppgaver og saksbehandler ikke er med i 4806 enhet i Los
     * @return Liste med åpne journalføringsoppgaver.
     */
    List<Oppgave> finnÅpneOppgaverFiltrert();

    void flyttLokalOppgaveTilGosys(JournalpostId journalpostId);
}
