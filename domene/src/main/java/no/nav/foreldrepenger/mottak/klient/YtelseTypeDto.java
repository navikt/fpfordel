package no.nav.foreldrepenger.mottak.klient;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum YtelseTypeDto {
    @JsonProperty("ES")
    ENGANGSTØNAD,
    @JsonProperty("FP")
    FORELDREPENGER,
    @JsonProperty("SVP")
    SVANGERSKAPSPENGER,
}
