package no.nav.foreldrepenger.mottak.klient;

import javax.validation.Valid;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;

public record OpprettSakV2Dto(@Digits(integer = 18, fraction = 0) String journalpostId, @NotNull @Valid YtelseTypeDto ytelseType,
                              @NotNull @Digits(integer = 19, fraction = 0) String aktørId) {
}
