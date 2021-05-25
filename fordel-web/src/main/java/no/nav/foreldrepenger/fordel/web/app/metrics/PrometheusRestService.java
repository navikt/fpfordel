package no.nav.foreldrepenger.fordel.web.app.metrics;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyRestClient.REGISTRY;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import io.prometheus.client.hotspot.DefaultExports;
import io.swagger.v3.oas.annotations.Operation;

@Path("/metrics")
@ApplicationScoped
public class PrometheusRestService {

    static {
        DefaultExports.initialize();
    }

    @GET
    @Operation(tags = "metrics", hidden = true)
    @Path("/prometheus")
    @Produces(TEXT_PLAIN)
    public Response prometheus() {
        return Response.ok().encoding("UTF-8")
                .entity(REGISTRY.scrape())
                .build();

    }
}
