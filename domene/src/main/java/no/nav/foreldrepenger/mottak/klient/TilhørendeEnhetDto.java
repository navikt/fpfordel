package no.nav.foreldrepenger.mottak.klient;

import javax.validation.constraints.NotNull;

public record TilhørendeEnhetDto(@NotNull String enhetsnummer, @NotNull String enhetsnavn) {
}
