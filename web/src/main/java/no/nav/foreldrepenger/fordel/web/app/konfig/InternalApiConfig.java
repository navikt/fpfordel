package no.nav.foreldrepenger.fordel.web.app.konfig;

import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import no.nav.foreldrepenger.fordel.web.app.metrics.PrometheusRestService;
import no.nav.foreldrepenger.fordel.web.app.selftest.HealthCheckRestService;

@ApplicationScoped
@ApplicationPath(InternalApiConfig.API_URL)
public class InternalApiConfig extends Application {

    public static final String API_URL = "/internal";

    public InternalApiConfig() {
        // CDI
    }

    @Override
    public Set<Class<?>> getClasses() {
        return Set.of(PrometheusRestService.class, HealthCheckRestService.class);
    }
}
