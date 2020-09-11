package no.nav.foreldrepenger.fordel.web.app.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.UUID;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.specimpl.MultivaluedMapImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.net.HttpHeaders;

import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.DokumentforsendelseTjeneste;
import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseIdDto;
import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseStatus;
import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseStatusDto;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;

public class DokumentforsendelseRestTjenesteTest {
    static {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Oslo"));
    }

    private DokumentforsendelseTjeneste dokumentTjenesteMock;
    private DokumentforsendelseRestTjeneste tjeneste;
    private InputPart metadataPart;
    private InputPart hoveddokumentPart;
    private InputPart hoveddokumentPartPdf;
    private InputPart vedleggPart;
    private MultipartInput input;

    @BeforeEach
    public void setUp() throws Exception {
        dokumentTjenesteMock = mock(DokumentforsendelseTjeneste.class);
        when(dokumentTjenesteMock.finnStatusinformasjon(any(UUID.class))).thenReturn(new ForsendelseStatusDto(ForsendelseStatus.PENDING));
        tjeneste = new DokumentforsendelseRestTjeneste(dokumentTjenesteMock, null);

        // default mocking
        input = mock(MultipartInput.class);
        metadataPart = mockMetadataPart();
        hoveddokumentPart = mockHoveddokumentPartXml();
        hoveddokumentPartPdf = mockHoveddokumentPartPdf();
        vedleggPart = mockVedleggPart("<some ID 3>");

        List<InputPart> inputParts = new ArrayList<>();
        Collections.addAll(inputParts, metadataPart, hoveddokumentPart, hoveddokumentPartPdf, vedleggPart);
        when(input.getParts()).thenReturn(inputParts);
    }

    @Test
    public void input_skal_kaste_exception_når_inputpart_ikke_har_minst_2_parts() {
        when(input.getParts()).thenReturn(new ArrayList<>());

        assertThatThrownBy(() -> tjeneste.uploadFile(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Må ha minst to deler,fikk 0");
    }

    @Test
    public void skal_kaste_teknisk_exception_hvis_metadata_ikke_er_første_part() {
        MultivaluedMap<String, String> map = new MultivaluedMapImpl<>();
        map.put("Content-Disposition", Arrays.asList("ikke_metadata"));
        when(metadataPart.getHeaders()).thenReturn(map);

        assertThatThrownBy(() -> tjeneste.uploadFile(input))
                .isInstanceOf(TekniskException.class)
                .hasMessage("FP-892453:The first part must be the \"metadata\" part");
    }

    @Test
    public void skal_kaste_teknisk_exception_hvis_metadata_ikke_er_json() {
        when(metadataPart.getMediaType()).thenReturn(MediaType.APPLICATION_XML_TYPE);

        assertThatThrownBy(() -> tjeneste.uploadFile(input))
                .isInstanceOf(TekniskException.class)
                .hasMessage("FP-892454:The \"metadata\" part should be application/json");
    }

    @Test
    public void skal_kaste_teknisk_exception_hvis_man_ikke_kan_hente_body_for_metadata() throws Exception {
        when(metadataPart.getBodyAsString()).thenThrow(IOException.class);

        assertThatThrownBy(() -> tjeneste.uploadFile(input))
                .isInstanceOf(TekniskException.class)
                .hasMessageContaining("FP-892466:Klarte ikke å lese inn dokumentet");
    }

    @Test
    public void skal_kaste_teknisk_exception_hvis_metadata_har_flere_filer_enn_lastet_opp() throws Exception {
        List<InputPart> inputParts = new ArrayList<>();
        Collections.addAll(inputParts, metadataPart, hoveddokumentPart, hoveddokumentPartPdf);
        when(input.getParts()).thenReturn(inputParts);

        assertThatThrownBy(() -> tjeneste.uploadFile(input))
                .isInstanceOf(TekniskException.class)
                .hasMessageContaining("FP-892456:Metadata inneholder flere filer enn det som er lastet opp");
    }

    @Test
    public void skal_kaste_teknisk_exception_hvis_metadata_har_færre_filer_enn_lastet_opp() throws Exception {
        List<InputPart> inputParts = new ArrayList<>();
        String contentId = "<some ID 4>";
        Collections.addAll(inputParts, metadataPart, hoveddokumentPart, hoveddokumentPartPdf, vedleggPart,
                mockVedleggPart(contentId));
        when(input.getParts()).thenReturn(inputParts);

        assertThatThrownBy(() -> tjeneste.uploadFile(input))
                .isInstanceOf(TekniskException.class)
                .hasMessageContaining("FP-892446:")
                .hasMessageContaining(contentId);
    }

    @Test
    public void skal_kaste_teknisk_exception_hvis_hoveddokument_mangler_name() throws Exception {
        MultivaluedMap<String, String> map = new MultivaluedMapImpl<>();
        map.put("Content-Disposition", Arrays.asList("mangler ; foo=name"));
        when(hoveddokumentPart.getHeaders()).thenReturn(map);

        assertThatThrownBy(() -> tjeneste.uploadFile(input))
                .isInstanceOf(TekniskException.class)
                .hasMessageContaining("FP-892457:Unknown part name");
    }

    @Test
    public void skal_kaste_teknisk_exception_hvis_vedlegg_ikke_er_mediatype_pdf() throws Exception {
        when(vedleggPart.getMediaType()).thenReturn(MediaType.APPLICATION_XML_TYPE);

        assertThatThrownBy(() -> tjeneste.uploadFile(input))
                .isInstanceOf(TekniskException.class)
                .hasMessageContaining("FP-882558:Vedlegg er ikke pdf, Content-ID=<some ID 3>");
    }

    @Test
    public void skal_lagre_dokumentene() {
        Response response = tjeneste.uploadFile(input);
        assertThat(response.getStatus()).isEqualTo(Response.Status.ACCEPTED.getStatusCode());
        assertThat(response.getHeaderString(HttpHeaders.LOCATION))
                .isEqualTo("/dokumentforsendelse/status?forsendelseId=48f6e1cf-c5d8-4355-8e8c-b75494703959");
    }

    @Test
    public void skal_returnere_see_other_redirect_når_status_fpsak() {
        ForsendelseStatusDto status = new ForsendelseStatusDto(ForsendelseStatus.FPSAK);
        when(dokumentTjenesteMock.finnStatusinformasjon(any(UUID.class))).thenReturn(status);

        Response response = tjeneste.uploadFile(input);

        assertThat(response.getStatus()).isEqualTo(303);
    }

    @Test
    public void skal_returnere_status_ok_når_status_gosys() {
        ForsendelseStatusDto status = new ForsendelseStatusDto(ForsendelseStatus.GOSYS);
        when(dokumentTjenesteMock.finnStatusinformasjon(any(UUID.class))).thenReturn(status);

        Response response = tjeneste.uploadFile(input);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getEntity()).isEqualTo(status);
    }

    @Test
    public void skal_kaste_valideringsfeil_hvis_ugyldig_uuid() {
        assertThrows(IllegalArgumentException.class, () -> new ForsendelseIdDto("1234"));
    }

    @Test
    public void finnStatusInformasjon_skal_returnere_status_ok_når_status_ikke_er_fpsak() {
        UUID forsendelseId = UUID.randomUUID();
        ForsendelseIdDto idDto = new ForsendelseIdDto(forsendelseId.toString());
        ForsendelseStatusDto statusDto = new ForsendelseStatusDto(ForsendelseStatus.PENDING);
        when(dokumentTjenesteMock.finnStatusinformasjon(any(UUID.class))).thenReturn(statusDto);

        Response response = tjeneste.finnStatusinformasjon(idDto);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getEntity()).isEqualTo(statusDto);
    }

