package no.nav.foreldrepenger.fordel.web.app.konfig;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import com.codahale.metrics.MetricRegistry;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.dropwizard.DropwizardExports;
import io.prometheus.client.hotspot.DefaultExports;
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

    @Inject
    public void setRegistry(MetricRegistry registry) {
        //HS QAD siden registry ikke er tilgjengelig når klassen instansieres...
        DefaultExports.initialize();
        // Hook the Dropwizard registry into the Prometheus registry
        CollectorRegistry.defaultRegistry.register(new DropwizardExports(registry));
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
