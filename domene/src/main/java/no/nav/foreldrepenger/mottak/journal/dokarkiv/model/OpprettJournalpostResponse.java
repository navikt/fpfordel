package no.nav.foreldrepenger.mottak.journal.dokarkiv.model;

import java.util.List;

public record OpprettJournalpostResponse(String journalpostId,
        boolean journalpostferdigstilt,
        List<DokumentInfoResponse> dokumenter) {
}