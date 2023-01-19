package no.nav.foreldrepenger.fordel.web.app.rest;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record OppgaveDto(@NotNull Long id, @NotNull String journalpostId , String aktørId, @NotNull String ytelseType, @NotNull LocalDate frist, OppgavePrioritet prioritet,
                         String beskrivelse, @NotNull LocalDateTime opprettetTidspunkt, @NotNull boolean journalpostHarMangler) {}
