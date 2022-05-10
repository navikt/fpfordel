package no.nav.foreldrepenger.mottak.journal.dokarkiv.model;

import java.util.List;

import no.nav.foreldrepenger.mottak.journal.saf.model.Dokumentvariant;
import no.nav.foreldrepenger.mottak.journal.saf.model.LogiskVedlegg;

public record DokumentInfo(String dokumentInfoId,
        String tittel,
        String brevkode,
        List<LogiskVedlegg> logiskeVedlegg,
        List<Dokumentvariant> dokumentvarianter) {

}
