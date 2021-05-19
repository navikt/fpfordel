package no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto;

import java.util.UUID;

public record ForsendelseIdDto(UUID forsendelseId) {
    public ForsendelseIdDto(String uuid) {
        this(UUID.fromString(uuid));
    }
}
