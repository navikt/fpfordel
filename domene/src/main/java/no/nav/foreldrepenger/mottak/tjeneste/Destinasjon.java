package no.nav.foreldrepenger.mottak.tjeneste;

import static no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseStatus.FPSAK;

import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseStatus;

public record Destinasjon(ForsendelseStatus system,
                          String saksnummer) {

    public static Destinasjon GOSYS = new Destinasjon(ForsendelseStatus.GOSYS, null);
    public static Destinasjon FPSAK_UTEN_SAK = new Destinasjon(FPSAK, null);
}
