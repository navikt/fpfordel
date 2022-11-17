package no.nav.foreldrepenger.fordel.web.app.konfig;

import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
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
import no.nav.foreldrepenger.fordel.web.app.jackson.JacksonJsonConfig;
import no.nav.foreldrepenger.fordel.web.app.rest.DokumentforsendelseRestTjeneste;
import no.nav.foreldrepenger.fordel.web.app.tjenester.WhitelistingJwtTokenContainerRequestFilter;
import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vedtak.felles.prosesstask.rest.ProsessTaskRestTjeneste;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ServerProperties;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationPath(ApiConfig.API_URI)
public class ApiConfig extends Application {

    private static final Environment ENV = Environment.current();

    public static final String API_URI = "/api";

    private static final String ID_PREFIX = "openapi.context.id.servlet.";

    public ApiConfig() {
        var oas = new OpenAPI();
        var info = new Info()
                .title("Vedtaksløsningen - Fordeling.")
                .version("1.0")
                .description("REST grensesnitt for fordeling");

        oas.info(info).addServersItem(new Server().url(ENV.getProperty("context.path", "/fpfordel")));
        var oasConfig = new SwaggerConfiguration()
                .id(ID_PREFIX + ApiConfig.class.getName())
                .openAPI(oas)
                .prettyPrint(true)
                .resourceClasses(getClasses().stream().map(Class::getName).collect(Collectors.toSet()))
                .ignoredRoutes(Set.of("/api/sak/behandleDokument/v1"));

        try {
            new JaxrsOpenApiContextBuilder<>()
                    .ctxId(ID_PREFIX + ApiConfig.class.getName())
                    .application(this)
                    .openApiConfiguration(oasConfig)
                    .buildContext(true)
                    .read();
        } catch (OpenApiConfigurationException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public Set<Class<?>> getClasses() {
        return Set.of(
                WhitelistingJwtTokenContainerRequestFilter.class,
                ProsessTaskRestTjeneste.class,
                DokumentforsendelseRestTjeneste.class,
                ForvaltningRestTjeneste.class,
                OpenApiResource.class,
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
