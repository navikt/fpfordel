package no.nav.foreldrepenger.fordel.web.app.konfig;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import io.swagger.jaxrs.config.BeanConfig;
import no.nav.foreldrepenger.fordel.web.app.exceptions.ConstraintViolationMapper;
import no.nav.foreldrepenger.fordel.web.app.exceptions.JsonMappingExceptionMapper;
import no.nav.foreldrepenger.fordel.web.app.exceptions.JsonParseExceptionMapper;
import no.nav.foreldrepenger.fordel.web.app.forvaltning.ForvaltningRestTjeneste;
import no.nav.foreldrepenger.fordel.web.app.rest.DokumentforsendelseRestTjeneste;
import no.nav.vedtak.felles.prosesstask.rest.ProsessTaskRestTjeneste;
import no.nav.vedtak.isso.config.ServerInfo;

@ApplicationPath(ApplicationConfig.API_URI)
public class ApplicationConfig extends Application {

    public static final String API_URI = "/api";

    public ApplicationConfig() {
        BeanConfig beanConfig = new BeanConfig();
        beanConfig.setVersion("1.0");
        if (ServerInfo.instance().isUsingTLS()) {
            beanConfig.setSchemes(new String[]{"https"});
        } else {
            beanConfig.setSchemes(new String[]{"http"});

        }
        beanConfig.setBasePath("/fpfordel/api");
        beanConfig.setResourcePackage("no.nav");
        beanConfig.setTitle("Vedtaksløsningen - Fordeling");
        beanConfig.setDescription("REST grensesnitt for Vedtaksløsningen.");
        beanConfig.setScan(true);
    }

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        classes.add(ProsessTaskRestTjeneste.class);
        classes.add(DokumentforsendelseRestTjeneste.class);
        classes.add(ForvaltningRestTjeneste.class);

        classes.add(io.swagger.jaxrs.listing.ApiListingResource.class);
        classes.add(io.swagger.jaxrs.listing.SwaggerSerializers.class);

        classes.add(ConstraintViolationMapper.class);
        classes.add(JsonMappingExceptionMapper.class);
        classes.add(JsonParseExceptionMapper.class);
        classes.addAll(FellesKlasserForRest.getClasses());

        return Collections.unmodifiableSet(classes);
    }
}
