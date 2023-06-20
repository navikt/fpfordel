package no.nav.foreldrepenger.fordel.web.app.rest.journalføring;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public record SaksbehandlerIdentDto(@Pattern(regexp = "^[a-zA-Z0-9]*$") @NotNull String ident) {
}
