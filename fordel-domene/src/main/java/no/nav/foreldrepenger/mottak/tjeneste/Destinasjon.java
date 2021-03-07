package no.nav.foreldrepenger.mottak.tjeneste;

import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseStatus;

public record Destinasjon(ForsendelseStatus system, String saksnummer) {
}
