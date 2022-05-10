package no.nav.foreldrepenger.mottak.journal.saf.model;

import java.util.List;

public record DokumentInfo(String dokumentInfoId,
        String tittel,
        String brevkode,
        List<LogiskVedlegg> logiskeVedlegg,
        List<Dokumentvariant> dokumentvarianter) {
}
