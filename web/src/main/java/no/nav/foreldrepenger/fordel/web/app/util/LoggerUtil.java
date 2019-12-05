package no.nav.foreldrepenger.fordel.web.app.util;

import static org.eclipse.jetty.util.resource.Resource.newClassPathResource;

import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

public class LoggerUtil {
    static final String NAIS_CLUSTER_NAME = "NAIS_CLUSTER_NAME";

    public static void setupLogging() {
        Environment env = Environment.current();
        System.setProperty(NAIS_CLUSTER_NAME, env.clusterName());
        configureLogging(env);
    }

    private static void configureLogging(Environment env) {
        var context = (LoggerContext) LoggerFactory.getILoggerFactory();
        var logger = context.getLogger(LoggerUtil.class);
        try {
            var configurator = new JoranConfigurator();
            configurator.setContext(context);
            context.reset();
            configurator.doConfigure(konfigFra("logback.xml", logger));
        } catch (JoranException e) {
            logger.warn("Dette gikk ikke så bra", e);
        }
        StatusPrinter.printInCaseOfErrorsOrWarnings(context);
    }

    private static URL konfigFra(String konfig, Logger logger) {
        try {
            var resource = newClassPathResource(konfig);
            if (resource != null && resource.exists()) {
                URL url = resource.getURI().toURL();
                logger.info("Bruker loggekonfigurasjon {}", url);
                return url;
            }
            logger.info("Fant ingen loggekonfigurasjon for {}", konfig);
            throw new IllegalArgumentException("Fant ingen loggekonfigurasjon for " + konfig);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Fant ingen loggekonfigurasjon for " + konfig);
        }
    }
}
