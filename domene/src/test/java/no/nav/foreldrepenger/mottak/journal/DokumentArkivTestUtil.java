package no.nav.foreldrepenger.mottak.journal;

import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import no.nav.foreldrepenger.fordel.kodeverdi.ArkivFilType;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.mottak.domene.dokument.Dokument;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentMetadata;

public class DokumentArkivTestUtil {

    public static final String BRUKER_ID = "1234";
    public static final String JOURNALPOST_ID = "234567";
    public static final LocalDateTime FORSENDELSE_MOTTATT = LocalDateTime.now();
    public static final byte[] BLOB = "Bare litt testing".getBytes(Charset.forName("UTF-8"));

    public static DokumentMetadata lagMetadata(UUID forsendelseId, String saksnummer) {
        return DokumentMetadata.builder()
                .setForsendelseMottatt(FORSENDELSE_MOTTATT)
                .setBrukerId(BRUKER_ID)
                .setForsendelseId(forsendelseId)
                .setSaksnummer(saksnummer)
                .build();
    }

    public static Dokument lagDokument(UUID forsendelseId, DokumentTypeId dokumentTypeId, ArkivFilType arkivFilType, boolean erHoveddokument) {
        return Dokument.builder()
                .setForsendelseId(forsendelseId)
                .setDokumentInnhold(BLOB, arkivFilType)
                .setHovedDokument(erHoveddokument)
                .setDokumentTypeId(dokumentTypeId)
                .build();
    }

    public static Dokument lagDokumentBeskrivelse(UUID forsendelseId, DokumentTypeId dokumentTypeId, ArkivFilType arkivFilType, boolean erHoveddokument, String beskrivelse) {
        return Dokument.builder()
                .setForsendelseId(forsendelseId)
                .setDokumentInnhold(BLOB, arkivFilType)
                .setHovedDokument(erHoveddokument)
                .setDokumentTypeId(dokumentTypeId)
                .setBeskrivelse(beskrivelse)
                .build();
    }

    public static List<Dokument> lagHoveddokumentMedXmlOgPdf(UUID forsendelseId, DokumentTypeId dokumentTypeId) {
        List<Dokument> dokumenter = new ArrayList<>();
        dokumenter.add(lagDokument(forsendelseId, dokumentTypeId, ArkivFilType.XML, true));
        dokumenter.add(lagDokument(forsendelseId, dokumentTypeId, ArkivFilType.PDFA, true));
        return dokumenter;
    }

    public static OpprettetJournalpost lagOpprettRespons(Boolean endelig) {
        return new OpprettetJournalpost(JOURNALPOST_ID, endelig);
    }
}
