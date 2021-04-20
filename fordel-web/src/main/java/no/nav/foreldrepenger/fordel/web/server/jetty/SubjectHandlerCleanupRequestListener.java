package no.nav.foreldrepenger.fordel.web.server.jetty;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.annotation.WebListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.sikkerhet.context.SubjectHandler;
import no.nav.vedtak.sikkerhet.context.ThreadLocalSubjectHandler;

@WebListener
public class SubjectHandlerCleanupRequestListener implements ServletRequestListener {

    private final Logger LOG = LoggerFactory.getLogger(SubjectHandlerCleanupRequestListener.class);

    @Override
    public void requestDestroyed(ServletRequestEvent sre) {
        LOG.trace("Servlet request destroyed");
        try {
            ThreadLocalSubjectHandler.class.cast(SubjectHandler.getSubjectHandler()).setSubject(null);
            LOG.trace("Subject fjernet fra ThredLocal OK");
        } catch (Exception e) {
            LOG.trace("Kunne ikke fjerne subject", e);
        }
    }
}
