package no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto;

import java.util.Arrays;

public enum ForsendelseStatus {
    PENDING("Pending"),
    GOSYS("Gosys"),
    FPSAK("Fpsak");

    private final String value;

    ForsendelseStatus(String value) {
        this.value = value;
    }

    /**
     * @return the Enum representation for the given string.
     */
    public static ForsendelseStatus asEnumValue(String s) {
        return Arrays.stream(values())
                .filter(v -> v.value.equalsIgnoreCase(s))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("ugyldig verdi: " + s));
    }
}
