package no.nav.foreldrepenger.mottak.klient;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum StatusDto {
    @JsonProperty("OPPR")
    OPPRETTET,
    @JsonProperty("UBEH")
    UNDER_BEHANDLING,
    @JsonProperty("LOP")
    LØPENDE,
    @JsonProperty("AVSLU")
    AVSLUTTET
}
