package no.nav.foreldrepenger.fordel.web.app.rest.journalføring;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum FamilihendelseTypeJFDto {
    @JsonProperty("FODSL")
    FØDSEL,
    @JsonProperty("TERM")
    TERMIN,
    @JsonProperty("ADPSJN")
    ADOPSJON,
    @JsonProperty("OMSRGO")
    OMSORG
}
