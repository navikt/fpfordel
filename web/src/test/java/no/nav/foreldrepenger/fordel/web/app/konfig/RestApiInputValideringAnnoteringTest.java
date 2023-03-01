package no.nav.foreldrepenger.fordel.web.app.konfig;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.function.Function;

import javax.validation.Valid;
import javax.ws.rs.core.Context;

import org.junit.jupiter.api.Test;

class RestApiInputValideringAnnoteringTest extends RestApiTester {
    private static String PREV_LB_URL;

    private Function<Method, String> printKlasseOgMetodeNavn = (method -> String.format("%s.%s", method.getDeclaringClass(), method.getName()));

    private static boolean isRequiredAnnotationPresent(Parameter parameter) {
        final Valid validAnnotation = parameter.getAnnotation(Valid.class);
        if (validAnnotation == null) {
            final Context contextAnnotation = parameter.getAnnotation(Context.class);
            return contextAnnotation != null;
        }
        return true;
    }

    /**
     * IKKE ignorer eller fjern denne testen, den sørger for at inputvalidering er i
     * orden for REST-grensesnittene
     * <p>
     * Kontakt Team Humle hvis du trenger hjelp til å endre koden din slik at den
     * går igjennom her
     */
    @Test
    void alle_felter_i_objekter_som_brukes_som_inputDTO_skal_enten_ha_valideringsannotering_eller_være_av_godkjent_type() throws Exception {
        for (Method method : finnAlleRestMetoder()) {
            for (int i = 0; i < method.getParameterCount(); i++) {
                assertThat(method.getParameterTypes()[i].isAssignableFrom(String.class)).as(
                    "REST-metoder skal ikke har parameter som er String eller mer generelt. Bruk DTO-er og valider. " + printKlasseOgMetodeNavn.apply(
                        method)).isFalse();
                assertThat(isRequiredAnnotationPresent(method.getParameters()[i])).as(
                        "Alle parameter for REST-metoder skal være annotert med @Valid. Var ikke det for " + printKlasseOgMetodeNavn.apply(method))
                    .withFailMessage("Fant parametere som mangler @Valid annotation '" + method.getParameters()[i].toString() + "'")
                    .isTrue();
            }
        }
    }

}
