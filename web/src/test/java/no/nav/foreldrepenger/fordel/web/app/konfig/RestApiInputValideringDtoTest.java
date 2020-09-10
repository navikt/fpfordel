package no.nav.foreldrepenger.fordel.web.app.konfig;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.json.JsonValue;
import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;
import javax.validation.Valid;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import no.nav.foreldrepenger.fordel.IndexClasses;
import no.nav.vedtak.isso.config.ServerInfo;

@RunWith(Parameterized.class)
public class RestApiInputValideringDtoTest extends RestApiTester {

    private Class<?> dto;

    @Parameterized.Parameters(name = "Validerer Dto - {0}")
    public static Collection<Object[]> getDtos() {
        String prevLBUrl = System.setProperty(ServerInfo.PROPERTY_KEY_LOADBALANCER_URL, "http://localhost:8090");
        Set<Object[]> alleDtoTyper = finnAlleDtoTyper().stream().map(c -> new Object[] { c.getName(), c }).collect(Collectors.toSet());
        if (prevLBUrl != null) {
            System.setProperty(ServerInfo.PROPERTY_KEY_LOADBALANCER_URL, prevLBUrl);
        }
        return alleDtoTyper;
    }

    public RestApiInputValideringDtoTest(@SuppressWarnings("unused") String name, Class<?> dto) {
        this.dto = dto;
    }

    /**
     * IKKE ignorer eller fjern denne testen, den sørger for at inputvalidering er i
     * orden for REST-grensesnittene
     * <p>
     * Kontakt Team Humle hvis du trenger hjelp til å endre koden din slik at den
     * går igjennom her
     */
    @Test
    public void alle_felter_i_objekter_som_brukes_som_inputDTO_skal_enten_ha_valideringsannotering_eller_være_av_godkjent_type() throws Exception {
        Set<Class<?>> validerteKlasser = new HashSet<>(); // trengs for å unngå løkker og unngå å validere samme klasse flere ganger
                                                          // dobbelt
        validerRekursivt(validerteKlasser, dto, null);
    }

    private static final List<Class<? extends Object>> ALLOWED_ENUM_ANNOTATIONS = Arrays.asList(JsonProperty.class, JsonValue.class, JsonIgnore.class,
            Valid.class, Null.class, NotNull.class);

    @SuppressWarnings("rawtypes")
    private static final Map<Class, List<List<Class<? extends Annotation>>>> UNNTATT_FRA_VALIDERING = new HashMap<>() {
        {

            put(boolean.class, singletonList(emptyList()));
            put(Boolean.class, singletonList(emptyList()));

            // LocalDate og LocalDateTime har egne deserializers
            put(LocalDate.class, singletonList(emptyList()));
            put(LocalDateTime.class, singletonList(emptyList()));

            // Enforces av UUID selv
            put(UUID.class, singletonList(emptyList()));
        }
    };

    @SuppressWarnings("rawtypes")
    private static final Map<Class, List<List<Class<? extends Annotation>>>> VALIDERINGSALTERNATIVER = new HashMap<>() {
        {
            put(String.class, asList(
                    asList(Pattern.class, Size.class),
                    asList(Pattern.class),
                    singletonList(Digits.class)));
            put(Long.class, asList(
                    asList(Min.class, Max.class),
                    asList(Digits.class)));
            put(long.class, asList(
                    asList(Min.class, Max.class),
                    asList(Digits.class)));
            put(Integer.class, singletonList(
                    asList(Min.class, Max.class)));
            put(int.class, singletonList(
                    asList(Min.class, Max.class)));
            put(BigDecimal.class, asList(
                    asList(Min.class, Max.class, Digits.class),
                    asList(DecimalMin.class, DecimalMax.class, Digits.class)));

            putAll(UNNTATT_FRA_VALIDERING);
        }
    };

