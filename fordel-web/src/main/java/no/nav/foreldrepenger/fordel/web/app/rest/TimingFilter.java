package no.nav.foreldrepenger.fordel.web.app.rest;

import java.io.IOException;
import java.time.Duration;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.micrometer.core.instrument.Metrics;
import no.nav.vedtak.log.metrics.MetricsUtil;

@Provider
public class TimingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final String METRIC_NAME = "rest";
    private static final Logger LOG = LoggerFactory.getLogger(TimingFilter.class);
    private static final ThreadLocalTimer TIMER = new ThreadLocalTimer();

    public TimingFilter() {
        MetricsUtil.utvidMedHistogram(METRIC_NAME);
    }

    @Override
    public void filter(ContainerRequestContext req, ContainerResponseContext res) throws IOException {
        var stop = TIMER.stop();
        Metrics.timer(METRIC_NAME, "path", req.getUriInfo().getPath()).record(Duration.ofMillis(stop));
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
