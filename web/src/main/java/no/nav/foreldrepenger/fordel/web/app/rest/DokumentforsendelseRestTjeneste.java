package no.nav.foreldrepenger.fordel.web.app.rest;

import static javax.ws.rs.core.HttpHeaders.CONTENT_DISPOSITION;
import static javax.ws.rs.core.HttpHeaders.CONTENT_ID;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseStatus.FPSAK;
import static no.nav.vedtak.feil.LogLevel.ERROR;
import static no.nav.vedtak.feil.LogLevel.WARN;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.CREATE;
import static no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt.READ;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
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
import no.nav.foreldrepenger.fordel.web.app.exceptions.Valideringsfeil;
import no.nav.foreldrepenger.fordel.web.app.jackson.JacksonJsonConfig;
import no.nav.foreldrepenger.mottak.domene.dokument.Dokument;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentMetadata;
import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.Dokumentforsendelse;
import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.DokumentforsendelseTjeneste;
import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.FilMetadata;
import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseIdDto;
import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseStatusDto;
import no.nav.foreldrepenger.sikkerhet.abac.AppAbacAttributtType;
import no.nav.foreldrepenger.sikkerhet.abac.BeskyttetRessursAttributt;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;
import no.nav.vedtak.konfig.KonfigVerdi;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;