    private static List<List<Class<? extends Annotation>>> getVurderingsalternativer(Field field) {
        Class<?> type = field.getType();
        if (field.getType().isEnum()) {
            return Collections.singletonList(Collections.singletonList(Valid.class));
        } else if (Collection.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type)) {
            if (brukerGenerics(field)) {
                Type[] args = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
                if (Arrays.asList(args).stream().allMatch(t -> UNNTATT_FRA_VALIDERING.containsKey(t))) {
                    return Collections.singletonList(Arrays.asList(Size.class));
                }
            }
            return singletonList(Arrays.asList(Valid.class, Size.class));

        }
        return VALIDERINGSALTERNATIVER.get(type);
    }

    private static Set<Class<?>> finnAlleDtoTyper() {
        Set<Class<?>> parametre = new TreeSet<>(Comparator.comparing(Class::getName));
        for (Method method : finnAlleRestMetoder()) {
            parametre.addAll(Arrays.asList(method.getParameterTypes()));
            for (Type type : method.getGenericParameterTypes()) {
                if (type instanceof ParameterizedType) {
                    ParameterizedType genericTypes = (ParameterizedType) type;
                    for (Type gen : genericTypes.getActualTypeArguments()) {
                        parametre.add((Class<?>) gen);
                    }
                }
            }
        }
        Set<Class<?>> filtreteParametre = new TreeSet<>(Comparator.comparing(Class::getName));
        for (Class<?> klasse : parametre) {
            if (klasse.getName().startsWith("java")) {
                // ikke sjekk nedover i innebygde klasser, det skal brukes annoteringer på
                // tidligere tidspunkt
                continue;
            }
            filtreteParametre.add(klasse);
        }
        return filtreteParametre;
    }

    private static void validerRekursivt(Set<Class<?>> besøkteKlasser, Class<?> klasse, Class<?> forrigeKlasse) throws URISyntaxException {
        if (besøkteKlasser.contains(klasse)) {
            return;
        }

        ProtectionDomain protectionDomain = klasse.getProtectionDomain();
        CodeSource codeSource = protectionDomain.getCodeSource();
        if (codeSource == null) {
            // system klasse
            return;
        }

        besøkteKlasser.add(klasse);
        if ((klasse.getAnnotation(Entity.class) != null) || (klasse.getAnnotation(MappedSuperclass.class) != null)) {
            throw new AssertionError("Klassen " + klasse + " er en entitet, kan ikke brukes som DTO. Brukes i " + forrigeKlasse);
        }

        URL klasseLocation = codeSource.getLocation();
        for (Class<?> subklasse : IndexClasses.getIndexFor(klasseLocation.toURI()).getSubClassesWithAnnotation(klasse, JsonTypeName.class)) {
            validerRekursivt(besøkteKlasser, subklasse, forrigeKlasse);
        }
        for (Field field : getRelevantFields(klasse)) {
            if (field.getAnnotation(JsonIgnore.class) != null) {
                continue; // feltet blir hverken serialisert elle deserialisert, unntas fra sjekk
            }
            if (field.getType().isEnum()) {
                validerEnum(field);
                continue; // enum er OK
            }
            if (getVurderingsalternativer(field) != null) {
                validerRiktigAnnotert(field); // har konfigurert opp spesifikk validering
            } else if (field.getType().getName().startsWith("java")) {
                throw new AssertionError(
                        "Feltet " + field + " har ikke påkrevde annoteringer. Trenger evt. utvidelse av denne testen for å akseptere denne typen.");
            } else {
                validerHarValidAnnotering(field);
                validerRekursivt(besøkteKlasser, field.getType(), forrigeKlasse);
            }
            if (brukerGenerics(field)) {
                validerRekursivt(besøkteKlasser, field.getType(), forrigeKlasse);
                for (Class<?> klazz : genericTypes(field)) {
                    validerRekursivt(besøkteKlasser, klazz, forrigeKlasse);
                }
            }
        }
    }

    private static void validerEnum(Field field) {
        validerRiktigAnnotert(field);
        List<Annotation> illegal = Arrays.asList(field.getAnnotations()).stream().filter(a -> !ALLOWED_ENUM_ANNOTATIONS.contains(a.annotationType()))
                .collect(Collectors.toList());
        if (!illegal.isEmpty()) {
            throw new AssertionError("Ugyldige annotasjoner funnet på [" + field + "]: " + illegal);
        }

    }

    private static void validerHarValidAnnotering(Field field) {
        if (field.getAnnotation(Valid.class) == null) {
            throw new AssertionError("Feltet " + field + " må ha @Valid-annotering.");
        }
    }

    private static Set<Class<?>> genericTypes(Field field) {
        Set<Class<?>> klasser = new HashSet<>();
        ParameterizedType type = (ParameterizedType) field.getGenericType();
        for (Type t : type.getActualTypeArguments()) {
            klasser.add((Class<?>) t);
        }
        return klasser;
    }

    private static boolean brukerGenerics(Field field) {
        return field.getGenericType() instanceof ParameterizedType;
    }

    private static Set<Field> getRelevantFields(Class<?> klasse) {
        Set<Field> fields = new LinkedHashSet<>();
        while (!klasse.isPrimitive() && !klasse.getName().startsWith("java")) {
            fields.addAll(fjernStaticFields(Arrays.asList(klasse.getDeclaredFields())));
            klasse = klasse.getSuperclass();
        }
        return fields;
    }

    private static Collection<Field> fjernStaticFields(List<Field> fields) {
        return fields.stream().filter(f -> !Modifier.isStatic(f.getModifiers())).collect(Collectors.toList());
    }

    private static void validerRiktigAnnotert(Field field) {
        List<List<Class<? extends Annotation>>> alternativer = getVurderingsalternativer(field);
        for (List<Class<? extends Annotation>> alternativ : alternativer) {
            boolean harAlleAnnoteringerForAlternativet = true;
            for (Class<? extends Annotation> annotering : alternativ) {
                if (field.getAnnotation(annotering) == null) {
                    harAlleAnnoteringerForAlternativet = false;
                }
            }
            if (harAlleAnnoteringerForAlternativet) {
                return;
            }
        }
        throw new IllegalArgumentException("Feltet " + field + " har ikke påkrevde annoteringer: " + alternativer);
    }
}
