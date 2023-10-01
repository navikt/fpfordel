package no.nav.foreldrepenger.fordel.kodeverdi;

import com.fasterxml.jackson.annotation.JsonValue;

public enum MottakKanal implements Kodeverdi {

    // OBS: Bruk getKode (ikke name()) - eller forenkle ned til plain enum dersom ikke skal til frontend.
    SELVBETJENING("NAV_NO"),
    ALTINN("ALTINN"),
    EESSI("EESSI"),
    EIA("EIA"),
    HELSENETTET("HELSENETTET"),
    SKAN_NETS("SKAN_NETS"), // Ubrukt siden 2020
    SKAN_IM("SKAN_IM"), // Iron Mountain, ikke inntektsmelding

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
