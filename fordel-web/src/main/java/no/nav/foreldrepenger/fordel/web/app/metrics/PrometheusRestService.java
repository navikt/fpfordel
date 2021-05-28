package no.nav.foreldrepenger.fordel.web.app.metrics;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static no.nav.vedtak.log.metrics.MetricsUtil.REGISTRY;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import io.swagger.v3.oas.annotations.Operation;

@Path("/metrics")
@ApplicationScoped
public class PrometheusRestService {

    @GET
    @Operation(tags = "metrics", hidden = true)
    @Path("/prometheus")
    @Produces(TEXT_PLAIN)
    public Response prometheus() {
        return Response.ok()
                .entity(REGISTRY.scrape())
                .build();
    }
}
