package no.nav.foreldrepenger.mottak.klient;

import com.fasterxml.jackson.annotation.JsonValue;

public enum FagsakYtelseType {

    ENGANGSTØNAD("ES"),
    FORELDREPENGER("FP"),
    SVANGERSKAPSPENGER("SVP"),
    ;

    @JsonValue
    private String kode;

    FagsakYtelseType(String kode) {
        this.kode = kode;
    }

    public String getKode() {
        return kode;
    }

}
