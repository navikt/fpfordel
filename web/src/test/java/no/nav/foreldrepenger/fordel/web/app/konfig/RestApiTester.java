package no.nav.foreldrepenger.fordel.web.app.konfig;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import org.glassfish.jersey.media.multipart.MultiPart;

import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;

class RestApiTester {

    static final List<Class<?>> UNNTATT = Collections.singletonList(OpenApiResource.class);

    static Collection<Method> finnAlleRestMetoder() {
        List<Method> liste = new ArrayList<>();
        for (Class<?> klasse : finnAlleRestTjenester()) {
            for (Method method : klasse.getDeclaredMethods()) {
                if (Modifier.isPublic(method.getModifiers())) {
                    if (!erRestMetodeSomErUnntatt(method)) {
                        liste.add(method);
                    }
                }
            }
        }
        return liste;
    }

    private static boolean erRestMetodeSomErUnntatt(Method method) {
        boolean unntatt = // Et unntak pr linje
                ((method.getParameterCount() == 1)
                        && MultiPart.class.isAssignableFrom(method.getParameterTypes()[0]));
        return unntatt;
    }

    static Collection<Class<?>> finnAlleRestTjenester() {
        ApiConfig config = new ApiConfig();
        return config.getClasses().stream()
                .filter(c -> c.getAnnotation(Path.class) != null)
                .filter(c -> !UNNTATT.contains(c))
                .collect(Collectors.toList());
    }
}
