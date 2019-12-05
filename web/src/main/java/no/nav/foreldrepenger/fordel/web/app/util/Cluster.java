package no.nav.foreldrepenger.fordel.web.app.util;

import static java.lang.System.getenv;

import java.util.Arrays;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum Cluster {
    LOCAL("local", false),
    DEV_FSS("dev-fss", false),
    PROD_FSS("prod-fss", true);

    private static final Logger LOG = LoggerFactory.getLogger(Cluster.class);
    public static final String NAIS_CLUSTER_NAME = "NAIS_CLUSTER_NAME";

    private final String naiseratorName;
    private final boolean isProd;

    Cluster(String naiseratorName, boolean isProd) {
        this.naiseratorName = naiseratorName;
        this.isProd = isProd;
    }

    public String clusterName() {
        return naiseratorName;
    }

    public boolean isProd() {
        return isProd;
    }

    public static Cluster current() {
        return Arrays.stream(values())
                .filter(Cluster::isActive)
                .findFirst()
                .orElse(LOCAL);
    }

    private boolean isActive() {
        return Optional.ofNullable(env(LoggerUtil.NAIS_CLUSTER_NAME))
                .filter(naiseratorName::equals)
                .isPresent();
    }

    private String env(String key) {
        var val = getenv(key);
        LOG.info("{}={}", key, val);
        return val;
    }
}