    @Test
    public void finnStatusInformasjon_skal_returnere_status_see_other_når_status_er_fpsak() {
        UUID forsendelseId = UUID.randomUUID();
        ForsendelseIdDto idDto = new ForsendelseIdDto(forsendelseId.toString());
        ForsendelseStatusDto statusDto = new ForsendelseStatusDto(ForsendelseStatus.FPSAK);
        when(dokumentTjenesteMock.finnStatusinformasjon(any(UUID.class))).thenReturn(statusDto);

        @SuppressWarnings("resource")
        var response = tjeneste.finnStatusinformasjon(idDto);

        assertThat(response.getStatus()).isEqualTo(303);
        assertThat(response.getEntity()).isEqualTo(statusDto);
    }

    @Test
    public void skal_populerer_AbacDataAttributter_med_aktør() {
        DokumentforsendelseRestTjeneste.AbacDataSupplier abacDataSupplier = new DokumentforsendelseRestTjeneste.AbacDataSupplier();
        AbacDataAttributter abacDataAttributter = abacDataSupplier.apply(input);
        assertThat(abacDataAttributter.toString()).contains("AKTØR_ID=[MASKERT#1]");
    }

    private static InputPart mockBasicInputPart(Optional<String> contentId, String contentDispositionName) {
        InputPart part = mock(InputPart.class);
        MultivaluedMap<String, String> map = new MultivaluedMapImpl<>();
        map.put("Content-Disposition",
                Arrays.asList("attachment; name=\"" + contentDispositionName + "\"; filename=\"" + "Farskap\""));
        contentId.ifPresent(id -> map.put("Content-ID", Arrays.asList(id)));
        when(part.getHeaders()).thenReturn(map);
        return part;
    }

    private InputPart mockMetadataPart() throws Exception, Exception {
        InputPart part = mockBasicInputPart(Optional.empty(), "metadata");
        when(part.getMediaType()).thenReturn(MediaType.APPLICATION_JSON_TYPE);
        when(part.getBodyAsString()).thenReturn(byggMetadataString());
        return part;
    }

    private static InputPart mockHoveddokumentPartXml() throws Exception {
        InputPart part = mockBasicInputPart(Optional.of("<some ID 1>"), "hoveddokument");
        when(part.getMediaType()).thenReturn(MediaType.APPLICATION_XML_TYPE);
        when(part.getBodyAsString()).thenReturn("body");
        return part;
    }

    private static InputPart mockHoveddokumentPartPdf() throws Exception {
        InputPart part = mockBasicInputPart(Optional.of("<some ID 2>"), "hoveddokument");
        when(part.getMediaType()).thenReturn(MediaType.valueOf("application/pdf"));
        when(part.getBodyAsString()).thenReturn("");
        when(part.getBody(byte[].class, null)).thenReturn("body".getBytes(Charset.forName("UTF-8")));
        return part;
    }

    private static InputPart mockVedleggPart(String contentId) throws Exception {
        InputPart part = mockBasicInputPart(Optional.of(contentId), "vedlegg");
        when(part.getMediaType()).thenReturn(MediaType.valueOf("application/pdf"));
        when(part.getBodyAsString()).thenReturn("");
        when(part.getBody(byte[].class, null)).thenReturn("body".getBytes(Charset.forName("UTF-8")));
        return part;
    }

    private String byggMetadataString() throws Exception {
        URL resource = this.getClass().getClassLoader().getResource("testdata/metadata.json");
        Path path = Paths.get(resource.toURI());
        return new String(Files.readAllBytes(path));
    }

}
