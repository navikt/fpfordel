package no.nav.foreldrepenger.fordel.web.app.util;

import static java.lang.System.getenv;

import java.util.Arrays;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum Cluster {
    LOCAL("local", false),
    DEV_FSS_Q1("dev-fss", "q1", false),
    DEV_FSS_T4("dev-fss", "t4", false),
    PROD_FSS("prod-fss", "p", true);

    private static final Logger LOG = LoggerFactory.getLogger(Cluster.class);
    private static final String NAIS_CLUSTER_NAME = "NAIS_CLUSTER_NAME";
    private static final String FASIT_ENVIRONMENT_NAME = "FASIT_ENVIRONMENT_NAME";

    private final String naiseratorName;
    private final String naisdName;
    private final boolean isProd;

    Cluster(String naiseratorName, boolean isProd) {
        this(naiseratorName, null, isProd);
    }

    Cluster(String naiseratorName, String naisdName, boolean isProd) {
        this.naiseratorName = naiseratorName;
        this.naisdName = naisdName;
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
        return Optional.ofNullable(getenv(NAIS_CLUSTER_NAME))
                .filter(naiseratorName::equals)
                .or(this::naisdName)
                .isPresent();
    }

    private Optional<String> naisdName() {
        return Optional.ofNullable(getenv(FASIT_ENVIRONMENT_NAME))
                .filter(naisdName::equals);
    }
}
