package no.nav.foreldrepenger.fordel.web.app.selftest;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class StackTraceFormatter {

    private static final Logger LOGGER = LoggerFactory.getLogger(StackTraceFormatter.class);

    private StackTraceFormatter() {
        // skjult
    }

    static String formatForWeb(Throwable error) {
        return format(error).replaceAll("\n", "<br />");
    }

    static String format(Throwable error) {
        String res;
        try {
            try (StringWriter stringWriter = new StringWriter()) {
                try (PrintWriter printWriter = new PrintWriter(stringWriter)) {
                    error.printStackTrace(printWriter);
                    res = stringWriter.toString();
                }
            }
        } catch (IOException e) {
            LOGGER.error("", e);
            res = "!! " + e.getMessage() + " !!";
            // otherwise ignored - not critical
        }
        return res;
    }
}
