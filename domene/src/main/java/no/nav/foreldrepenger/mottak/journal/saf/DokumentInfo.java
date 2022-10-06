package no.nav.foreldrepenger.mottak.journal.saf;

import java.util.List;

import no.nav.vedtak.felles.integrasjon.dokarkiv.dto.Dokumentvariant;

public record DokumentInfo(String dokumentInfoId,
        String tittel,
        String brevkode,
        List<LogiskVedlegg> logiskeVedlegg,
        List<Dokumentvariant> dokumentvarianter) {

    public record LogiskVedlegg(String tittel) {
    }
}
