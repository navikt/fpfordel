package no.nav.foreldrepenger.fordel.web.app.konfig;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import no.nav.foreldrepenger.fordel.web.app.metrics.PrometheusRestService;
import no.nav.foreldrepenger.fordel.web.app.selftest.HealthCheckRestService;
import no.nav.foreldrepenger.fordel.web.app.tjenester.SelftestRestTjeneste;

@ApplicationScoped
@ApplicationPath(InternalApplication.API_URL)
public class InternalApplication extends Application {

    public static final String API_URL = "/internal";

    public InternalApplication() {
        // CDI
    }
    
    @Override
    public Set<Class<?>> getClasses() {

        Set<Class<?>> classes = new HashSet<>();

        classes.add(PrometheusRestService.class);
        classes.add(HealthCheckRestService.class);
        classes.add(SelftestRestTjeneste.class);

        return Collections.unmodifiableSet(classes);
    }
}
