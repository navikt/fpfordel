package no.nav.foreldrepenger.mottak.klient;

import no.nav.foreldrepenger.kontrakter.fordel.SaksnummerDto;

import java.time.LocalDate;

public record FagsakJournalFøringDto(SaksnummerDto saksnummer, String ytelseNavn, LocalDate opprettetDato, LocalDate endretDato, String status) {
}
