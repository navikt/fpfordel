package no.nav.foreldrepenger.fordel.web.app.rest;

import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.ws.rs.core.HttpHeaders.CONTENT_DISPOSITION;
import static javax.ws.rs.core.HttpHeaders.LOCATION;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_XML_TYPE;
import static no.nav.foreldrepenger.fordel.web.app.rest.DokumentforsendelseRestTjeneste.APPLICATION_PDF_TYPE;
import static no.nav.foreldrepenger.fordel.web.app.rest.DokumentforsendelseRestTjeneste.IMAGE_JPG_TYPE;
import static no.nav.foreldrepenger.fordel.web.app.rest.DokumentforsendelseRestTjeneste.IMAGE_PNG_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.UUID;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.internal.util.collection.StringKeyIgnoreCaseMultivaluedMap;
import org.glassfish.jersey.media.multipart.BodyPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.DokumentforsendelseTjeneste;
import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseIdDto;
import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseStatus;
import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseStatusDto;
import no.nav.vedtak.exception.TekniskException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DokumentforsendelseRestTjenesteTest {
    static {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Oslo"));
    }

    @Mock
    private DokumentforsendelseTjeneste dokumentTjeneste;
    private DokumentforsendelseRestTjeneste tjeneste;
    private BodyPart metadataPart;
    private BodyPart hoveddokumentPart;
    private BodyPart hoveddokumentPartPdf;
    private BodyPart vedleggPartPdf;
    private BodyPart vedleggPartJpg;
    private BodyPart vedleggPartPng;
    @Mock
    private MultiPart input;

    private static BodyPart mockBasicInputPart(Optional<String> contentId, String contentDispositionName) {
        var part = mock(BodyPart.class);
        MultivaluedMap<String, String> map = new StringKeyIgnoreCaseMultivaluedMap<>();
        map.put("Content-Disposition", List.of("attachment; name=\"" + contentDispositionName + "\"; filename=\"" + "Farskap\""));
        contentId.ifPresent(id -> map.put("Content-ID", List.of(id)));
        when(part.getHeaders()).thenReturn(map);
        return part;
    }

    private static BodyPart mockHoveddokumentPartPdf() {
        var part = mockBasicInputPart(Optional.of("<some ID 2>"), "hoveddokument");
        when(part.getMediaType()).thenReturn(APPLICATION_PDF_TYPE);
        when(part.getEntityAs(String.class)).thenReturn("");
        when(part.getEntityAs(byte[].class)).thenReturn("body".getBytes(UTF_8));
        return part;
    }

    private static BodyPart mockVedleggPartPDF(String contentId) {
        var part = mockBasicInputPart(Optional.of(contentId), "vedlegg");
        when(part.getMediaType()).thenReturn(APPLICATION_PDF_TYPE);
        when(part.getEntityAs(String.class)).thenReturn("");
        when(part.getEntityAs(byte[].class)).thenReturn("body".getBytes(UTF_8));
        return part;
    }

    private static BodyPart mockVedleggPartJpg(String contentId) {
        var part = mockBasicInputPart(Optional.of(contentId), "vedlegg");
        when(part.getMediaType()).thenReturn(IMAGE_JPG_TYPE);
        when(part.getEntityAs(String.class)).thenReturn("");
        when(part.getEntityAs(byte[].class)).thenReturn("body".getBytes(UTF_8));
        return part;
    }

    private static BodyPart mockVedleggPartPng(String contentId) {
        var part = mockBasicInputPart(Optional.of(contentId), "vedlegg");
        when(part.getMediaType()).thenReturn(IMAGE_PNG_TYPE);
        when(part.getEntityAs(String.class)).thenReturn("");
        when(part.getEntityAs(byte[].class)).thenReturn("body".getBytes(UTF_8));
        return part;
    }

    @BeforeEach
    void setUp() throws Exception {
        tjeneste = new DokumentforsendelseRestTjeneste(dokumentTjeneste);
        metadataPart = mockMetadataPart();
        hoveddokumentPart = mockHoveddokumentPartXml();
        hoveddokumentPartPdf = mockHoveddokumentPartPdf();
        vedleggPartPdf = mockVedleggPartPDF("<some ID 3>");
        vedleggPartJpg = mockVedleggPartJpg("<some ID 4>");
        vedleggPartPng = mockVedleggPartPng("<some ID 5>");
        when(input.getBodyParts()).thenReturn(List.of(metadataPart, hoveddokumentPart, hoveddokumentPartPdf, vedleggPartPdf, vedleggPartJpg, vedleggPartPng));
    }

    @Test
    void input_skal_kaste_exception_når_inputpart_ikke_har_minst_2_parts() {
        when(input.getBodyParts()).thenReturn(new ArrayList<>());
        var forsendelseStatusDto = new ForsendelseStatusDto(ForsendelseStatus.PENDING);
        when(dokumentTjeneste.finnStatusinformasjon(any(UUID.class))).thenReturn(forsendelseStatusDto);
        when(dokumentTjeneste.finnStatusinformasjonHvisEksisterer(any(UUID.class))).thenReturn(Optional.of(forsendelseStatusDto));

        assertThatThrownBy(() -> tjeneste.uploadFile(input)).isInstanceOf(IllegalArgumentException.class).hasMessage("Må ha minst to deler,fikk 0");
    }

    @Test
    void skal_kaste_teknisk_exception_hvis_metadata_ikke_er_første_part() {
        MultivaluedMap<String, String> map = new StringKeyIgnoreCaseMultivaluedMap<>();
        map.put(CONTENT_DISPOSITION, List.of("ikke_metadata"));
        when(metadataPart.getHeaders()).thenReturn(map);
        assertThatThrownBy(() -> tjeneste.uploadFile(input)).isInstanceOf(TekniskException.class)
            .hasMessage("FP-892453:The first part must be the metadata part");
    }

    @Test
    void skal_kaste_teknisk_exception_hvis_metadata_ikke_er_json() {
        when(metadataPart.getMediaType()).thenReturn(APPLICATION_XML_TYPE);
        assertThatThrownBy(() -> tjeneste.uploadFile(input)).isInstanceOf(TekniskException.class)
            .hasMessage("FP-892454:The metadata part should be application/json");
    }

    @Test
    void skal_kaste_teknisk_exception_hvis_man_ikke_kan_hente_body_for_metadata() throws Exception {
        when(metadataPart.getEntityAs(String.class)).thenThrow(ProcessingException.class);
        assertThatThrownBy(() -> tjeneste.uploadFile(input)).isInstanceOf(TekniskException.class)
            .hasMessageContaining("FP-892466:Klarte ikke å lese inn dokumentet");
    }

    @Test
    void skal_kaste_teknisk_exception_hvis_metadata_har_flere_filer_enn_lastet_opp() {
        when(input.getBodyParts()).thenReturn(List.of(metadataPart, hoveddokumentPart, hoveddokumentPartPdf));
        assertThatThrownBy(() -> tjeneste.uploadFile(input)).isInstanceOf(TekniskException.class)
            .hasMessageContaining("FP-892456:Metadata inneholder flere filer enn det som er lastet opp");
    }

    @Test
    void skal_kaste_teknisk_exception_hvis_metadata_har_færre_filer_enn_lastet_opp() throws Exception {
        String contentId = "<some ID 4>";
        var inputParts = List.of(metadataPart, hoveddokumentPart, hoveddokumentPartPdf, vedleggPartPdf, vedleggPartJpg, vedleggPartPng, mockVedleggPartPDF(contentId));
        when(input.getBodyParts()).thenReturn(inputParts);

        assertThatThrownBy(() -> tjeneste.uploadFile(input)).isInstanceOf(TekniskException.class)
            .hasMessageContaining("FP-892446:")
            .hasMessageContaining(contentId);
    }

    @Test
    void skal_kaste_teknisk_exception_hvis_hoveddokument_mangler_name() {
        MultivaluedMap<String, String> map = new StringKeyIgnoreCaseMultivaluedMap<>();
        map.put("Content-Disposition", List.of("mangler ; foo=name"));
        when(hoveddokumentPart.getHeaders()).thenReturn(map);

        assertThatThrownBy(() -> tjeneste.uploadFile(input)).isInstanceOf(TekniskException.class).hasMessageContaining("FP-892457:Unknown part name");
    }

    @Test
    void skal_kaste_teknisk_exception_hvis_vedlegg_ikke_er_mediatype_pdf_jpg_png() {
        when(vedleggPartPdf.getMediaType()).thenReturn(APPLICATION_XML_TYPE);
        assertThatThrownBy(() -> tjeneste.uploadFile(input)).isInstanceOf(TekniskException.class)
            .hasMessageContaining("FP-882558:Vedlegg er hverken pdf, png eller jpeg, Content-ID=<some ID 3>");
    }

    @Test
    void skal_lagre_dokumentene() {
        var forsendelseStatusDto = new ForsendelseStatusDto(ForsendelseStatus.PENDING);
        when(dokumentTjeneste.finnStatusinformasjonHvisEksisterer(any(UUID.class))).thenReturn(Optional.of(forsendelseStatusDto));
        var response = tjeneste.uploadFile(input);
        assertThat(response.getStatus()).isEqualTo(Response.Status.ACCEPTED.getStatusCode());
        assertThat(response.getHeaderString(LOCATION)).contains("/dokumentforsendelse/status?forsendelseId=48f6e1cf-c5d8-4355-8e8c-b75494703959");
    }

    @Test
    void skal_returnere_see_other_redirect_når_status_fpsak() {
        var status = new ForsendelseStatusDto(ForsendelseStatus.FPSAK);
        when(dokumentTjeneste.finnStatusinformasjon(any(UUID.class))).thenReturn(status);
        var response = tjeneste.uploadFile(input);
        assertThat(response.getStatus()).isEqualTo(HttpURLConnection.HTTP_OK);
    }

    @Test
    void skal_returnere_status_ok_når_status_gosys() {
        var status = new ForsendelseStatusDto(ForsendelseStatus.GOSYS);
        when(dokumentTjeneste.finnStatusinformasjon(any(UUID.class))).thenReturn(status);
        var response = tjeneste.uploadFile(input);
        assertThat(response.getStatus()).isEqualTo(HttpURLConnection.HTTP_OK);
        assertThat(response.getEntity()).isEqualTo(status);
    }

    @Test
    void skal_håndtere_duplikate_kall() {
        var status = new ForsendelseStatusDto(ForsendelseStatus.FPSAK);
        when(dokumentTjeneste.finnStatusinformasjon(any(UUID.class))).thenReturn(status);
        var response1 = tjeneste.uploadFile(input);
        when(dokumentTjeneste.finnStatusinformasjonHvisEksisterer(any(UUID.class))).thenReturn(Optional.of(status));
        var response2 = tjeneste.uploadFile(input);

        assertThat(response1.getStatus()).isEqualTo(HttpURLConnection.HTTP_OK);
        assertThat(response1.getEntity()).isEqualTo(status);

        assertThat(response2.getStatus()).isEqualTo(HttpURLConnection.HTTP_OK);
        assertThat(response2.getEntity()).isEqualTo(status);
    }

    @Test
    void skal_kaste_valideringsfeil_hvis_ugyldig_uuid() {
        assertThrows(IllegalArgumentException.class, () -> new ForsendelseIdDto("1234"));
    }

    @Test
    void finnStatusInformasjon_skal_returnere_status_ok_når_status_ikke_er_fpsak() {
        var idDto = new ForsendelseIdDto(UUID.randomUUID().toString());
        var statusDto = new ForsendelseStatusDto(ForsendelseStatus.PENDING);
        when(dokumentTjeneste.finnStatusinformasjon(any(UUID.class))).thenReturn(statusDto);
        var response = tjeneste.finnStatusinformasjon(idDto);
        assertThat(response.getStatus()).isEqualTo(HttpURLConnection.HTTP_OK);
        assertThat(response.getEntity()).isEqualTo(statusDto);
    }

    @Test
    void finnStatusInformasjon_skal_returnere_status_see_other_når_status_er_fpsak() {
        var idDto = new ForsendelseIdDto(UUID.randomUUID().toString());
        var statusDto = new ForsendelseStatusDto(ForsendelseStatus.FPSAK);
        when(dokumentTjeneste.finnStatusinformasjon(any(UUID.class))).thenReturn(statusDto);
        var response = tjeneste.finnStatusinformasjon(idDto);
        assertThat(response.getStatus()).isEqualTo(HttpURLConnection.HTTP_OK);
        assertThat(response.getEntity()).isEqualTo(statusDto);
    }

    @Test
    void skal_populerer_AbacDataAttributter_med_aktør() {
        assertThat(new DokumentforsendelseRestTjeneste.AbacDataSupplier().apply(input).toString()).contains("AKTØR_ID=[MASKERT#1]");
    }

    private BodyPart mockMetadataPart() throws Exception {
        var part = mockBasicInputPart(Optional.empty(), "metadata");
        when(part.getMediaType()).thenReturn(APPLICATION_JSON_TYPE);
        when(part.getEntityAs(String.class)).thenReturn(byggMetadataString());
        return part;
    }

    private BodyPart mockHoveddokumentPartXml() throws Exception {
        var part = mockBasicInputPart(Optional.of("<some ID 1>"), "hoveddokument");
        when(part.getMediaType()).thenReturn(APPLICATION_XML_TYPE);
        when(part.getEntityAs(String.class)).thenReturn(lesInnSøknad());
        return part;
    }

    private String lesInnSøknad() throws Exception {
        URL resource = this.getClass().getClassLoader().getResource("testdata/selvb-soeknad-forp.xml");
        Path path = Paths.get(resource.toURI());
        return new String(Files.readAllBytes(path));
    }

    private String byggMetadataString() throws Exception {
        URL resource = this.getClass().getClassLoader().getResource("testdata/metadata.json");
        Path path = Paths.get(resource.toURI());
        return new String(Files.readAllBytes(path));
    }

}
