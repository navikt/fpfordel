package no.nav.foreldrepenger.fordel.web.app.rest;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOG = LoggerFactory.getLogger(TimingFilter.class);
    private static final ThreadLocalTimer TIMER = new ThreadLocalTimer();

    @Override
    public void filter(ContainerRequestContext req, ContainerResponseContext res) throws IOException {
        TIMER.stop();
        LOG.info("Eksekvering {} tok {}ms", req.getUriInfo().getMatchedURIs(), TIMER.get());
    }

    @Override
    public void filter(ContainerRequestContext req) throws IOException {
        TIMER.start();
    }

}
