package no.nav.foreldrepenger.mottak.infotrygd.rest;

import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.readAllBytes;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class SerializationTest {

    private static ObjectMapper mapper;

    @BeforeClass
    public static void setup() throws IOException {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    public void saksnummerTest() throws Exception {
        test(saksnummer(42));
    }

    @Test
    public void sakTest() throws Exception {
        test(enSak(1));
    }

    @Test
    public void utbetalingTest() throws Exception {
        test(utbetaling(0));
    }

    @Test
    public void åpenSakTest() throws Exception {
        test(åpenSak(4));
    }

    @Test
    public void avsluttedeSakerTest() throws Exception {
        test(avsluttedeSaker(4));
    }

    @Test
    public void avsluttedeSakTest() throws Exception {
        test(enAvsluttetSak(4));
    }

    @Test
    public void sakResponsTest() throws Exception {
        test(sakRespons(2));
    }

    @Test
    public void faktiskResponsTest() throws Exception {
        testJson(jsonFra("rest/svprespons.json"), Saker.class);
    }

    private void testJson(String json, Class<?> clazz) throws Exception {
        testJson(json, clazz, true);
    }

    private void testJson(String json, Class<?> clazz, boolean log) throws Exception {
        var deser = mapper.readValue(json, clazz);
        if (log) {
            System.out.println("##");
            System.out.println(json);
            System.out.println("Deserialisert:     " + deser);
        }
    }

    private static void test(Object object) throws IOException {
        test(object, false);
    }

    private static void test(Object object, boolean log) throws IOException {
        String ser = write(object);
        var deser = mapper.readValue(ser, object.getClass());
        if (log) {
            System.out.println("##");
            System.out.println("Før serialisering: " + object);
            System.out.println(ser);
            System.out.println("Deserialisert:     " + deser);
        }
        assertEquals(object, deser);
    }

    private static String write(Object object) throws JsonProcessingException {
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
    }

    private static Saksnummer saksnummer(int n) {
        return new Saksnummer("B", n);
    }

    private static Sak enSak(int n) {
        return new Sak(plusDays(n), SakResultat.FB, saksnummer(n), "FI", SakType.S, plusDays(1));
    }

    private static LøpendeSak åpenSak(int n) {
        return new LøpendeSak(plusDays(n), utbetalinger(n));
    }

    private static AvsluttedeSaker avsluttedeSaker(int n) {
        return new AvsluttedeSaker(plusDays(n), alleAvsluttedeSaker(n));
    }

    private static AvsluttetSak enAvsluttetSak(int n) {
        return new AvsluttetSak(plusDays(n), plusDays(n + 1), utbetalinger(n));
    }

    private static IkkeStartetSak enIkkeStartetSak(int n) {
        return new IkkeStartetSak(plusDays(n), plusDays(n + 1));
    }

    private static List<AvsluttetSak> alleAvsluttedeSaker(int n) {
        return IntStream.range(0, n)
                .boxed()
                .map(SerializationTest::enAvsluttetSak)
                .collect(toList());
    }

    private static List<Utbetaling> utbetalinger(int n) {
        return IntStream.range(0, n)
                .boxed()
                .map(SerializationTest::utbetaling)
                .collect(toList());
    }

    private static List<LøpendeSak> åpneSaker(int n) {
        return IntStream.range(0, n)
                .boxed()
                .map(SerializationTest::åpenSak)
                .collect(toList());
    }

    private static List<Sak> saker(int n) {
        return IntStream.range(0, n)
                .boxed()
                .map(SerializationTest::enSak)
                .collect(toList());
    }

    private static List<IkkeStartetSak> ikkeStartedeSaker(int n) {
        return IntStream.range(0, n)
                .boxed()
                .map(SerializationTest::enIkkeStartetSak)
                .collect(toList());
    }

    private static Utbetaling utbetaling(int n) {
        return new Utbetaling(80, plusDays(n), plusDays(n + 1));
    }

    private static LocalDate plusDays(int n) {
        return LocalDate.now().plusDays(n);
    }

    private static Saker sakRespons(int n) {
        return new Saker("hello", saker(n), åpneSaker(n), avsluttedeSaker(n), ikkeStartedeSaker(n));
    }

    String jsonFra(String fil) throws Exception {
        return new String(readAllBytes(Paths.get(getClass().getClassLoader().getResource(fil).toURI())), UTF_8);
    }

}
