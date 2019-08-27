package no.nav.foreldrepenger.fordel.web.app.konfig;

import io.swagger.jaxrs.listing.ApiListingResource;

import javax.ws.rs.Path;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;

public class RestApiTester {

    static final List<Class<?>> UNNTATT = Collections.singletonList(ApiListingResource.class);

    static Collection<Method> finnAlleRestMetoder() {
        List<Method> liste = new ArrayList<>();
        for (Class<?> klasse : finnAlleRestTjenester()) {
            for (Method method : klasse.getDeclaredMethods()) {
                if (Modifier.isPublic(method.getModifiers())) {
                    if(!erRestMetodeSomErUnntatt(method)) {
                        liste.add(method);
                    }
                }
            }
        }
        return liste;
    }

    private static boolean erRestMetodeSomErUnntatt(Method method) {
        boolean unntatt = // Et unntak pr linje
                (method.getParameterCount() == 1 && MultipartInput.class.isAssignableFrom(method.getParameterTypes()[0]));
        return unntatt;
    }

    static Collection<Class<?>> finnAlleRestTjenester() {
        ApplicationConfig config = new ApplicationConfig();
        return config.getClasses().stream()
            .filter(c -> c.getAnnotation(Path.class) != null)
            .filter(c -> !UNNTATT.contains(c))
            .collect(Collectors.toList());
    }
}
