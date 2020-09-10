package no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto;

import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import no.nav.foreldrepenger.sikkerhet.abac.AppAbacAttributtType;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.AbacDto;

public class ForsendelseIdDto implements AbacDto {

    @NotNull
    private UUID forsendelseId;

    public ForsendelseIdDto(@Valid String forsendelseId) {
        this.forsendelseId = UUID.fromString(forsendelseId);
    }

    public UUID getForsendelseId() {
        return forsendelseId;
    }

    @Override
    public AbacDataAttributter abacAttributter() {
        return AbacDataAttributter.opprett().leggTil(AppAbacAttributtType.FORSENDELSE_UUID, getForsendelseId());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[forsendelseId=" + forsendelseId + "]";
    }

}
