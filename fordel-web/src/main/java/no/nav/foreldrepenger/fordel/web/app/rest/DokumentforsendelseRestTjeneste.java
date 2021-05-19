package no.nav.foreldrepenger.fordel.web.app.rest;

import static javax.ws.rs.core.HttpHeaders.CONTENT_DISPOSITION;
import static javax.ws.rs.core.HttpHeaders.CONTENT_ID;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseStatus.FPSAK;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.CREATE;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import no.nav.foreldrepenger.fordel.kodeverdi.ArkivFilType;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.fordel.web.app.exceptions.FeltFeilDto;
import no.nav.foreldrepenger.fordel.web.app.exceptions.ValideringException;
import no.nav.foreldrepenger.fordel.web.app.jackson.JacksonJsonConfig;
import no.nav.foreldrepenger.fordel.web.server.abac.AppAbacAttributtType;
import no.nav.foreldrepenger.fordel.web.server.abac.BeskyttetRessursAttributt;
import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.foreldrepenger.mottak.domene.dokument.Dokument;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentMetadata;
import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.Dokumentforsendelse;
import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.DokumentforsendelseTjeneste;
import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.FilMetadata;
import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseIdDto;
import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseStatusDto;
import no.nav.security.token.support.core.api.Unprotected;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.log.mdc.MDCOperations;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;

@Path(DokumentforsendelseRestTjeneste.SERVICE_PATH)
@Produces(APPLICATION_JSON)
@RequestScoped
@Unprotected // midlertidig, fram til vi skrur over
public class DokumentforsendelseRestTjeneste {
    static final String SERVICE_PATH = "/dokumentforsendelse";

    private static final String DEFAULT_FPINFO_BASE_URI = "http://fpinfo";
    private static final String DOKUMENTFORSENDELSE_STATUS_PATH = "/fpinfo/api/dokumentforsendelse/status";

    private static final String PART_KEY_METADATA = "metadata";
    private static final String PART_KEY_HOVEDDOKUMENT = "hoveddokument";
    private static final String PART_KEY_VEDLEGG = "vedlegg";

    public static final MediaType APPLICATION_PDF_TYPE = MediaType.valueOf("application/pdf");

    private static final ObjectMapper OBJECT_MAPPER = new JacksonJsonConfig().getObjectMapper();
    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    private static final Logger LOG = LoggerFactory.getLogger(DokumentforsendelseRestTjeneste.class);

    private DokumentforsendelseTjeneste service;

    private URI fpStatusUrl;

    public DokumentforsendelseRestTjeneste() {
    }

    @Inject
    public DokumentforsendelseRestTjeneste(DokumentforsendelseTjeneste service,
            @KonfigVerdi(value = "fpinfo.base.url", defaultVerdi = DEFAULT_FPINFO_BASE_URI) URI endpoint) {
        this.service = service;
        this.fpStatusUrl = URI.create(endpoint.toString() + DOKUMENTFORSENDELSE_STATUS_PATH);
        LOG.trace("Created");
    }

    @POST
    @Consumes("multipart/mixed")

    @Operation(description = "Innsending av en dokumentforsendelse", tags = "Mottak", summary = "Denne kan ikke kalles fra Swagger", responses = {
            @ApiResponse(responseCode = "200", headers = {
                    @Header(name = HttpHeaders.LOCATION, description = "Link til hvor man kan følge statusen på dokumentforsendelsen") }),
    })
    @BeskyttetRessurs(action = CREATE, resource = BeskyttetRessursAttributt.FAGSAK)
    public Response uploadFile(@TilpassetAbacAttributt(supplierClass = AbacDataSupplier.class) MultipartInput input) {
        LOG.info("Innsending av dokumentforsendelse");
        List<InputPart> inputParts = input.getParts();
        if (inputParts.size() < 2) {
            throw new IllegalArgumentException("Må ha minst to deler,fikk " + inputParts.size());
        }
        MDCOperations.ensureCallId();

        var dokumentforsendelse = map(inputParts.get(0));
        var eksisterendeForsendelseStatus = service.finnStatusinformasjonHvisEksisterer(
                dokumentforsendelse.getForsendelsesId());

        var response = eksisterendeForsendelseStatus.map(
                status -> tilForsendelseStatusRespons(dokumentforsendelse, status)).orElseGet(() -> {
                    lagreDokumentForsendelse(inputParts.subList(1, inputParts.size()), dokumentforsendelse);
                    var status = service.finnStatusinformasjon(dokumentforsendelse.getForsendelsesId());
                    return tilForsendelseStatusRespons(dokumentforsendelse, status);
                });
        LOG.info("Innsending av dokumentforsendelse med id prosessert {}", dokumentforsendelse.getForsendelsesId());
        return response;
    }

    private void lagreDokumentForsendelse(List<InputPart> inputParts, Dokumentforsendelse dokumentforsendelse) {
        service.nyDokumentforsendelse(dokumentforsendelse.getMetadata());
        for (var inputPart : inputParts) {
            lagreDokument(dokumentforsendelse, inputPart);
        }
        validerDokumentforsendelse(dokumentforsendelse);
    }

