package no.nav.foreldrepenger.mottak.klient;

import no.nav.foreldrepenger.kontrakter.fordel.SaksnummerDto;

import java.time.LocalDate;

public record FagSakInfoDto(SaksnummerDto saksnummer, FagsakYtelseType ytelseType, LocalDate opprettetDato, LocalDate endretDato, FagsakStatus status) {
    public enum FagsakStatus { OPPRETTET, UNDER_BEHANDLING, LOEPENDE, AVSLUTTET }
    public enum FagsakYtelseType { ENGANGSTØNAD, FORELDREPENGER, SVANGERSKAPSPENGER }
}

