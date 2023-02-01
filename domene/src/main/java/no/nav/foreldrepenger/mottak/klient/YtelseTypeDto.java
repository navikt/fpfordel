package no.nav.foreldrepenger.mottak.klient;

import com.fasterxml.jackson.annotation.JsonValue;

public enum YtelseTypeDto {

    ENGANGSTØNAD("ES"),
    FORELDREPENGER("FP"),
    SVANGERSKAPSPENGER("SVP"),
    ;

    @JsonValue
    private String kode;

    YtelseTypeDto(String kode) {
        this.kode = kode;
    }

    public String getKode() {
        return kode;
    }

}
