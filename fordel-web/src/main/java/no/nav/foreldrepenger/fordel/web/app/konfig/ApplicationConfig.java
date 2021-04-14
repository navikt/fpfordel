package no.nav.foreldrepenger.fordel.web.app.konfig;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import io.swagger.v3.jaxrs2.SwaggerSerializers;
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
import no.nav.foreldrepenger.fordel.web.app.jackson.JacksonJsonConfig;
import no.nav.foreldrepenger.fordel.web.app.rest.DokumentforsendelseRestTjeneste;
import no.nav.security.token.support.jaxrs.JwtTokenContainerRequestFilter;
import no.nav.vedtak.felles.prosesstask.rest.ProsessTaskRestTjeneste;

@ApplicationPath(ApplicationConfig.API_URI)
public class ApplicationConfig extends Application {

    public static final String API_URI = "/api";

    public ApplicationConfig() {

        OpenAPI oas = new OpenAPI();
        Info info = new Info()
                .title("Vedtaksløsningen - Fordeling")
                .version("1.0")
                .description("REST grensesnitt for fordeling.");

        oas.info(info)
                .addServersItem(new Server()
                        .url("/fpfordel"));
        SwaggerConfiguration oasConfig = new SwaggerConfiguration()
                .openAPI(oas)
                .prettyPrint(true)
                .scannerClass("io.swagger.v3.jaxrs2.integration.JaxrsAnnotationScanner")
                .resourcePackages(Stream.of("no.nav")
                        .collect(Collectors.toSet()));

        try {
            new GenericOpenApiContextBuilder<>()
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
                JwtTokenContainerRequestFilter.class,
                ProsessTaskRestTjeneste.class,
                DokumentforsendelseRestTjeneste.class,
                ForvaltningRestTjeneste.class,
                SwaggerSerializers.class,
                OpenApiResource.class,
                ConstraintViolationMapper.class,
                JsonMappingExceptionMapper.class,
                JsonParseExceptionMapper.class,
                GeneralRestExceptionMapper.class,
                JacksonJsonConfig.class);

    }
}
