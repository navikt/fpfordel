package no.nav.foreldrepenger.mottak.journal.dokarkiv.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Dokumentvariant(Variantformat variantformat,
        String filtype,
        String fysiskDokument) {
}
