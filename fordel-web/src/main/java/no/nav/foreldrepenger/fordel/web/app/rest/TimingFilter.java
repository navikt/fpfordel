package no.nav.foreldrepenger.fordel.web.app.rest;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class TimingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOG = LoggerFactory.getLogger(TimingFilter.class);
    private static final ThreadLocalTimer TIMER = new ThreadLocalTimer();

    @Override
    public void filter(ContainerRequestContext req, ContainerResponseContext res) throws IOException {
        var stop = TIMER.stop();

        LOG.info("Eksekvering {} tok {}ms", req.getUriInfo().getPath(), stop);
    }

    @Override
    public void filter(ContainerRequestContext req) throws IOException {
        TIMER.start();
    }

    private static class ThreadLocalTimer extends ThreadLocal<Long> {
        public void start() {
            this.set(System.currentTimeMillis());
        }

        public long stop() {
            return System.currentTimeMillis() - get();
        }

        @Override
        protected Long initialValue() {
            return System.currentTimeMillis();
        }
    }
}
