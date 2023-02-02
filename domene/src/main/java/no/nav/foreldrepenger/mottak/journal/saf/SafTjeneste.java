package no.nav.foreldrepenger.mottak.journal.saf;

import java.util.List;

import no.nav.vedtak.felles.integrasjon.dokarkiv.dto.Dokumentvariant;

public interface SafTjeneste {

    List<Journalpost> hentEksternReferanseId(String dokumentInfoId);

    String hentDokument(String journalpostId, String dokumentInfoId, Dokumentvariant.Variantformat variantFormat);
    byte[] hentDokumentByteArray(String journalpostId, String dokumentInfoId, Dokumentvariant.Variantformat variantFormat);

    Journalpost hentJournalpostInfo(String journalpostId);

}
