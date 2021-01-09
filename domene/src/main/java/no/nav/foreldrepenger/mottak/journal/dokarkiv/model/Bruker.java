package no.nav.foreldrepenger.mottak.journal.dokarkiv.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.foreldrepenger.mottak.journal.saf.model.BrukerIdType;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Bruker(@JsonProperty("id") String id, @JsonProperty("idType") BrukerIdType idType) {

}
