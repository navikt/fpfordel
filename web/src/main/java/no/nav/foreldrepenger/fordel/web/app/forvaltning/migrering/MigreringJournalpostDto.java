package no.nav.foreldrepenger.fordel.web.app.forvaltning.migrering;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record MigreringJournalpostDto(@Valid @Size List<JournalpostDto> journalposter) {

    public record JournalpostDto(@Size @Pattern(regexp = "^[\\p{Alnum}_.\\-]*$") String journalpostId,
                                 @Size @Pattern(regexp = "^[\\p{Alnum}_.\\-]*$") String tilstand,
                                 @Size @Pattern(regexp = "^[\\p{Alnum}_.\\-]*$") String kanal,
                                 @Size @Pattern(regexp = "^[\\p{P}\\p{L}\\p{N}\\p{Alnum}\\p{Punct}\\p{Space}\\\\_.\\-]*$") String referanse) {}
}

