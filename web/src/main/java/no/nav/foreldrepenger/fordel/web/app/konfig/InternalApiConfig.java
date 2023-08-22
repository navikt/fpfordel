package no.nav.foreldrepenger.fordel.web.app.konfig;

import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

import no.nav.foreldrepenger.fordel.web.app.metrics.PrometheusRestService;
import no.nav.foreldrepenger.fordel.web.app.healthcheck.HealthCheckRestService;

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
