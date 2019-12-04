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
    private static final String NAIS_CLUSTER_NAME = "NAIS_CLUSTER_NAME";

    private final String clusterName;
    private final boolean isProd;

    Cluster(String clusterName, boolean isProd) {
        this.clusterName = clusterName;
        this.isProd = isProd;
    }

    public String clusterName() {
        return clusterName;
    }

    public boolean isProd() {
        return isProd;
    }

    public static Cluster current() {
        return Arrays.stream(values())
                .filter(Cluster::isActive)
                .findFirst()
                .orElse(Cluster.LOCAL);
    }

    private boolean isActive() {
        var aktiv = Optional.ofNullable(getenv(NAIS_CLUSTER_NAME))
                .filter(clusterName::equals)
                .isPresent();
        return aktiv;
    }

}
