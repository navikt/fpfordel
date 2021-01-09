package no.nav.foreldrepenger.mottak.journal.dokarkiv.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpprettJournalpostResponse(String journalpostId,
        boolean journalpostferdigstilt,
        List<DokumentInfoResponse> dokumenter) {
}