@Path(DokumentforsendelseRestTjeneste.SERVICE_PATH)
@Produces(APPLICATION_JSON)
@RequestScoped
@Transactional
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

    public DokumentforsendelseRestTjeneste() { // For Rest-CDI
    }

    @Inject
    public DokumentforsendelseRestTjeneste(DokumentforsendelseTjeneste service,
                                           @KonfigVerdi(value = "fpinfo.base.url", defaultVerdi = DEFAULT_FPINFO_BASE_URI) URI endpoint) {
        this.service = service;
        this.fpStatusUrl = URI.create(endpoint.toString() + DOKUMENTFORSENDELSE_STATUS_PATH);
    }

    @POST
    @Consumes("multipart/mixed")
    @Operation(description = "Innsending av en dokumentforsendelse", tags = "Mottak", summary = "Denne kan ikke kalles fra Swagger", responses = {
            @ApiResponse(responseCode = "200", headers = {
                    @Header(name = HttpHeaders.LOCATION, description = "Link til hvor man kan følge statusen på dokumentforsendelsen") }),
    })
    @BeskyttetRessurs(action = CREATE, resource = BeskyttetRessursAttributt.FAGSAK)
    public Response uploadFile(@TilpassetAbacAttributt(supplierClass = AbacDataSupplier.class) MultipartInput input) {
        List<InputPart> inputParts = input.getParts();
        if (inputParts.size() < 2) {
            throw new IllegalArgumentException("Må ha minst to deler,fikk " + inputParts.size());
        }

        Dokumentforsendelse dokumentforsendelse = nyDokumentforsendelse(inputParts.remove(0));
        for (var inputPart : inputParts) {
            lagreDokument(dokumentforsendelse, inputPart);
        }
        validerDokumentforsendelse(dokumentforsendelse);

        ForsendelseStatusDto forsendelseStatusDto = service
                .finnStatusinformasjon(dokumentforsendelse.getForsendelsesId());
        switch (forsendelseStatusDto.getForsendelseStatus()) {
            case FPSAK:
                LOG.info("Forsendelse {} ble fordelt til FPSAK", dokumentforsendelse.getForsendelsesId());
                return Response.seeOther(lagStatusURI(dokumentforsendelse.getForsendelsesId()))
                        .entity(forsendelseStatusDto)
                        .build();
            case GOSYS:
                LOG.info("Forsendelse {} ble fordelt til GOSYS", dokumentforsendelse.getForsendelsesId());
                return Response.ok(forsendelseStatusDto).build();
            case PENDING:
            default:
                LOG.info("Forsendelse {} foreløpig ikke fordelt", dokumentforsendelse.getForsendelsesId());
                return Response.accepted()
                        .location(URI
                                .create(SERVICE_PATH + "/status?forsendelseId=" + dokumentforsendelse.getForsendelsesId()))
                        .entity(forsendelseStatusDto)
                        .build();
        }
    }

    @GET
    @Path("/status")
    @BeskyttetRessurs(action = READ, resource = BeskyttetRessursAttributt.FAGSAK)
    @Operation(description = "Finner status på prosessering av mottatt dokumentforsendelse", tags = "Mottak", summary = "Format: \"8-4-4-4-12\" eksempel \"48F6E1CF-C5D8-4355-8E8C-B75494703959\"", responses = {
            @ApiResponse(responseCode = "200", description = "Status og Periode"),
            @ApiResponse(responseCode = "303", description = "See Other")
    })
    public Response finnStatusinformasjon(
            @NotNull @QueryParam("forsendelseId") @Parameter(name = "forsendelseId") @Valid ForsendelseIdDto forsendelseIdDto) {
        UUID forsendelseId;
        try {
            forsendelseId = forsendelseIdDto.getForsendelseId();
        } catch (IllegalArgumentException e) { // NOSONAR
            FeltFeilDto ffd = new FeltFeilDto("forsendelseId", "Ugyldig uuid");
            throw new Valideringsfeil(Collections.singletonList(ffd));
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

    private Dokumentforsendelse nyDokumentforsendelse(InputPart inputPart) {
        DokumentforsendelseDto dokumentforsendelseDto = getMetadataDto(inputPart);
        Dokumentforsendelse dokumentforsendelse = mapping(dokumentforsendelseDto);

        service.nyDokumentforsendelse(dokumentforsendelse.getMetadata());
        return dokumentforsendelse;
    }

    private void lagreDokument(Dokumentforsendelse dokumentforsendelse, InputPart inputPart) {
        boolean hovedDokument;
        String name = getDirective(inputPart.getHeaders(), CONTENT_DISPOSITION, "name");
        if (name == null) {
            throw DokumentforsendelseRestTjenesteFeil.FACTORY.ukjentPartNavn().toException();
        }

        String contentId = inputPart.getHeaders().getFirst(CONTENT_ID);
        if (contentId == null) {
            throw DokumentforsendelseRestTjenesteFeil.FACTORY.manglerHeaderAttributt(CONTENT_ID).toException();
        }

        switch (name) {
            case PART_KEY_HOVEDDOKUMENT:
                hovedDokument = true;
                break;
            case PART_KEY_VEDLEGG:
                hovedDokument = false;
                if (!APPLICATION_PDF_TYPE.isCompatible(inputPart.getMediaType())) {
                    throw DokumentforsendelseRestTjenesteFeil.FACTORY.vedleggErIkkePdf(contentId).toException();
                }
                break;
            default:
                throw DokumentforsendelseRestTjenesteFeil.FACTORY.ukjentPartNavn().toException();
        }

        FilMetadata filMetadata = dokumentforsendelse.håndter(contentId);
        if (filMetadata == null) {
            throw DokumentforsendelseRestTjenesteFeil.FACTORY.manglerInformasjonIMetadata(contentId).toException();
        }

        String partFilename = getDirective(inputPart.getHeaders(), CONTENT_DISPOSITION, "filename");
        String finalFilename = partFilename != null ? partFilename.strip() : partFilename;
        if ((finalFilename != null) && DokumentTypeId.ANNET.equals(filMetadata.getDokumentTypeId())) {
            LOG.info("Mottatt vedlegg av type ANNET med filename {}", partFilename);
        }

        Dokument.Builder builder = Dokument.builder()
                .setForsendelseId(dokumentforsendelse.getForsendelsesId())
                .setHovedDokument(hovedDokument)
                .setBeskrivelse(finalFilename)
                .setDokumentTypeId(filMetadata.getDokumentTypeId());
        try {
            if (MediaType.APPLICATION_XML_TYPE.isCompatible(inputPart.getMediaType())) {
                builder.setDokumentInnhold(inputPart.getBodyAsString().getBytes(Charset.forName("UTF-8")),
                        mapMediatypeTilArkivFilType(inputPart.getMediaType()));
            } else {
                builder.setDokumentInnhold(inputPart.getBody(byte[].class, null),
                        mapMediatypeTilArkivFilType(inputPart.getMediaType()));
            }
        } catch (IOException e) {
            throw DokumentforsendelseRestTjenesteFeil.FACTORY.feiletUnderInnlesningAvInputPart(name, contentId, e)
                    .toException();
        }

        Dokument dokument = builder.build();
        service.lagreDokument(dokument);
    }

    private void validerDokumentforsendelse(Dokumentforsendelse dokumentforsendelse) {
        if (!dokumentforsendelse.harHåndtertAlleFiler()) {
            throw DokumentforsendelseRestTjenesteFeil.FACTORY.forventetFlereFilerIForsendelsen().toException();
        }

        service.validerDokumentforsendelse(dokumentforsendelse.getForsendelsesId());
    }

    private static DokumentforsendelseDto getMetadataDto(InputPart inputPart) {
        String name = getDirective(inputPart.getHeaders(), CONTENT_DISPOSITION, "name");
        if (!PART_KEY_METADATA.equals(name)) {
            throw DokumentforsendelseRestTjenesteFeil.FACTORY.førsteInputPartSkalVæreMetadata().toException();
        }
        if (!APPLICATION_JSON_TYPE.isCompatible(inputPart.getMediaType())) {
            throw DokumentforsendelseRestTjenesteFeil.FACTORY.metadataPartSkalHaMediaType(APPLICATION_JSON)
                    .toException();
        }

        String body;
        try {
            body = inputPart.getBodyAsString();
        } catch (IOException e1) {
            throw DokumentforsendelseRestTjenesteFeil.FACTORY.feiletUnderInnlesningAvInputPart(name, e1).toException();
        }

        DokumentforsendelseDto dokumentforsendelseDto;
        try {
            dokumentforsendelseDto = OBJECT_MAPPER.readValue(body, DokumentforsendelseDto.class);
        } catch (IOException e) {
            throw DokumentforsendelseRestTjenesteFeil.FACTORY.kunneIkkeParseMetadata(PART_KEY_METADATA, e)
                    .toException();
        }

        Set<ConstraintViolation<DokumentforsendelseDto>> violations = VALIDATOR.validate(dokumentforsendelseDto);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        return dokumentforsendelseDto;
    }

    private static Dokumentforsendelse mapping(DokumentforsendelseDto dokumentforsendelseDto) {
        DokumentMetadata metadata = DokumentMetadata.builder()
                .setForsendelseId(dokumentforsendelseDto.getForsendelsesId())
                .setBrukerId(dokumentforsendelseDto.getBrukerId())
                .setSaksnummer(dokumentforsendelseDto.getSaksnummer())
                .setForsendelseMottatt(dokumentforsendelseDto.getForsendelseMottatt())
                .build();

        Map<String, FilMetadata> map = new HashMap<>();
        for (FilMetadataDto fmDto : dokumentforsendelseDto.getFiler()) {
            String dokumentTypeId = fmDto.getDokumentTypeId();
            DokumentTypeId dokumentType = DokumentTypeId.fraOffisiellKode(dokumentTypeId);
            FilMetadata fmd = new FilMetadata(fmDto.getContentId(), dokumentType);
            map.put(fmDto.getContentId(), fmd);
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
        throw DokumentforsendelseRestTjenesteFeil.FACTORY.ulovligFilType(mediaType.getType()).toException();

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

    private interface DokumentforsendelseRestTjenesteFeil extends DeklarerteFeil {
        DokumentforsendelseRestTjeneste.DokumentforsendelseRestTjenesteFeil FACTORY = FeilFactory
                .create(DokumentforsendelseRestTjeneste.DokumentforsendelseRestTjenesteFeil.class);

        @TekniskFeil(feilkode = "FP-892453", feilmelding = "The first part must be the \"metadata\" part", logLevel = ERROR)
        Feil førsteInputPartSkalVæreMetadata();

        @TekniskFeil(feilkode = "FP-892454", feilmelding = "The \"metadata\" part should be %s", logLevel = ERROR)
        Feil metadataPartSkalHaMediaType(String mediaType);

        @TekniskFeil(feilkode = "FP-892455", feilmelding = "Mangler %s", logLevel = ERROR)
        Feil manglerHeaderAttributt(String attributt);

        @TekniskFeil(feilkode = "FP-892456", feilmelding = "Metadata inneholder flere filer enn det som er lastet opp", logLevel = ERROR)
        Feil forventetFlereFilerIForsendelsen();

        @TekniskFeil(feilkode = "FP-892446", feilmelding = "Metadata inneholder ikke informasjon om Content-ID=%s", logLevel = ERROR)
        Feil manglerInformasjonIMetadata(String contentId);

        @TekniskFeil(feilkode = "FP-892457", feilmelding = "Unknown part name", logLevel = WARN)
        Feil ukjentPartNavn();

        @TekniskFeil(feilkode = "FP-892458", feilmelding = "Klarte ikke å parse %s", logLevel = WARN)
        Feil kunneIkkeParseMetadata(String json, IOException e);

        @TekniskFeil(feilkode = "FP-892466", feilmelding = "Klarte ikke å lese inn dokumentet, name=%s", logLevel = WARN)
        Feil feiletUnderInnlesningAvInputPart(String name, IOException e);

        @TekniskFeil(feilkode = "FP-892467", feilmelding = "Klarte ikke å lese inn dokumentet, name=%s, Content-ID=%s", logLevel = WARN)
        Feil feiletUnderInnlesningAvInputPart(String name, String contentId, IOException e);

        @TekniskFeil(feilkode = "FP-892468", feilmelding = "Ulovlig mediatype %s", logLevel = ERROR)
        Feil ulovligFilType(String type);

        @TekniskFeil(feilkode = "FP-882558", feilmelding = "Vedlegg er ikke pdf, Content-ID=%s", logLevel = WARN)
        Feil vedleggErIkkePdf(String contentId);
    }

}
