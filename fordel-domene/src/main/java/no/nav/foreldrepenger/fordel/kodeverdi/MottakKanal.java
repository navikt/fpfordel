package no.nav.foreldrepenger.fordel.kodeverdi;

import com.fasterxml.jackson.annotation.JsonValue;

public enum MottakKanal implements Kodeverdi {

    SELVBETJENING("NAV_NO"),
    ALTINN("ALTINN"),
    EESSI("EESSI"),
    EIA("EIA"),
    HELSENETTET("HELSENETTET"),
    SKAN_NETS("SKAN_NETS"),

    /**
     * Alle kodeverk må ha en verdi, det kan ikke være null i databasen. Denne koden
     * gjør samme nytten.
     */
    UDEFINERT("-");

    @JsonValue
    private String kode;

    MottakKanal() {
        // Hibernate trenger den
    }

    private MottakKanal(String kode) {
        this.kode = kode;
    }

    @Override
    public String getKode() {
        return kode;
    }


}
