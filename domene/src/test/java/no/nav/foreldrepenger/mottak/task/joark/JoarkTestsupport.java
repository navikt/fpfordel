package no.nav.foreldrepenger.mottak.task.joark;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import no.nav.foreldrepenger.fordel.kodeverdi.ArkivFilType;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentKategori;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverdi.VariantFormat;
import no.nav.foreldrepenger.mottak.journal.JournalMetadata;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

class JoarkTestsupport {

    static final String ARKIV_ID = "kiv2";
    static final String DOKUMENT_ID = "456";
    static final String BRUKER_FNR = "01234567890";
    static final String AKTØR_ID = "12345";
    static final DokumentKategori DOKUMENT_KATEGORI = DokumentKategori.UDEFINERT;
    static final List<String> brukerListe = Collections.singletonList(BRUKER_FNR);

    final ProsessTaskData taskData;

    JoarkTestsupport() {
        taskData = new ProsessTaskData(HentDataFraJoarkTask.TASKNAME);
        taskData.setSekvens("1");
    }

    JournalMetadata.Builder<DokumentTypeId> lagJournalMetadata(ArkivFilType arkivfilType, List<String> brukerListe,
                                                               VariantFormat variantFormat, DokumentTypeId dokumentTypeId) {
        JournalMetadata.Builder<DokumentTypeId> builder = JournalMetadata.builder();
        builder.medJournalpostId(ARKIV_ID);
        builder.medDokumentId(DOKUMENT_ID);
        builder.medVariantFormat(variantFormat);
        builder.medDokumentType(dokumentTypeId);
        builder.medDokumentKategori(DOKUMENT_KATEGORI);
        builder.medArkivFilType(arkivfilType);
        builder.medErHoveddokument(true);
        builder.medForsendelseMottatt(LocalDate.now());
        builder.medForsendelseMottattTidspunkt(LocalDateTime.now());
        builder.medBrukerIdentListe(brukerListe);
        return builder;
    }

    JournalMetadata<DokumentTypeId> lagJournalMetadataUstrukturert(List<String> brukerListe) {
        return lagJournalMetadata(ArkivFilType.PDF, brukerListe, VariantFormat.ARKIV, DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL).build();
    }

    JournalMetadata<DokumentTypeId> lagJournalMetadataUstrukturert() {
        return lagJournalMetadata(ArkivFilType.PDF, brukerListe, VariantFormat.ARKIV, DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL).build();
    }

    JournalMetadata<DokumentTypeId> lagJournalMetadataUstrukturert(DokumentTypeId dokumentTypeId) {
        return lagJournalMetadata(ArkivFilType.PDF, brukerListe, VariantFormat.ARKIV, dokumentTypeId).build();
    }

    JournalMetadata<DokumentTypeId> lagJournalMetadataStrukturert() {
        return lagJournalMetadata(ArkivFilType.XML, brukerListe, VariantFormat.FULLVERSJON, DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL).build();
    }

    JournalMetadata<DokumentTypeId> lagJournalMetadataStrukturert(DokumentTypeId dokumentTypeId) {
        return lagJournalMetadata(ArkivFilType.XML, brukerListe, VariantFormat.FULLVERSJON, dokumentTypeId).build();
    }

    String readFile(String filename) throws URISyntaxException, IOException {
        Path path = Paths.get(getClass().getClassLoader().getResource(filename).toURI());
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
