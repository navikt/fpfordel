package no.nav.foreldrepenger.mottak.klient;

import java.time.LocalDate;

import no.nav.foreldrepenger.kontrakter.fordel.SaksnummerDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record SakInfoDto(@NotNull @Valid SaksnummerDto saksnummer, @NotNull @Valid FagsakYtelseTypeDto ytelseType, @NotNull LocalDate opprettetDato, @NotNull @Valid FagsakStatusDto status, @Valid FamiliehendelseInfoDto familiehendelseInfoDto, LocalDate førsteUttaksdato) {
    public record FamiliehendelseInfoDto(LocalDate familiehendelseDato, @Valid FamilieHendelseTypeDto familihendelseType) {}
}

