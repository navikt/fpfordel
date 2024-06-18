package no.nav.foreldrepenger.journalføring.oppgave.domene;

import java.time.LocalDate;

import no.nav.foreldrepenger.journalføring.oppgave.lager.YtelseType;

public record Oppgave(String oppgaveId,
                      // trenges kun for oppgaver fra gosys, for lokale oppgaver er den lik til journalpostId og ikke brukes noe særlig.
                      String journalpostId,
                      String aktørId,
                      YtelseType ytelseType,
                      String tildeltEnhetsnr,
                      LocalDate fristFerdigstillelse,
                      LocalDate aktivDato,
                      Oppgavestatus status,
                      String beskrivelse,
                      String tilordnetRessurs,
                      Kilde kilde) {

    public enum Kilde {
        LOKAL,
        GOSYS,
    }

    public static OppgaveBuilder builder() {
        return new OppgaveBuilder();
    }
}
