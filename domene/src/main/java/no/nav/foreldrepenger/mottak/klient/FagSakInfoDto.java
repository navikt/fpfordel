package no.nav.foreldrepenger.mottak.klient;

import no.nav.foreldrepenger.kontrakter.fordel.SaksnummerDto;

import java.time.LocalDate;

public record FagSakInfoDto(SaksnummerDto saksnummer,
                            YtelseTypeDto ytelseType,
                            LocalDate opprettetDato,
                            LocalDate endretDato,
                            StatusDto status) {}

