package no.nav.foreldrepenger.mottak.klient;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import no.nav.foreldrepenger.kontrakter.fordel.SaksnummerDto;

import java.time.LocalDate;

public record SakInfoDto(@NotNull @Valid SaksnummerDto saksnummer,
                         @NotNull @Valid FagsakYtelseTypeDto ytelseType,
                         @NotNull LocalDate opprettetDato,
                         @NotNull @Valid FagsakStatusDto status,
                         @Valid FamiliehendelseInfoDto familiehendelseInfoDto,
                         LocalDate førsteUttaksdato) {
    public record FamiliehendelseInfoDto(LocalDate familiehendelseDato,
                                         @Valid FamilieHendelseTypeDto familihendelseType) {
    }

    public enum FagsakYtelseTypeDto {
        ENGANGSTØNAD,
        FORELDREPENGER,
        SVANGERSKAPSPENGER
    }

    public enum FagsakStatusDto {
        UNDER_BEHANDLING,
        LØPENDE,
        AVSLUTTET
    }

    public enum FamilieHendelseTypeDto {
        FØDSEL,
        TERMIN,
        ADOPSJON,
        OMSORG
    }
}

