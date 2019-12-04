package no.nav.foreldrepenger.fordel.web.app.util;

import static java.lang.System.getenv;

import java.util.Optional;

public class Namespace {
    private static final String NAIS_NAMESPACE_NAME = "NAIS_NAMESPACE_NAME";

    private static final String DEFAULT_NAMESPACE = "default";

    private final String namespace;

    private Namespace(String name) {
        this.namespace = name;
    }

    public static Namespace of(String name) {
        return new Namespace(name);
    }

    public String getNamespace() {
        return namespace;
    }

    public static Namespace current() {
        return Namespace.of(Optional.ofNullable(getenv(NAIS_NAMESPACE_NAME))
                .orElse(DEFAULT_NAMESPACE));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[namespace=" + namespace + "]";
    }

}
