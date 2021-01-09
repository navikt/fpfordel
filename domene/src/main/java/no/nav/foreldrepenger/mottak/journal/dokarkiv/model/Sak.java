package no.nav.foreldrepenger.mottak.journal.dokarkiv.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Sak(String fagsakId,
        String fagsaksystem,
        String sakstype,
        String arkivsaksnummer,
        String arkivsaksystem) {

}
