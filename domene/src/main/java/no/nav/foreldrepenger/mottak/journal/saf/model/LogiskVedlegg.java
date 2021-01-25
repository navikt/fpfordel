package no.nav.foreldrepenger.mottak.journal.saf.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LogiskVedlegg(@JsonProperty("filnavn") String tittel) {

}
