package no.nav.foreldrepenger.fordel.kodeverdi;

import com.fasterxml.jackson.annotation.JsonValue;

public enum MottakKanal implements Kodeverdi {

    // OBS: Bruk getKode (ikke name()) - eller forenkle ned til plain enum dersom ikke skal til frontend.
    SELVBETJENING("NAV_NO"),
    UINNLOGGET("NAV_NO_UINNLOGGET"),
    CHAT("NAV_NO_CHAT"),
    NAV_ANSATT_FOR_BRUKER("INNSENDT_NAV_ANSATT"), // Nav-ansatt var pålogget, fylt ut sammen med bruker
    ALTINN("ALTINN"),
    ALTINN_INNBOKS("ALTINN_INNBOKS"),
    EESSI("EESSI"),
    EIA("EIA"),
    EKSTERN_OPPSLAG("EKST_OPPS"), // Typisk oppholdstillateser fra UDI
    EPOST("E_POST"),
    HELSENETTET("HELSENETTET"),
    SKAN_NETS("SKAN_NETS"), // Ubrukt siden 2020
    SKAN_PEN("SKAN_PEN"), // Scanning pensjon, skal ikke være relevant
    SKAN_IM("SKAN_IM"), // Iron Mountain, ikke inntektsmelding
    HR_SYSTEM_API("HR_SYSTEM_API"), // Inntektsmelding sendt fra HR-system direkte til Nav-API

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
