package no.nav.foreldrepenger.fordel.kodeverdi;

import com.fasterxml.jackson.annotation.JsonValue;

public enum YtelseType {

    ENGANGSTØNAD("ES"),
    FORELDREPENGER("FP"),
    SVANGERSKAPSPENGER("SVP"),
    UDEFINERT("-"),
    ;

    @JsonValue
    private String kode;

    YtelseType(String kode) {
        this.kode = kode;
    }

    public String getKode() {
        return kode;
    }
}
