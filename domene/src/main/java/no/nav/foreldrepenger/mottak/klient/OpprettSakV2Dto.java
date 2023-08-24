package no.nav.foreldrepenger.mottak.klient;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

public record OpprettSakV2Dto(@Digits(integer = 18, fraction = 0) String journalpostId, @NotNull @Valid YtelseTypeDto ytelseType,
                              @NotNull @Digits(integer = 19, fraction = 0) String aktørId) {
}
