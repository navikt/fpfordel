package no.nav.foreldrepenger.mottak.journal.dokarkiv.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DokumentInfoOppdater(String dokumentInfoId,
        String tittel,
        String brevkode) {
}
