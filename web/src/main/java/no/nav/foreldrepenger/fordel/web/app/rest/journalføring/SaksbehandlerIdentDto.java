package no.nav.foreldrepenger.fordel.web.app.rest.journalføring;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record SaksbehandlerIdentDto(@Pattern(regexp = "^[a-zA-Z0-9]*$") @NotNull String ident) {
}
