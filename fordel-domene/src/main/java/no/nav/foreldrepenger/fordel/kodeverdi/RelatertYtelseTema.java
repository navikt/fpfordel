package no.nav.foreldrepenger.fordel.kodeverdi;

import com.fasterxml.jackson.annotation.JsonValue;

public enum RelatertYtelseTema implements Kodeverdi {

    FORELDREPENGER_TEMA("FA"),
    SYKEPENGER_TEMA("SP"),
    PÅRØRENDE_TEMA("BS"),

    /**
     * Alle kodeverk må ha en verdi, det kan ikke være null i databasen. Denne koden
     * gjør samme nytten.
     */
    UDEFINERT("-"),
    ;

    @JsonValue
    private String kode;

    RelatertYtelseTema() {
        // Hibernate trenger den
    }

    private RelatertYtelseTema(String kode) {
        this.kode = kode;
    }

    @Override
    public String getKode() {
        return kode;
    }

}
