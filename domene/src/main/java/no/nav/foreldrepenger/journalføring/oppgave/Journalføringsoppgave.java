package no.nav.foreldrepenger.journalføring.oppgave;

import java.util.List;
import java.util.Set;

import no.nav.foreldrepenger.journalføring.domene.JournalpostId;
import no.nav.foreldrepenger.journalføring.oppgave.domene.Oppgave;
import no.nav.foreldrepenger.journalføring.oppgave.domene.NyOppgave;

public interface Journalføringsoppgave {

    String opprettJournalføringsoppgaveFor(NyOppgave nyOppgave);

    String opprettGosysJournalføringsoppgaveFor(NyOppgave nyOppgave);

    boolean finnesÅpeneJournalføringsoppgaverFor(JournalpostId journalpostId);

    void ferdigstillAlleÅpneJournalføringsoppgaverFor(JournalpostId journalpostId);

    Oppgave hentOppgaveFor(String oppgaveId);

    void reserverOppgaveFor(String oppgaveId, String saksbehandlerId);

    void avreserverOppgaveFor(String oppgaveId);

    List<Oppgave> finnÅpneOppgaverFor(Set<String> enhet);
}
