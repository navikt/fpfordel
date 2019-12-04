package no.nav.foreldrepenger.fordel.web.app.util;

import static org.eclipse.jetty.util.resource.Resource.newClassPathResource;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jetty.util.resource.Resource;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

public class LoggerUtil {

    public static void setupLogging() {
        Environment env = Environment.current();
        if (!env.isProd()) {
            System.setProperty("log.level.no.nav.foreldrepenger.fordel", "TRACE");
        }
        // override default initialization
        configureLogging(env);
    }

    private static void configureLogging(Environment env) {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger logger = context.getLogger(LoggerUtil.class);
        try {
            var url = url(env, logger);
            if (url != null) {
                JoranConfigurator configurator = new JoranConfigurator();
                configurator.setContext(context);
                logger.info("URL er {}", url);
                context.reset();
                configurator.doConfigure(url);
            } else {
                logger.warn("Ingen URL funnet");
            }
        } catch (JoranException e) {
            logger.warn("Dette gikk ikke så bra", e);
        }
        StatusPrinter.printInCaseOfErrorsOrWarnings(context);
    }

    private static URL url(Environment env, Logger logger) {
        var resource = newClassPathResource("logback-" + env.clusterName() + ".xml");
        if (resource != null && resource.exists()) {
            return url(resource, logger);
        }
        logger.info("Fant ingen cluster-spesifikk loggekonfigurasjon");
        return url(newClassPathResource("logback.xml"), logger);
    }

    private static URL url(Resource resource, Logger logger) {
        try {
            if (resource != null && resource.exists()) {
                return resource.getURI().toURL();
            }
            logger.info("Fant ingen loggekonfigurasjon");
            return null;
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Uventer format på " + resource.getURI());
        }
    }
}
