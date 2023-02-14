package no.nav.foreldrepenger.fordel.web.app.tjenester;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.turbo.MarkerFilter;
import ch.qos.logback.core.spi.FilterReply;

public class CustomConfidentialMarkerFilter extends MarkerFilter {
        public static final Marker CONFIDENTIAL = MarkerFactory.getMarker("CONFIDENTIAL");

        public CustomConfidentialMarkerFilter() {
            super.setMarker(CONFIDENTIAL.getName());
        }

        public FilterReply decide(Marker marker, Logger logger, Level level, String format, Object[] params, Throwable t) {
            if (this.isStarted() && marker != null) {
                if (marker.equals(CONFIDENTIAL)) {
                    // deny som default. Har vanskelig med å få den som ligger i felles til å funke med ENV
                    return FilterReply.DENY;
                } else {
                    return FilterReply.NEUTRAL;
                }
            } else {
                return FilterReply.NEUTRAL;
            }
        }
}
