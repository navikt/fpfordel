package no.nav.foreldrepenger.journalføring.oppgave.domene;

import java.time.LocalDate;

import no.nav.foreldrepenger.journalføring.oppgave.lager.YtelseType;

public record Oppgave(String id,
                      String aktoerId,
                      YtelseType ytelseType,
                      String tildeltEnhetsnr,
                      LocalDate fristFerdigstillelse,
                      LocalDate aktivDato,
                      Oppgavestatus status,
                      String beskrivelse,
                      String tilordnetRessurs,
                      Kilde kilde,
                      String kildeId) {

    public enum Kilde { LOKAL,
        GOSYS
    }

    public static OppgaveBuilder builder() {
        return new OppgaveBuilder();
    }
}
