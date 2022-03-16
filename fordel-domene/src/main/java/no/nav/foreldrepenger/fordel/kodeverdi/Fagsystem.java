package no.nav.foreldrepenger.fordel.kodeverdi;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Fagsystem implements Kodeverdi {

    FPSAK("FS36"),
    TPS("FS03"),
    JOARK("AS36"),
    INFOTRYGD("IT01"),
    ARENA("AO01"),
    INNTEKT("FS28"),
    MEDL("FS18"),
    GOSYS("FS22"),
    ENHETSREGISTERET("ER01"),
    AAREGISTERET("AR01"),
    MELOSYS("FS38"),
    UTBETALINGSMELDING("OB36"),
    GRISEN("AO11"),
    HJE_HEL_ORT("OEBS"),
    PESYS("PP01"),
    VENTELONN("V2"),
    UNNTAK("UFM"),

    /**
     * Alle kodeverk må ha en verdi, det kan ikke være null i databasen. Denne koden
     * gjør samme nytten.
     */
    UDEFINERT("-"),
    ;

    @JsonValue
    private String kode;

    Fagsystem() {
        // Hibernate trenger den
    }

    private Fagsystem(String kode) {
        this.kode = kode;
    }


    @Override
    public String getKode() {
        return kode;
    }

}