    private Response tilForsendelseStatusRespons(Dokumentforsendelse dokumentforsendelse,
            ForsendelseStatusDto forsendelseStatusDto) {
        return switch (forsendelseStatusDto.getForsendelseStatus()) {
            case FPSAK -> {
                LOG.info("Forsendelse {} ble fordelt til FPSAK", dokumentforsendelse.getForsendelsesId());
                yield Response.seeOther(lagStatusURI(dokumentforsendelse.getForsendelsesId()))
                        .entity(forsendelseStatusDto)
                        .build();
            }
            case GOSYS -> {
                LOG.info("Forsendelse {} ble fordelt til GOSYS", dokumentforsendelse.getForsendelsesId());
                yield Response.ok(forsendelseStatusDto).build();
            }
            default -> {
                LOG.info("Forsendelse {} foreløpig ikke fordelt", dokumentforsendelse.getForsendelsesId());
                yield Response.accepted()
                        .location(URI.create(
                                SERVICE_PATH + "/status?forsendelseId=" + dokumentforsendelse.getForsendelsesId()))
                        .entity(forsendelseStatusDto)
                        .build();
            }
        };
    }

    @GET
    @Path("/status")
    @BeskyttetRessurs(action = READ, resource = BeskyttetRessursAttributt.FAGSAK)
    @Operation(description = "Finner status på prosessering av mottatt dokumentforsendelse", tags = "Mottak", summary = "Format: \"8-4-4-4-12\" eksempel \"48F6E1CF-C5D8-4355-8E8C-B75494703959\"", responses = {
            @ApiResponse(responseCode = "200", description = "Status og Periode"),
            @ApiResponse(responseCode = "303", description = "See Other")
    })
    public Response finnStatusinformasjon(
            @TilpassetAbacAttributt(supplierClass = ForsendelseAbacDataSupplier.class) @NotNull @QueryParam("forsendelseId") @Parameter(name = "forsendelseId") @Valid ForsendelseIdDto forsendelseIdDto) {
        UUID forsendelseId;
        try {
            forsendelseId = forsendelseIdDto.forsendelseId();
        } catch (IllegalArgumentException e) { // NOSONAR
            throw new ValideringException(List.of(new FeltFeilDto("forsendelseId", "Ugyldig uuid")));
        }

        ForsendelseStatusDto forsendelseStatusDto = service.finnStatusinformasjon(forsendelseId);
        if (FPSAK.equals(forsendelseStatusDto.getForsendelseStatus())) {
            return Response.seeOther(lagStatusURI(forsendelseId))
                    .entity(forsendelseStatusDto)
                    .build();
        } else {
            return Response.ok(forsendelseStatusDto).build();
        }
    }

    private URI lagStatusURI(UUID forsendelseId) {
        return URI.create(fpStatusUrl + "?forsendelseId=" + forsendelseId);
    }

    private Dokumentforsendelse map(InputPart inputPart) {
        var dokumentforsendelseDto = getMetadataDto(inputPart);
        return map(dokumentforsendelseDto);
    }

    private void lagreDokument(Dokumentforsendelse dokumentforsendelse, InputPart inputPart) {
        boolean hovedDokument;
        String name = getDirective(inputPart.getHeaders(), CONTENT_DISPOSITION, "name");
        if (name == null) {
            throw new TekniskException("FP-892457", "Unknown part name");
        }

        String contentId = inputPart.getHeaders().getFirst(CONTENT_ID);
        if (contentId == null) {
            throw new TekniskException("FP-892455", String.format("Mangler %s", CONTENT_ID));
        }

        switch (name) {
            case PART_KEY_HOVEDDOKUMENT:
                hovedDokument = true;
                break;
            case PART_KEY_VEDLEGG:
                hovedDokument = false;
                if (!APPLICATION_PDF_TYPE.isCompatible(inputPart.getMediaType())) {
                    throw new TekniskException("FP-882558", String.format("Vedlegg er ikke pdf, Content-ID=%s", contentId));
                }
                break;
            default:
                throw new TekniskException("FP-892457", "Unknown part name");
        }

        FilMetadata filMetadata = dokumentforsendelse.håndter(contentId);
        if (filMetadata == null) {
            throw new TekniskException("FP-892446", String.format("Metadata inneholder ikke informasjon om Content-ID=%s", contentId));
        }

        String partFilename = getDirective(inputPart.getHeaders(), CONTENT_DISPOSITION, "filename");
        String finalFilename = partFilename != null ? partFilename.strip() : partFilename;
        if ((finalFilename != null) && DokumentTypeId.ANNET.equals(filMetadata.dokumentTypeId())) {
            LOG.info("Mottatt vedlegg av type ANNET med filename {}", partFilename);
        }

        Dokument.Builder builder = Dokument.builder()
                .setForsendelseId(dokumentforsendelse.getForsendelsesId())
                .setHovedDokument(hovedDokument)
                .setBeskrivelse(finalFilename)
                .setDokumentTypeId(filMetadata.dokumentTypeId());
        try {
            if (MediaType.APPLICATION_XML_TYPE.isCompatible(inputPart.getMediaType())) {
                builder.setDokumentInnhold(inputPart.getBodyAsString().getBytes(Charset.forName("UTF-8")),
                        mapMediatypeTilArkivFilType(inputPart.getMediaType()));
            } else {
                builder.setDokumentInnhold(inputPart.getBody(byte[].class, null),
                        mapMediatypeTilArkivFilType(inputPart.getMediaType()));
            }
        } catch (IOException e) {
            throw new TekniskException("FP-892467", String.format("Klarte ikke å lese inn dokumentet, name=%s, Content-ID=%s", name, contentId), e);
        }

        Dokument dokument = builder.build();
        service.lagreDokument(dokument);
    }

