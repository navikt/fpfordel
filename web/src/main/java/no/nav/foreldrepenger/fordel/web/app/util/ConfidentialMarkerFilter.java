package no.nav.foreldrepenger.fordel.web.app.util;

import static ch.qos.logback.core.spi.FilterReply.DENY;
import static ch.qos.logback.core.spi.FilterReply.NEUTRAL;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.turbo.MarkerFilter;
import ch.qos.logback.core.spi.FilterReply;

public class ConfidentialMarkerFilter extends MarkerFilter {

    public static final Marker CONFIDENTIAL = MarkerFactory.getMarker("CONFIDENTIAL");
    private final boolean isProd;

    public ConfidentialMarkerFilter() {
        this.isProd = Environment.current().isProd();
    }

    @Override
    public FilterReply decide(Marker marker, Logger logger, Level level, String format, Object[] params, Throwable t) {
        if (!isStarted() || marker == null) {
            return NEUTRAL;
        }

        if (marker.equals(CONFIDENTIAL)) {
            return isProd ? DENY : NEUTRAL;
        }
        return NEUTRAL;
    }

}
