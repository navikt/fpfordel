package no.nav.foreldrepenger.mottak.journal.dokarkiv;

import no.nav.foreldrepenger.mottak.journal.dokarkiv.model.OppdaterJournalpostRequest;
import no.nav.foreldrepenger.mottak.journal.dokarkiv.model.OpprettJournalpostRequest;
import no.nav.foreldrepenger.mottak.journal.dokarkiv.model.OpprettJournalpostResponse;

public interface DokArkiv {

    OpprettJournalpostResponse opprettJournalpost(OpprettJournalpostRequest request, boolean ferdigstill);

    boolean ferdigstillJournalpost(String journalpostId, String enhet);

    boolean oppdaterJournalpost(String journalpostId, OppdaterJournalpostRequest request);

}
