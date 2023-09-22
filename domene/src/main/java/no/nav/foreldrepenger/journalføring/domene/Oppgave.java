package no.nav.foreldrepenger.journalføring.domene;

import java.time.LocalDate;

import no.nav.foreldrepenger.domene.YtelseType;

public record Oppgave(String id,
                      String aktoerId,
                      YtelseType ytelseType,
                      String tildeltEnhetsnr,
                      LocalDate fristFerdigstillelse,
                      LocalDate aktivDato,
                      Oppgavestatus status,
                      String beskrivelse,
                      String tilordnetRessurs) {

    public static OppgaveBuilder builder() {
        return new OppgaveBuilder();
    }
}
