package no.nav.foreldrepenger.mottak.klient;

import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;

public record AktørIdDto(@NotNull @Digits(integer = 19, fraction = 0) String aktørId) {
    @Override
    public String toString() {
        return getClass().getSimpleName() + "<" + maskerAktørId() + ">";
    }

    private String maskerAktørId() {
        if (aktørId == null) {
            return "";
        }
        var length = aktørId.length();
        if (length <= 4) {
            return "*".repeat(length);
        }
        return "*".repeat(length - 4) + aktørId.substring(length - 4);
    }
}
