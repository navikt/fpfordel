package no.nav.foreldrepenger.fordel.web.app.metrics;

import static no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyRestClient.REGISTRY;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import io.swagger.v3.oas.annotations.Operation;
import no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyRestClient;

@Path("/metrics")
@ApplicationScoped
public class PrometheusRestService {

    static {
        new ClassLoaderMetrics().bindTo(REGISTRY);
        new JvmMemoryMetrics().bindTo(REGISTRY);
        new JvmGcMetrics().bindTo(REGISTRY);
        new ProcessorMetrics().bindTo(REGISTRY);
        new JvmThreadMetrics().bindTo(REGISTRY);
    }

    @GET
    @Operation(tags = "metrics", hidden = true)
    @Path("/prometheus")
    public Response prometheus() {
        try (final Writer writer = new StringWriter()) {
            // TextFormatter.write004(writer,
            // CollectorRegistry.defaultRegistry.metricFamilySamples());
            // return
            // Response.ok().encoding("UTF-8").entity(writer.toString()).header("content-type",
            // TextFormatter.CONTENT_TYPE_004).build();
            return Response.ok().encoding("UTF-8").entity(AbstractJerseyRestClient.REGISTRY.scrape())
                    .header("content-type", TextFormatter.CONTENT_TYPE_004).build();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
