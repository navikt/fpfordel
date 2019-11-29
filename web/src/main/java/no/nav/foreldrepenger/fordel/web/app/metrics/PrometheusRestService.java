package no.nav.foreldrepenger.fordel.web.app.metrics;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import io.prometheus.client.CollectorRegistry;

@Path("/metrics")
@ApplicationScoped
public class PrometheusRestService {

    @GET
    @Path("/prometheus")
    public Response prometheus() {

        final Writer writer = new StringWriter();
        try {
            TextFormater.write004(writer, CollectorRegistry.defaultRegistry.metricFamilySamples());
            return Response.ok().encoding("UTF-8").entity(writer.toString())
                    .header("content-type", TextFormater.CONTENT_TYPE_004).build();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
