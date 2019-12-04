package no.nav.foreldrepenger.fordel.web.app.util;

import static com.google.common.io.Resources.getResource;

import java.io.File;
import java.net.URL;

import org.slf4j.LoggerFactory;

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
        try {
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(context);
            context.reset();
            configurator.doConfigure(url(env));
        } catch (JoranException je) {
        }
        StatusPrinter.printInCaseOfErrorsOrWarnings(context);
    }

    private static URL url(Environment env) {
        var url = getResource("logback-" + env.clusterName() + ".xml");
        if (new File(url.getFile()).exists()) {
            return url;
        }
        return getResource("logback.xml");
    }
}
