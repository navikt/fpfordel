package no.nav.foreldrepenger.fordel.web.app.konfig;

import java.util.Collection;
import java.util.Set;

import no.nav.foreldrepenger.fordel.web.app.exceptions.GeneralRestExceptionMapper;
import no.nav.foreldrepenger.fordel.web.app.jackson.JacksonJsonConfig;

public class FellesKlasserForRest {

    private static final Set<Class<?>> CLASSES = Set.of(JacksonJsonConfig.class, GeneralRestExceptionMapper.class);

    public static Collection<Class<?>> getClasses() {
        return CLASSES;
    }
}
