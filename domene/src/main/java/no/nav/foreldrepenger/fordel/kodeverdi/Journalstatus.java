package no.nav.foreldrepenger.fordel.kodeverdi;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.LinkedHashMap;
import java.util.Map;

public enum Journalstatus implements Kodeverdi {

    MOTTATT("MOTTATT"),
    JOURNALFOERT("JOURNALFOERT"),
    FEILREGISTRERT("FEILREGISTRERT"),
    UTGAAR("UTGAAR"),
    UKJENT("UKJENT"),

    /**
     * Alle kodeverk må ha en verdi, det kan ikke være null i databasen. Denne koden
     * gjør samme nytten.
     */
    UDEFINERT("-"),
    ;

    private static final Map<String, Journalstatus> KODER = new LinkedHashMap<>();

    static {
        for (var v : values()) {
            if (KODER.putIfAbsent(v.kode, v) != null) {
                throw new IllegalArgumentException("Duplikat : " + v.kode);
            }
        }
    }

    @JsonValue
    private String kode;

    Journalstatus() {
        // Hibernate trenger den
    }


    private Journalstatus(String kode) {
        this.kode = kode;
    }

    public static Journalstatus fraKodeDefaultUdefinert(String kode) {
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
