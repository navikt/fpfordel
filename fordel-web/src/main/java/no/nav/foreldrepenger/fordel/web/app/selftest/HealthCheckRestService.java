package no.nav.foreldrepenger.fordel.web.app.selftest;

import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.Response.Status.SERVICE_UNAVAILABLE;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.annotations.Operation;
import no.nav.foreldrepenger.fordel.web.app.tjenester.ApplicationServiceStarter;
import no.nav.foreldrepenger.mottak.felles.LivenessAware;
import no.nav.foreldrepenger.mottak.felles.ReadinessAware;

@Path("/health")
@ApplicationScoped
public class HealthCheckRestService {

    private static final CacheControl CC = cacheControl();
    private static final Logger LOG = LoggerFactory.getLogger(HealthCheckRestService.class);
    private static final String RESPONSE_OK = "OK";

    private List<LivenessAware> live;
    private List<ReadinessAware> ready;
    private ApplicationServiceStarter starter;

    public HealthCheckRestService() {
        // CDI
    }

    @Inject
    public HealthCheckRestService(ApplicationServiceStarter starter, @Any Instance<LivenessAware> livenessAware,
            @Any Instance<ReadinessAware> readinessAware) {
        this(starter, livenessAware.stream().collect(toList()), readinessAware.stream().collect(toList()));
    }

    public HealthCheckRestService(ApplicationServiceStarter starter, List<LivenessAware> live, List<ReadinessAware> ready) {
        this.starter = starter;
        this.live = live;
        this.ready = ready;
    }

    @GET
    @Path("isAlive")
    @Operation(description = "sjekker om poden lever", tags = "nais", hidden = true)
    public Response isAlive() {
        if (live.stream().allMatch(LivenessAware::isAlive)) {
            return Response
                    .ok(RESPONSE_OK)
                    .cacheControl(CC)
                    .build();
        }
        return Response
                .serverError()
                .cacheControl(CC)
                .build();
    }

    @GET
    @Path("isReady")
    @Operation(description = "sjekker om poden er klar", tags = "nais", hidden = true)
    public Response isReady() {
        cacheControl();
        if (ready.stream().allMatch(ReadinessAware::isReady)) {
            return Response
                    .ok(RESPONSE_OK)
                    .cacheControl(CC)
                    .build();
        }
        return Response
                .status(SERVICE_UNAVAILABLE)
                .cacheControl(CC)
                .build();
    }

    private static CacheControl cacheControl() {
        CacheControl cc = new CacheControl();
        cc.setNoCache(true);
        cc.setNoStore(true);
        cc.setMustRevalidate(true);
        return cc;
    }

    @GET
    @Path("preStop")
    @Operation(description = "kalles på før stopp", tags = "nais", hidden = true)
    public Response preStop() {
        LOG.info("preStop endepunkt kalt");
        starter.stopServices();
        return Response.ok(RESPONSE_OK).build();
    }
}
