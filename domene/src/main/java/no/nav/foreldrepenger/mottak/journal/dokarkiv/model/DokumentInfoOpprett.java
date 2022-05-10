package no.nav.foreldrepenger.mottak.journal.dokarkiv.model;

import java.util.List;

public record DokumentInfoOpprett(String tittel,
        String brevkode,
        String dokumentKategori,
        List<Dokumentvariant> dokumentvarianter) {
}
