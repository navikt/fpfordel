package no.nav.foreldrepenger.fordel.web.app.util;

import static java.lang.System.getenv;

import java.util.Objects;
import java.util.Optional;

public final class ClusterDetector {

    private static final String NAIS_CLUSTER_NAME = "NAIS_CLUSTER_NAME";

    private ClusterDetector() {

    }

    public static boolean isProd() {
        return Optional.ofNullable(getenv(NAIS_CLUSTER_NAME))
                .filter(Objects::nonNull)
                .filter(name -> name.contains("prod"))
                .isPresent();
    }
}
