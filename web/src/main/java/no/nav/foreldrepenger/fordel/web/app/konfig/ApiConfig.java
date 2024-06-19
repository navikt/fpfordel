package no.nav.foreldrepenger.fordel.web.app.konfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ServerProperties;

import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.integration.GenericOpenApiContextBuilder;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import no.nav.foreldrepenger.fordel.web.app.exceptions.ConstraintViolationMapper;
import no.nav.foreldrepenger.fordel.web.app.exceptions.GeneralRestExceptionMapper;
import no.nav.foreldrepenger.fordel.web.app.exceptions.JsonMappingExceptionMapper;
import no.nav.foreldrepenger.fordel.web.app.exceptions.JsonParseExceptionMapper;
import no.nav.foreldrepenger.fordel.web.app.forvaltning.ForvaltningRestTjeneste;
import no.nav.foreldrepenger.fordel.web.app.forvaltning.migrering.MigreringRestTjeneste;
import no.nav.foreldrepenger.fordel.web.app.jackson.JacksonJsonConfig;
import no.nav.foreldrepenger.fordel.web.app.rest.DokumentforsendelseRestTjeneste;
import no.nav.foreldrepenger.fordel.web.app.rest.journalføring.FerdigstillJournalføringRestTjeneste;
import no.nav.foreldrepenger.fordel.web.app.rest.journalføring.JournalføringRestTjeneste;
import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.prosesstask.rest.ProsessTaskRestTjeneste;

@ApplicationPath(ApiConfig.API_URI)
public class ApiConfig extends Application {

    public static final String API_URI = "/api";
    private static final Environment ENV = Environment.current();

    public ApiConfig() {
        var oas = new OpenAPI();
        var info = new Info().title("Vedtaksløsningen - Fordeling.")
            .version(Optional.ofNullable(ENV.imageName()).orElse("1.0"))
            .description("REST grensesnitt for fordeling");

        oas.info(info).addServersItem(new Server().url(ENV.getProperty("context.path", "/fpfordel")));
        var oasConfig = new SwaggerConfiguration().openAPI(oas)
            .prettyPrint(true)
            .resourceClasses(getClasses().stream().map(Class::getName).collect(Collectors.toSet()));
        try {
            new GenericOpenApiContextBuilder<>().openApiConfiguration(oasConfig).buildContext(true).read();
        } catch (OpenApiConfigurationException e) {
            throw new TekniskException("OPEN-API", e.getMessage(), e);
        }
    }

    @Override
    public Set<Class<?>> getClasses() {
        return Set.of(ProsessTaskRestTjeneste.class,
            FerdigstillJournalføringRestTjeneste.class,
            DokumentforsendelseRestTjeneste.class,
            JournalføringRestTjeneste.class,
            ForvaltningRestTjeneste.class,
            MigreringRestTjeneste.class,
            OpenApiResource.class,
            AuthenticationFilter.class,
            MultiPartFeature.class,
            ConstraintViolationMapper.class,
            JsonMappingExceptionMapper.class,
            JsonParseExceptionMapper.class,
            GeneralRestExceptionMapper.class,
            JacksonJsonConfig.class);
    }

    @Override
    public Map<String, Object> getProperties() {
        Map<String, Object> properties = new HashMap<>();
        // Ref Jersey doc
        properties.put(ServerProperties.BV_SEND_ERROR_IN_RESPONSE, true);
        properties.put(ServerProperties.PROCESSING_RESPONSE_ERRORS_ENABLED, true);
        return properties;
    }
}
