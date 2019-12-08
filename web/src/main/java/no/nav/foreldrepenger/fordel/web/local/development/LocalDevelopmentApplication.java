package no.nav.foreldrepenger.fordel.web.local.development;

import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import io.swagger.v3.jaxrs2.SwaggerSerializers;

@ApplicationPath("/localdevelopment")
public class LocalDevelopmentApplication extends Application {
    // FIXME : Denne pakken skal ligge i src/test, men sliter litt med å få det til
    // :(
    @Override
    public Set<Class<?>> getClasses() {
        return Set.of(FakeQueueMottakRestTjeneste.class, SwaggerSerializers.class);
    }
}
