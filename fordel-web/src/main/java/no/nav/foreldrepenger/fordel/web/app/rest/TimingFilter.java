package no.nav.foreldrepenger.fordel.web.app.rest;

import static io.micrometer.core.instrument.Metrics.timer;
import static no.nav.vedtak.log.metrics.MetricsUtil.utvidMedHistogram;

import java.io.IOException;
import java.time.Duration;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

@Provider
public class TimingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final String METRIC_NAME = "rest";
    private static final ThreadLocalTimer TIMER = new ThreadLocalTimer();

    public TimingFilter() {
        utvidMedHistogram(METRIC_NAME);
    }

    @Override
    public void filter(ContainerRequestContext req, ContainerResponseContext res) throws IOException {
        timer(METRIC_NAME, "path", req.getUriInfo().getPath()).record(Duration.ofMillis(TIMER.stop()));
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
