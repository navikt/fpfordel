package no.nav.foreldrepenger.mottak.journal.saf;

import java.util.List;

import no.nav.foreldrepenger.mottak.journal.saf.model.Journalpost;
import no.nav.foreldrepenger.mottak.journal.saf.model.VariantFormat;

public interface SafTjeneste {

    List<Journalpost> hentEksternReferanseId(String dokumentInfoId);

    String hentDokument(String journalpostId, String dokumentInfoId, VariantFormat variantFormat);

    Journalpost hentJournalpostInfo(String journalpostId);

}
