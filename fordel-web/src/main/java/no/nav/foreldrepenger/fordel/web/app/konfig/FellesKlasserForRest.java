package no.nav.foreldrepenger.fordel.web.app.konfig;

import java.util.Collection;
import java.util.Set;

import no.nav.foreldrepenger.fordel.web.app.exceptions.GeneralRestExceptionMapper;
import no.nav.foreldrepenger.fordel.web.app.jackson.JacksonJsonConfig;

public class FellesKlasserForRest {

    public static Collection<Class<?>> getClasses() {
        return Set.of(JacksonJsonConfig.class, GeneralRestExceptionMapper.class);
    }
}
