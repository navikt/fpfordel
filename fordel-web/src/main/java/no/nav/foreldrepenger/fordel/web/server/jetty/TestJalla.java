package no.nav.foreldrepenger.fordel.web.server.jetty;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import no.nav.vedtak.sikkerhet.ContextPathHolder;

public class TestJalla implements ServletContextListener {

    public TestJalla() {
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ContextPathHolder.instance(sce.getServletContext().getContextPath());
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }

}
