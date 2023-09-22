package no.nav.foreldrepenger.fordel.web.app.rest.journalføring;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SaksbehandlerIdentDto(@Size(max = 20) @NotNull String ident) {
}