    private void validerDokumentforsendelse(Dokumentforsendelse dokumentforsendelse) {
        if (!dokumentforsendelse.harHåndtertAlleFiler()) {
            throw new TekniskException("FP-892456", "Metadata inneholder flere filer enn det som er lastet opp");
        }

        service.validerDokumentforsendelse(dokumentforsendelse.getForsendelsesId());
    }

    private static DokumentforsendelseDto getMetadataDto(InputPart inputPart) {
        String name = getDirective(inputPart.getHeaders(), CONTENT_DISPOSITION, "name");
        if (!PART_KEY_METADATA.equals(name)) {
            throw new TekniskException("FP-892453", "The first part must be the metadata part");
        }
        if (!APPLICATION_JSON_TYPE.isCompatible(inputPart.getMediaType())) {
            throw new TekniskException("FP-892454", String.format("The metadata part should be %s", APPLICATION_JSON));
        }

        String body;
        try {
            body = inputPart.getBodyAsString();
        } catch (IOException e1) {
            throw new TekniskException("FP-892466", String.format("Klarte ikke å lese inn dokumentet, name=%s", name), e1);
        }

        DokumentforsendelseDto dokumentforsendelseDto;
        try {
            dokumentforsendelseDto = OBJECT_MAPPER.readValue(body, DokumentforsendelseDto.class);
        } catch (IOException e) {
            throw new TekniskException("FP-892458", String.format("Klarte ikke å parse %s", PART_KEY_METADATA), e);
        }

        Set<ConstraintViolation<DokumentforsendelseDto>> violations = VALIDATOR.validate(dokumentforsendelseDto);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        return dokumentforsendelseDto;
    }

    private static Dokumentforsendelse map(DokumentforsendelseDto dokumentforsendelseDto) {
        DokumentMetadata metadata = DokumentMetadata.builder()
                .setForsendelseId(dokumentforsendelseDto.getForsendelsesId())
                .setBrukerId(dokumentforsendelseDto.getBrukerId())
                .setSaksnummer(dokumentforsendelseDto.getSaksnummer())
                .setForsendelseMottatt(dokumentforsendelseDto.getForsendelseMottatt())
                .build();

        Map<String, FilMetadata> map = new HashMap<>();
        for (FilMetadataDto fmDto : dokumentforsendelseDto.getFiler()) {
            map.put(fmDto.getContentId(), new FilMetadata(fmDto.getContentId(), DokumentTypeId.fraOffisiellKode(fmDto.getDokumentTypeId())));
        }

        return new Dokumentforsendelse(metadata, map);
    }

    private static String getDirective(MultivaluedMap<String, String> headers, String headerName,
            String directiveName) {
        String[] directives = new String[0];
        String header = headers.getFirst(headerName);
        if (header != null) {
            directives = header.split(";");
        }
        for (String directive : directives) {
            if (directive.trim().startsWith(directiveName)) {
                String[] val = directive.split("=");
                return val[1].trim().replace("\"", "");
            }
        }
        return null;
    }

    private static ArkivFilType mapMediatypeTilArkivFilType(MediaType mediaType) {
        if (mediaType.isCompatible(APPLICATION_PDF_TYPE)) {
            return ArkivFilType.PDFA;
        }
        if (mediaType.isCompatible(MediaType.APPLICATION_XML_TYPE)) {
            return ArkivFilType.XML;
        }
        throw new TekniskException("FP-892468", String.format("Ulovlig mediatype %s", mediaType.getType()));

    }

    public static class AbacDataSupplier implements Function<Object, AbacDataAttributter> {

        @Override
        public AbacDataAttributter apply(Object obj) {
            List<InputPart> inputParts = ((MultipartInput) obj).getParts();
            if (inputParts.isEmpty()) {
                throw new IllegalArgumentException("No parts");
            }

            DokumentforsendelseDto dto = getMetadataDto(inputParts.get(0));
            return AbacDataAttributter.opprett().leggTil(AppAbacAttributtType.AKTØR_ID, dto.getBrukerId());
        }
    }

    public static class ForsendelseAbacDataSupplier implements Function<Object, AbacDataAttributter> {

        @Override
        public AbacDataAttributter apply(Object obj) {
            ForsendelseIdDto dto = ((ForsendelseIdDto) obj);
            return AbacDataAttributter.opprett().leggTil(AppAbacAttributtType.FORSENDELSE_UUID, dto.forsendelseId());
        }
    }

}
