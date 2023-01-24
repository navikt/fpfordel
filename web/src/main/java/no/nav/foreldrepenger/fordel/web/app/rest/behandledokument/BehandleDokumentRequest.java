package no.nav.foreldrepenger.fordel.web.app.rest.behandledokument;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public record BehandleDokumentRequest(@NotNull @Pattern(regexp = "^(-?[1-9]|[a-z0])[a-z0-9_:-]*$", message = "journalpostId ${validatedValue} har ikke gyldig verdi (pattern '{regexp}')") String journalpostId,
                                      @NotNull String saksnummer,
                                      @NotNull String enhetId) {
}