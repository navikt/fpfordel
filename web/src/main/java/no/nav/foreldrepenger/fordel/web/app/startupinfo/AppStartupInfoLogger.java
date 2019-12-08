package no.nav.foreldrepenger.fordel.web.app.startupinfo;

import java.util.Map.Entry;
import java.util.SortedMap;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.health.HealthCheck;

import no.nav.foreldrepenger.fordel.web.app.selftest.SelftestResultat;
import no.nav.foreldrepenger.fordel.web.app.selftest.Selftests;
import no.nav.foreldrepenger.fordel.web.app.selftest.checks.ExtHealthCheck;
import no.nav.vedtak.log.mdc.MDCOperations;
import no.nav.vedtak.log.util.LoggerUtils;

@ApplicationScoped
class AppStartupInfoLogger {

    private static final Logger LOG = LoggerFactory.getLogger(AppStartupInfoLogger.class);

    private Selftests selftests;

    private static final String OPPSTARTSINFO = "OPPSTARTSINFO";
    private static final String HILITE_SLUTT = "********";
    private static final String HILITE_START = HILITE_SLUTT;
    private static final String KONFIGURASJON = "Konfigurasjon";
    private static final String SELFTEST = "Selftest";
    private static final String APPLIKASJONENS_STATUS = "Applikasjonens status";
    private static final String SYSPROP = "System property";
    private static final String ENVVAR = "Env. var";
    private static final String START = "start:";
    private static final String SLUTT = "slutt.";

    private static final String SKIP_LOG_SYS_PROPS = "skipLogSysProps";
    private static final String SKIP_LOG_ENV_VARS = "skipLogEnvVars";
    private static final String TRUE = "true";

    AppStartupInfoLogger() {
        // for CDI proxy
    }

    @Inject
    AppStartupInfoLogger(Selftests selftests) {
        this.selftests = selftests;
    }

    void logAppStartupInfo() {
        log(HILITE_START + " " + OPPSTARTSINFO + " " + START + " " + HILITE_SLUTT);
        logKonfigurasjon();
        logSelftest();
        log(HILITE_START + " " + OPPSTARTSINFO + " " + SLUTT + " " + HILITE_SLUTT);
    }

    private void logKonfigurasjon() {
        log(KONFIGURASJON + " " + START);

        SystemPropertiesHelper sysPropsHelper = SystemPropertiesHelper.getInstance();
        boolean skipSysProps = TRUE.equalsIgnoreCase(System.getProperty(SKIP_LOG_SYS_PROPS));
        boolean skipEnvVars = TRUE.equalsIgnoreCase(System.getProperty(SKIP_LOG_ENV_VARS));

        if (!skipSysProps) {
            SortedMap<String, String> sysPropsMap = sysPropsHelper.filteredSortedProperties();
            String sysPropFormat = SYSPROP + ": {}={}";
            for (Entry<String, String> entry : sysPropsMap.entrySet()) {
                log(sysPropFormat, LoggerUtils.removeLineBreaks(entry.getKey()), LoggerUtils.removeLineBreaks(entry.getValue()));
            }
        }

        if (!skipEnvVars) {
            SortedMap<String, String> envVarsMap = sysPropsHelper.filteredSortedEnvVars();
            for (Entry<String, String> entry : envVarsMap.entrySet()) {
                String envVarFormat = ENVVAR + ": {}={}";
                log(envVarFormat, LoggerUtils.removeLineBreaks(entry.getKey()), LoggerUtils.removeLineBreaks(entry.getValue()));
            }
        }

        log(KONFIGURASJON + " " + SLUTT);
    }

    private void logSelftest() {
        log(SELFTEST + " " + START);

        // callId er påkrevd på utgående kall og må settes før selftest kjøres
        MDCOperations.putCallId();
        SelftestResultat samletResultat = selftests.run();
        MDCOperations.removeCallId();

        for (HealthCheck.Result result : samletResultat.getAlleResultater()) {
            log(result);
        }

        log(APPLIKASJONENS_STATUS + ": {}", samletResultat.getAggregateResult());

        log(SELFTEST + " " + SLUTT);
    }

    private void log(String msg, Object... args) {
        LOG.info(msg, args); //NOSONAR
    }

    private void log(HealthCheck.Result result) {
        if (result.getDetails() != null) {
            OppstartFeil.FACTORY.selftestStatus(
                    getStatus(result.isHealthy()),
                    (String) result.getDetails().get(ExtHealthCheck.DETAIL_DESCRIPTION),
                    (String) result.getDetails().get(ExtHealthCheck.DETAIL_ENDPOINT),
                    (String) result.getDetails().get(ExtHealthCheck.DETAIL_RESPONSE_TIME),
                    result.getMessage()
            ).log(LOG);
        } else {
            OppstartFeil.FACTORY.selftestStatus(
                    getStatus(result.isHealthy()),
                    null,
                    null,
                    null,
                    result.getMessage()
            ).log(LOG);
        }
    }

    private String getStatus(boolean isHealthy) {
        return isHealthy ? "OK" : "ERROR";
    }
}
