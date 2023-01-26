package no.nav.foreldrepenger.fordel.kodeverdi;

import com.fasterxml.jackson.annotation.JsonValue;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

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
