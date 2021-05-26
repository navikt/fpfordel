package no.nav.foreldrepenger.fordel.web.app.metrics;

import static io.micrometer.core.instrument.Metrics.globalRegistry;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.swagger.v3.oas.annotations.Operation;

@Path("/metrics")
@ApplicationScoped
public class PrometheusRestService {

    static {
        new ClassLoaderMetrics().bindTo(globalRegistry);
        new JvmMemoryMetrics().bindTo(globalRegistry);
        new JvmGcMetrics().bindTo(globalRegistry);
        new ProcessorMetrics().bindTo(globalRegistry);
        new JvmThreadMetrics().bindTo(globalRegistry);
    }

    @GET
    @Operation(tags = "metrics", hidden = true)
    @Path("/prometheus")
    @Produces(TEXT_PLAIN)
    public Response prometheus() {
        return Response.ok()
                .entity(globalRegistry.getRegistries()
                        .stream()
                        .filter(PrometheusMeterRegistry.class::isInstance)
                        .map(PrometheusMeterRegistry.class::cast)
                        .map(PrometheusMeterRegistry::scrape))
                .build();

    }
}
