package no.nav.foreldrepenger.mottak.journal.saf;

import java.time.LocalDateTime;
import java.util.List;

import no.nav.vedtak.felles.integrasjon.dokarkiv.dto.AvsenderMottaker;
import no.nav.vedtak.felles.integrasjon.dokarkiv.dto.Bruker;
import no.nav.vedtak.felles.integrasjon.dokarkiv.dto.Sak;
import no.nav.vedtak.felles.integrasjon.dokarkiv.dto.Tilleggsopplysning;


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
