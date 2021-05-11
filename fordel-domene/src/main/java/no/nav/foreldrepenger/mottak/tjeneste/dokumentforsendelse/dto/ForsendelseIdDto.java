package no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto;

import java.util.UUID;

import javax.validation.constraints.NotNull;

public record ForsendelseIdDto(@NotNull UUID forsendelseId) {
    public ForsendelseIdDto(String forsendelseId) {
        this(UUID.fromString(forsendelseId));
    }
}
