package no.nav.foreldrepenger.mottak.journal.saf.model;

import java.time.LocalDateTime;
import java.util.List;

import no.nav.foreldrepenger.mottak.journal.dokarkiv.model.Tilleggsopplysning;

public record Journalpost(String journalpostId,
        String journalposttype,
        String journalstatus,
        LocalDateTime datoOpprettet,
        String tittel,
        String kanal,
        String tema,
        String behandlingstema,
        String journalfoerendeEnhet,
        String eksternReferanseId,
        Bruker bruker,
        AvsenderMottaker avsenderMottaker,
        Sak sak,
        List<Tilleggsopplysning> tilleggsopplysninger,
        List<DokumentInfo> dokumenter) {
}
