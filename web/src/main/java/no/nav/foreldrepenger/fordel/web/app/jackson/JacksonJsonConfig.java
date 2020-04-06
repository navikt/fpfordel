package no.nav.foreldrepenger.fordel.web.app.jackson;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

@Provider
public class JacksonJsonConfig implements ContextResolver<ObjectMapper> {

    private static final SimpleModule SER_DESER = createModule();
    private final ObjectMapper objectMapper;

    public JacksonJsonConfig() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new Jdk8Module());
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setTimeZone(TimeZone.getTimeZone("Europe/Oslo"));
        objectMapper.enable(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.disable(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        objectMapper.registerModule(SER_DESER);
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public static Module defaultModule() {
        return SER_DESER;
    }

    private static SimpleModule createModule() {
        SimpleModule module = new SimpleModule("VL-REST", new Version(1, 0, 0, null, null, null));

        addSerializers(module);
        addDeserializers(module);

        return module;
    }

    private static void addSerializers(SimpleModule module) {
        module.addSerializer(new KodeverdiSerializer());
        module.addSerializer(new LocalDateTimeSerializer(DateTimeFormatter.ISO_DATE_TIME));
        module.addSerializer(new LocalDateSerializer(DateTimeFormatter.ISO_DATE));
        module.addSerializer(new StringSerializer());
    }

    private static void addDeserializers(SimpleModule module) {
        module.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ISO_DATE_TIME));
        module.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ISO_DATE));
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        return objectMapper;
    }
}
