package no.nav.foreldrepenger.mottak.infotrygd.rest;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum SakType {
    @JsonEnumDefaultValue
    UKJENT,
    S,
    R,
    K,
    A
}
