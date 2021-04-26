package no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto;

import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class ForsendelseIdDto {

    @NotNull
    private UUID forsendelseId;

    public ForsendelseIdDto(@Valid String forsendelseId) {
        this.forsendelseId = UUID.fromString(forsendelseId);
    }

    public UUID getForsendelseId() {
        return forsendelseId;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[forsendelseId=" + forsendelseId + "]";
    }

}
