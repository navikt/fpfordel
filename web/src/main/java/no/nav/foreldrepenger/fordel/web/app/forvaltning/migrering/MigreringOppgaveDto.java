package no.nav.foreldrepenger.fordel.web.app.forvaltning.migrering;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import no.nav.foreldrepenger.journalføring.oppgave.lager.AktørId;
import no.nav.foreldrepenger.journalføring.oppgave.lager.YtelseType;

public record MigreringOppgaveDto(@Valid @Size List<OppgaveDto> oppgaver) {

    public record OppgaveDto(@Size @Pattern(regexp = "^[\\p{Alnum}_.\\-]*$") String journalpostId,
                             @Size @Pattern(regexp = "^[\\p{Alnum}_.\\-]*$") String enhet,
                             LocalDate frist,
                             @Valid AktørId brukerId,
                             @Valid YtelseType ytelseType,
                             @Size @Pattern(regexp = "^[\\p{P}\\p{L}\\p{N}\\p{Alnum}\\p{Punct}\\p{Space}\\\\_.\\-]*$") String beskrivelse,
                             @Size @Pattern(regexp = "^[\\p{Alnum}_.\\-]*$") String reservertAv) {}
}
