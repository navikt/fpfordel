package no.nav.foreldrepenger.mottak.tjeneste;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import no.nav.foreldrepenger.fordel.kodeverdi.ArkivFilType;
import no.nav.foreldrepenger.fordel.kodeverdi.VariantFormat;
import no.nav.foreldrepenger.mottak.journal.JournalDokument;
import no.nav.foreldrepenger.mottak.journal.JournalMetadata;
import no.nav.foreldrepenger.mottak.journal.JournalTjeneste;

public class HentDataFraJoarkTjenesteTest {
    private static final String XML = "<xml/>";
    private HentDataFraJoarkTjeneste hentDataFraJoarkTjeneste; // objektet vi tester
    private JournalTjeneste mockJournalTjeneste;
    private JournalMetadata mockJournalMetadata;
    private List<JournalMetadata> mockJournalMetadataListe;

    private static final String ARKIV_ID = "ark001";
    private static final ArkivFilType ARKIVFILTYPE_XML = ArkivFilType.XML;
    private static final ArkivFilType ARKIVFILTYPE_PDF = ArkivFilType.PDF;

    @Before
    public void setup() {
        mockJournalTjeneste = mock(JournalTjeneste.class);
        hentDataFraJoarkTjeneste = new HentDataFraJoarkTjeneste(mockJournalTjeneste);
        mockJournalMetadata = mock(JournalMetadata.class);
        mockJournalMetadataListe = Collections.singletonList(mockJournalMetadata);
    }

    @Test
    public void skal_hente_JournalMetadata_for_strukturert_dokument() {

        when(mockJournalMetadata.getArkivFilType()).thenReturn(ARKIVFILTYPE_XML);
        when(mockJournalMetadata.getVariantFormat()).thenReturn(VariantFormat.ORIGINAL);
        when(mockJournalMetadata.getErHoveddokument()).thenReturn(true);
        when(mockJournalTjeneste.hentMetadata(ARKIV_ID)).thenReturn(mockJournalMetadataListe);

        Optional<JournalMetadata> optJournalMetadata = hentDataFraJoarkTjeneste
                .hentHoveddokumentMetadata(ARKIV_ID);

        verify(mockJournalTjeneste).hentMetadata(eq(ARKIV_ID));
        assertThat(optJournalMetadata.isPresent()).isTrue();
        JournalMetadata journalMetadata = optJournalMetadata.get();
        assertThat(journalMetadata).isEqualTo(mockJournalMetadata);
    }

    @Test
    public void skal_hente_JournalMetadata_for_ustrukturert_dokument() {

        when(mockJournalMetadata.getArkivFilType()).thenReturn(ARKIVFILTYPE_PDF);
        when(mockJournalMetadata.getVariantFormat()).thenReturn(VariantFormat.ARKIV);
        when(mockJournalMetadata.getErHoveddokument()).thenReturn(true);
        when(mockJournalTjeneste.hentMetadata(ARKIV_ID)).thenReturn(mockJournalMetadataListe);

        Optional<JournalMetadata> optJournalMetadata = hentDataFraJoarkTjeneste
                .hentHoveddokumentMetadata(ARKIV_ID);

        verify(mockJournalTjeneste).hentMetadata(eq(ARKIV_ID));
        assertThat(optJournalMetadata.isPresent()).isTrue();
        JournalMetadata journalMetadata = optJournalMetadata.get();
        assertThat(journalMetadata).isEqualTo(mockJournalMetadata);
    }

    @Test
    public void skal_hente_JournalMetadata_for_ustrukturert_dokument_med_skanning_xml() {
        List<JournalMetadata> lagUstrukturertDokumentMedSkanningMetaXml = lagUstrukturertDokumentMedSkanningMetaXml();

        when(mockJournalTjeneste.hentMetadata(ARKIV_ID)).thenReturn(lagUstrukturertDokumentMedSkanningMetaXml);

        Optional<JournalMetadata> optJournalMetadata = hentDataFraJoarkTjeneste
                .hentHoveddokumentMetadata(ARKIV_ID);

        verify(mockJournalTjeneste).hentMetadata(eq(ARKIV_ID));
        assertThat(optJournalMetadata).isPresent();
        JournalMetadata journalMetadata = optJournalMetadata.get();
        assertThat(journalMetadata).extracting("arkivFilType").isEqualTo(ArkivFilType.PDF);
    }

