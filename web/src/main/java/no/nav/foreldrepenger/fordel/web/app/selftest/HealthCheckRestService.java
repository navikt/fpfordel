package no.nav.foreldrepenger.fordel.web.app.selftest;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import no.nav.foreldrepenger.fordel.web.app.tjenester.ApplicationServiceStarter;

@Path("/health")
@ApplicationScoped
public class HealthCheckRestService {

    private static final String RESPONSE_CACHE_KEY = "Cache-Control";
    private static final String RESPONSE_CACHE_VAL = "must-revalidate,no-cache,no-store";
    private static final String RESPONSE_OK = "OK";

    private ApplicationServiceStarter starterService;
    private SelftestService selftestService;

    public HealthCheckRestService() {
        // CDI
    }

    @Inject
    public HealthCheckRestService(ApplicationServiceStarter starterService, SelftestService selftestService) {
        this.starterService = starterService;
        this.selftestService = selftestService;
    }

    @GET
    @Path("isAlive")
    public Response isAlive() {
        return Response
                .ok(RESPONSE_OK)
                .header(RESPONSE_CACHE_KEY, RESPONSE_CACHE_VAL)
                .build();
    }

    @GET
    @Path("isReady")
    public Response isReady() {
        if (selftestService.kritiskTjenesteFeilet()) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .header(RESPONSE_CACHE_KEY, RESPONSE_CACHE_VAL)
                    .build();
        } else {
            return Response.ok(RESPONSE_OK)
                    .header(RESPONSE_CACHE_KEY, RESPONSE_CACHE_VAL)
                    .build();
        }
    }

    @GET
    @Path("preStop")
    public Response preStop() {
        starterService.stopServices();
        return Response.ok(RESPONSE_OK).build();
    }
}
