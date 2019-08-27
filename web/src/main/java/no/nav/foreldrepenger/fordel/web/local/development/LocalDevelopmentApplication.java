package no.nav.foreldrepenger.fordel.web.local.development;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("/localdevelopment")
public class LocalDevelopmentApplication extends Application {
    //FIXME : Denne pakken skal ligge i src/test, men sliter litt med å få det til :(
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        classes.add(FakeQueueMottakRestTjeneste.class);

        classes.add(io.swagger.jaxrs.listing.ApiListingResource.class);
        classes.add(io.swagger.jaxrs.listing.SwaggerSerializers.class);
        return classes;
    }
}
