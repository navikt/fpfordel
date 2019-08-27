package no.nav.foreldrepenger.mottak.journal;

import java.util.List;

import no.nav.foreldrepenger.fordel.kodeverk.DokumentTypeId;
import no.nav.foreldrepenger.mottak.journal.dokumentforsendelse.DokumentforsendelseRequest;
import no.nav.foreldrepenger.mottak.journal.dokumentforsendelse.DokumentforsendelseResponse;


public interface JournalTjeneste {

    <T extends DokumentTypeId> JournalDokument<T> hentDokument(JournalMetadata<T> journalMetadata);
    List<JournalMetadata<DokumentTypeId>> hentMetadata(String journalpostId);
    JournalPostMangler utledJournalføringsbehov(String journalpostId);
    void ferdigstillJournalføring(String journalpostId, String enhetId);
    void oppdaterJournalpost(JournalPost journalPost);
    DokumentforsendelseResponse journalførDokumentforsendelse(DokumentforsendelseRequest dokumentforsendelseRequest);
}
