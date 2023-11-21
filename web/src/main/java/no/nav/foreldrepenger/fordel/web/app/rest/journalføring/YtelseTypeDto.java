package no.nav.foreldrepenger.fordel.web.app.rest.journalføring;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum YtelseTypeDto {
    @JsonProperty("ES")
    ENGANGSTØNAD,
    @JsonProperty("FP")
    FORELDREPENGER,
    @JsonProperty("SVP")
    SVANGERSKAPSPENGER,
}
