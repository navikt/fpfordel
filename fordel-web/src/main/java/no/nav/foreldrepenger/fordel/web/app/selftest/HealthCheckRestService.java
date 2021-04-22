package no.nav.foreldrepenger.fordel.web.app.selftest;

import static java.util.stream.Collectors.toList;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import io.swagger.v3.oas.annotations.Operation;
import no.nav.foreldrepenger.mottak.felles.LivenessAware;
import no.nav.foreldrepenger.mottak.felles.ReadinessAware;

@Path("/health")
@ApplicationScoped
public class HealthCheckRestService {

    private static final String RESPONSE_CACHE_KEY = "Cache-Control";
    private static final String RESPONSE_CACHE_VAL = "must-revalidate,no-cache,no-store";
    private static final String RESPONSE_OK = "OK";

    private List<LivenessAware> live;
    private List<ReadinessAware> ready;

    public HealthCheckRestService() {
        // CDI
    }

    @Inject
    public HealthCheckRestService(@Any Instance<LivenessAware> livenessAware, @Any Instance<ReadinessAware> readinessAware) {
        live = livenessAware.stream().collect(toList());
        ready = readinessAware.stream().collect(toList());
    }

    @GET
    @Path("isAlive")
    @Operation(description = "sjekker om poden lever", tags = "nais", hidden = true)
    public Response isAlive() {
        if (live.stream().allMatch(LivenessAware::isAlive)) {
            return Response
                    .ok(RESPONSE_OK)
                    .header(RESPONSE_CACHE_KEY, RESPONSE_CACHE_VAL)
                    .build();
        }
        return Response
                .serverError()
                .header(RESPONSE_CACHE_KEY, RESPONSE_CACHE_VAL)
                .build();

    }

    @GET
    @Path("isReady")
    @Operation(description = "sjekker om poden er klar", tags = "nais", hidden = true)
    public Response isReady() {
        if (ready.stream().allMatch(ReadinessAware::isReady)) {
            return Response
                    .ok(RESPONSE_OK)
                    .header(RESPONSE_CACHE_KEY, RESPONSE_CACHE_VAL)
                    .build();
        }
        return Response
                .status(Response.Status.SERVICE_UNAVAILABLE)
                .header(RESPONSE_CACHE_KEY, RESPONSE_CACHE_VAL)
                .build();
    }

}