    @Test
    public void skal_ikke_hente_JournalMetadata_naar_ikke_hoveddok() {

        when(mockJournalMetadata.getArkivFilType()).thenReturn(ARKIVFILTYPE_XML);
        when(mockJournalMetadata.getErHoveddokument()).thenReturn(false);
        when(mockJournalTjeneste.hentMetadata(ARKIV_ID)).thenReturn(mockJournalMetadataListe);

        Optional<JournalMetadata> optJournalMetadata = hentDataFraJoarkTjeneste
                .hentHoveddokumentMetadata(ARKIV_ID);

        verify(mockJournalTjeneste).hentMetadata(eq(ARKIV_ID));
        assertThat(optJournalMetadata.isPresent()).isFalse();
    }

    @Test
    public void skal_hente_JournalDokument_for_strukturert_dokument() {

        when(mockJournalMetadata.getArkivFilType()).thenReturn(ARKIVFILTYPE_XML);
        when(mockJournalMetadata.getVariantFormat()).thenReturn(VariantFormat.ORIGINAL);
        when(mockJournalTjeneste.hentDokument(mockJournalMetadata))
                .thenReturn(new JournalDokument(mockJournalMetadata, XML));

        Optional<JournalDokument> optJournalDokument = hentDataFraJoarkTjeneste
                .hentStrukturertJournalDokument(mockJournalMetadata);

        assertThat(optJournalDokument).isPresent();
        assertThat(optJournalDokument.get().getXml()).isEqualTo(XML);
        assertThat(optJournalDokument.get().getMetadata()).isEqualTo(mockJournalMetadata);
    }

    @Test
    public void skal_returnere_empty_ved_henting_JournalDokument_for_ustrukturert_dokument() {

        when(mockJournalMetadata.getArkivFilType()).thenReturn(ARKIVFILTYPE_PDF);
        when(mockJournalMetadata.getVariantFormat()).thenReturn(VariantFormat.ARKIV);

        Optional<JournalDokument> optJournalDokument = hentDataFraJoarkTjeneste
                .hentStrukturertJournalDokument(mockJournalMetadata);

        assertThat(optJournalDokument).isNotPresent();
    }

    @Test
    public void er_strukturert_dokument() {
        JournalMetadata strukturertJournalMetadataFullversjon = JournalMetadata.builder()
                .medArkivFilType(ARKIVFILTYPE_XML).medVariantFormat(VariantFormat.FULLVERSJON).build();
        assertThat(HentDataFraJoarkTjeneste.erStrukturertDokument(Arrays.asList(strukturertJournalMetadataFullversjon)))
                .isTrue();
    }

    @Test
    public void er_strukturert_dokument_med_arkiv_pdf() {
        JournalMetadata strukturertJournalMetadataFullversjon = JournalMetadata.builder()
                .medArkivFilType(ARKIVFILTYPE_XML).medVariantFormat(VariantFormat.ORIGINAL).build();
        JournalMetadata arkivPdfMetadata = JournalMetadata.builder().medArkivFilType(ARKIVFILTYPE_PDF)
                .medVariantFormat(VariantFormat.ARKIV).build();
        assertThat(HentDataFraJoarkTjeneste
                .erStrukturertDokument(Arrays.asList(strukturertJournalMetadataFullversjon, arkivPdfMetadata)))
                        .isTrue();
    }

    @Test
    public void er_ustrukturert_dokument() {
        JournalMetadata arkivPdfMetadata = JournalMetadata.builder().medArkivFilType(ARKIVFILTYPE_PDF)
                .medVariantFormat(VariantFormat.ARKIV).build();
        assertThat(HentDataFraJoarkTjeneste.erStrukturertDokument(Arrays.asList(arkivPdfMetadata))).isFalse();
    }

    @Test
    public void er_ustrukturert_dokument_med_skanning_meta_xml() {
        assertThat(HentDataFraJoarkTjeneste.erStrukturertDokument(lagUstrukturertDokumentMedSkanningMetaXml()))
                .isFalse();
    }

    private List<JournalMetadata> lagUstrukturertDokumentMedSkanningMetaXml() {
        JournalMetadata strukturertJournalMetadataArkiv = JournalMetadata.builder()
                .medErHoveddokument(true)
                .medArkivFilType(ARKIVFILTYPE_PDF).medVariantFormat(VariantFormat.ARKIV).build();
        JournalMetadata strukturertJournalMetadataSkanningMeta = JournalMetadata.builder()
                .medErHoveddokument(true)
                .medArkivFilType(ARKIVFILTYPE_XML).medVariantFormat(VariantFormat.SKANNING_META).build();
        return Arrays.asList(strukturertJournalMetadataSkanningMeta, strukturertJournalMetadataArkiv);

    }
}
