package no.nav.foreldrepenger.mottak.journal.dokarkiv.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import no.nav.foreldrepenger.mottak.journal.saf.model.Dokumentvariant;
import no.nav.foreldrepenger.mottak.journal.saf.model.LogiskVedlegg;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DokumentInfo(String dokumentInfoId,
        String tittel,
        String brevkode,
        List<LogiskVedlegg> logiskeVedlegg,
        List<Dokumentvariant> dokumentvarianter) {

}
