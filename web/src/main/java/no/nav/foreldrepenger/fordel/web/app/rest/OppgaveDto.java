package no.nav.foreldrepenger.fordel.web.app.rest;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

public record OppgaveDto(@NotNull Long id,
                         @NotNull String journalpostId ,
                         String aktørId, String fødselsnummer,
                         @NotNull String ytelseType,
                         @NotNull LocalDate frist,
                         OppgavePrioritet prioritet,
                         String beskrivelse,
                         @NotNull LocalDate opprettetDato,
                         @NotNull boolean journalpostHarMangler) {

}
