package no.nav.foreldrepenger.mottak.klient;

import com.fasterxml.jackson.annotation.JsonValue;

public enum StatusDto {

    OPPRETTET("OPPR"),
    UNDER_BEHANDLING("UBEH"),
    LØPENDE("LOP"),
    AVSLUTTET("AVSLU"),
    ;

    @JsonValue
    private String kode;

    StatusDto(String kode) {
        this.kode = kode;
    }

    public String getKode() {
        return kode;
    }
}
