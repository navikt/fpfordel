package no.nav.foreldrepenger.mottak.klient;

import java.time.LocalDate;

import no.nav.foreldrepenger.kontrakter.fordel.SaksnummerDto;

public record SakInfoDto(SaksnummerDto saksnummer, YtelseTypeDto ytelseType, LocalDate opprettetDato, LocalDate endretDato, StatusDto status, LocalDate gjeldendeFamiliehendelseDato) {
}

