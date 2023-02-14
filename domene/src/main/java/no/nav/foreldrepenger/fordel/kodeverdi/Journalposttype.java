package no.nav.foreldrepenger.fordel.kodeverdi;

import java.util.LinkedHashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Journalposttype implements Kodeverdi {

    INNGÅENDE("I"),
    UTGÅENDE("U"),
    NOTAT("N"),

    /**
     * Alle kodeverk må ha en verdi, det kan ikke være null i databasen. Denne koden
     * gjør samme nytten.
     */
    UDEFINERT("-"),
    ;

    private static final Map<String, Journalposttype> KODER = new LinkedHashMap<>();

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    @JsonValue
    private String kode;

    Journalposttype() {
        // Hibernate trenger den
    }

    private Journalposttype(String kode) {
        this.kode = kode;
    }

    public static Journalposttype fraKodeDefaultUdefinert(String kode) {
        if (kode == null) {
            return UDEFINERT;
        }
        return KODER.getOrDefault(kode, UDEFINERT);
    }

    @Override
    public String getKode() {
        return kode;
    }
}
