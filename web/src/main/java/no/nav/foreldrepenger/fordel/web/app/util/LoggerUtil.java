package no.nav.foreldrepenger.fordel.web.app.util;

import static org.eclipse.jetty.util.resource.Resource.newClassPathResource;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

public class LoggerUtil {

    public static void setupLogging() {
        Environment env = Environment.current();
        /*
         * if (!env.isProd()) {
         * System.setProperty("log.level.no.nav.foreldrepenger.fordel", "TRACE"); }
         */
        // override default initialization
        configureLogging(env);
    }

    private static void configureLogging(Environment env) {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger logger = context.getLogger(LoggerUtil.class);
        try {
            var konfig = konfigFra(env, logger);
            if (konfig != null) {
                logger.info("Bruker loggekonfigurasjon {}", konfig);
                JoranConfigurator configurator = new JoranConfigurator();
                configurator.setContext(context);
                context.reset();
                configurator.doConfigure(konfig);
            } else {
                logger.warn("Ingen loggekonfigurasjon funnet");
            }
        } catch (JoranException e) {
            logger.warn("Dette gikk ikke så bra", e);
        }
        StatusPrinter.printInCaseOfErrorsOrWarnings(context);
    }

    private static URL konfigFra(Environment env, Logger logger) {
        return Optional.ofNullable(konfigFra("logback-" + env.clusterName() + ".xml", logger))
                .orElse(konfigFra("logback.xml", logger));
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
            return null;
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Uventer format på ressurs for " + konfig);
        }
    }
}
