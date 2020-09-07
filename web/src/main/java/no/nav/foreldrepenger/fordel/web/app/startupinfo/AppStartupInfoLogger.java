package no.nav.foreldrepenger.fordel.web.app.startupinfo;

import static com.google.common.collect.Maps.fromProperties;
import static java.util.Map.Entry.comparingByKey;
import static no.nav.vedtak.konfig.StandardPropertySource.APP_PROPERTIES;
import static no.nav.vedtak.konfig.StandardPropertySource.ENV_PROPERTIES;
import static no.nav.vedtak.konfig.StandardPropertySource.SYSTEM_PROPERTIES;

import java.util.List;
import java.util.Map.Entry;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.konfig.StandardPropertySource;
import no.nav.vedtak.util.env.Environment;

@ApplicationScoped
class AppStartupInfoLogger {

    private static final List<String> SECRETS = List.of("passord", "password", "passwd");

    private static final Logger LOG = LoggerFactory.getLogger(AppStartupInfoLogger.class);

    private static final String OPPSTARTSINFO = "OPPSTARTSINFO";
    private static final String HILITE_SLUTT = "********";
    private static final String HILITE_START = HILITE_SLUTT;
    private static final String KONFIGURASJON = "Konfigurasjon";
    private static final String START = "start:";
    private static final String SLUTT = "slutt.";

    private static final List<String> IGNORE = List.of("TCP_ADDR", "PORT_HTTP", "SERVICE_HOST",
            "TCP_PROTO", "_TCP", "_PORT");

    private static final Environment ENV = Environment.current();

    @Inject
    AppStartupInfoLogger() {
    }

    void logAppStartupInfo() {
        log(HILITE_START + " " + OPPSTARTSINFO + " " + START + " " + HILITE_SLUTT);
        logKonfigurasjon();
        log(HILITE_START + " " + OPPSTARTSINFO + " " + SLUTT + " " + HILITE_SLUTT);
    }

    private static void logKonfigurasjon() {
        log(KONFIGURASJON + " " + START);
        log(SYSTEM_PROPERTIES);
        log(ENV_PROPERTIES);
        log(APP_PROPERTIES);
        log(KONFIGURASJON + " " + SLUTT);
    }

    private static void log(StandardPropertySource source) {
        fromProperties(ENV.getProperties(source).getVerdier()).entrySet()
                .stream()
                .sorted(comparingByKey())
                .forEach(e -> log(source, e));
    }

    private static void log(StandardPropertySource source, Entry<String, String> entry) {
        String value = secret(entry.getKey()) ? hide(entry.getValue()) : entry.getValue();
        log(ignore(entry.getKey()), "{}: {}={}", source.getName(), entry.getKey(), value);
    }

    private static boolean ignore(String key) {
        for (String ignore : IGNORE) {
            if (key.toLowerCase().endsWith(ignore.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private static boolean secret(String key) {
        for (String secret : SECRETS) {
            if (key.toLowerCase().endsWith(secret.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private static String hide(String val) {
        return "*".repeat(val.length());
    }

    private static void log(String msg, Object... args) {
        log(false, msg, args);
    }

    private static void log(boolean ignore, String msg, Object... args) {
        if (ignore) {
            LOG.debug(msg, args);
        } else {
            LOG.info(msg, args);
        }
    }
}
