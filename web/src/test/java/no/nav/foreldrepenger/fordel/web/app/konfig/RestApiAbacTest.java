package no.nav.foreldrepenger.fordel.web.app.konfig;

import static org.assertj.core.api.Fail.fail;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import no.nav.vedtak.isso.config.ServerInfo;
import no.nav.vedtak.sikkerhet.abac.AbacDto;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessurs;
import no.nav.vedtak.sikkerhet.abac.TilpassetAbacAttributt;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ActionType;
import no.nav.vedtak.sikkerhet.abac.beskyttet.ResourceType;

class RestApiAbacTest {

    private static String PREV_LB_URL;

    /**
     * IKKE ignorer denne testen, sikrer at REST-endepunkter får tilgangskontroll
     *
     */
    @Test
    void test_at_alle_restmetoder_er_annotert_med_BeskyttetRessurs() throws Exception {
        for (Method restMethod : RestApiTester.finnAlleRestMetoder()) {
            if (restMethod.getAnnotation(BeskyttetRessurs.class) == null) {
                throw new AssertionError("Mangler @" + BeskyttetRessurs.class.getSimpleName() + "-annotering på " + restMethod);
            }
        }
    }

    @Test
    void sjekk_at_ingen_metoder_er_annotert_med_dummy_verdier() {
        for (Method metode : RestApiTester.finnAlleRestMetoder()) {
            assertAtIngenBrukerDummyVerdierPåBeskyttetRessurs(metode);
        }
    }

    /**
     * IKKE ignorer denne testen, helper til med at input til tilgangskontroll blir
     * riktig
     * <p>
     * Kontakt Team Humle hvis du trenger hjelp til å endre koden din slik at den
     * går igjennom her *
     */
    @Test
    void test_at_alle_input_parametre_til_restmetoder_implementer_AbacDto() throws Exception {
        String feilmelding = "Parameter på %s.%s av type %s må implementere " + AbacDto.class.getSimpleName() + ".\n";
        StringBuilder feilmeldinger = new StringBuilder();

        for (Method restMethode : RestApiTester.finnAlleRestMetoder()) {
            for (Parameter parameter : restMethode.getParameters()) {
                if (Collection.class.isAssignableFrom(parameter.getType())) {
                    ParameterizedType type = (ParameterizedType) parameter.getParameterizedType();
                    @SuppressWarnings("rawtypes")
                    Class<?> aClass = (Class) (type.getActualTypeArguments()[0]);
                    if (!AbacDto.class.isAssignableFrom(aClass)
                            && !parameter.isAnnotationPresent(TilpassetAbacAttributt.class)
                            && !IgnorerteInputTyper.ignore(aClass)) {
                        feilmeldinger.append(String.format(feilmelding, restMethode.getDeclaringClass().getSimpleName(), restMethode.getName(),
                                aClass.getSimpleName()));
                    }
                } else {
                    if (!AbacDto.class.isAssignableFrom(parameter.getType())
                            && !parameter.isAnnotationPresent(TilpassetAbacAttributt.class)
                            && !IgnorerteInputTyper.ignore(parameter.getType())) {
                        feilmeldinger.append(String.format(feilmelding, restMethode.getDeclaringClass().getSimpleName(), restMethode.getName(),
                                parameter.getType().getSimpleName()));
                    }
                }
            }
        }
        if (feilmeldinger.length() > 0) {
            throw new AssertionError("Følgende inputparametre til REST-tjenester mangler AbacDto-impl\n" + feilmeldinger);
        }
    }

    private static void assertAtIngenBrukerDummyVerdierPåBeskyttetRessurs(Method metode) {
        Class<?> klasse = metode.getDeclaringClass();
        BeskyttetRessurs annotation = metode.getAnnotation(BeskyttetRessurs.class);
        if ((annotation != null) && (annotation.actionType() == ActionType.DUMMY)) {
            fail(klasse.getSimpleName() + "." + metode.getName() + " Ikke bruk DUMMY-verdi for "
                    + ActionType.class.getSimpleName());
        } else if (annotation != null && annotation.resource().isEmpty() && annotation.resourceType() == ResourceType.DUMMY && annotation.property().isEmpty()) {
            fail(klasse.getSimpleName() + "." + metode.getName() + " En verdi for resource må være satt!");
        }
    }

    /**
     * Disse typene slipper naturligvis krav om impl av {@link AbacDto}
     */
    enum IgnorerteInputTyper {
        BOOLEAN(Boolean.class.getName()),
        SERVLET(HttpServletRequest.class.getName());

        private String className;

        IgnorerteInputTyper(String className) {
            this.className = className;
        }

        static boolean ignore(Class<?> klasse) {
            return Arrays.stream(IgnorerteInputTyper.values()).anyMatch(e -> e.className.equals(klasse.getName()));
        }
    }

    @BeforeAll
    public static void setup() {
        PREV_LB_URL = System.setProperty(ServerInfo.PROPERTY_KEY_LOADBALANCER_URL, "http://localhost:8090");
    }

    @AfterAll
    public static void teardown() {
        if (PREV_LB_URL != null) {
            System.setProperty(ServerInfo.PROPERTY_KEY_LOADBALANCER_URL, PREV_LB_URL);
        }
    }
}
