package no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto;

import java.util.Arrays;

public enum ForsendelseStatus {
    PENDING("Pending"), //$NON-NLS-1$
    GOSYS("Gosys"), //$NON-NLS-1$
    FPSAK("Fpsak"); //$NON-NLS-1$

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
                .orElseThrow(() -> new IllegalArgumentException("ugyldig verdi: " + s)); //$NON-NLS-1$
    }

}
