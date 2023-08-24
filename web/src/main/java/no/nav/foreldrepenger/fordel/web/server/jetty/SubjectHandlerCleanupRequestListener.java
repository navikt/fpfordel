package no.nav.foreldrepenger.fordel.web.server.jetty;

import jakarta.servlet.ServletRequestEvent;
import jakarta.servlet.ServletRequestListener;
import jakarta.servlet.annotation.WebListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.sikkerhet.context.ContextCleaner;

@WebListener
public class SubjectHandlerCleanupRequestListener implements ServletRequestListener {

    private static final Logger LOG = LoggerFactory.getLogger(SubjectHandlerCleanupRequestListener.class);

    @Override
    public void requestDestroyed(ServletRequestEvent sre) {

        try {
            ContextCleaner.enusureCleanContext();
        } catch (Exception e) {
            LOG.info("Kunne ikke fjerne subject", e);
        }
    }
}
