package no.nav.foreldrepenger.journalføring.oppgave.domene;

import no.nav.foreldrepenger.journalføring.domene.JournalpostId;

public record NyOppgave(JournalpostId journalpostId, String enhetId, String aktørId, String saksref, String behandlingTema, String beskrivelse) {
    public static NyOppgaveBuilder builder() {
        return new NyOppgaveBuilder();
    }
}
