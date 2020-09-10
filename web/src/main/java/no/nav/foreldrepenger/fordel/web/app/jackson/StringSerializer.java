package no.nav.foreldrepenger.fordel.web.app.jackson;

import java.io.IOException;
import java.lang.reflect.Type;

import org.owasp.encoder.Encode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;

import no.nav.vedtak.log.util.LoggerUtils;

public class StringSerializer extends StdScalarSerializer<Object> {
    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(StringSerializer.class);

    public StringSerializer() {
        super(String.class, false);
    }

    /** @deprecated */
    @Deprecated
    @Override
    public boolean isEmpty(Object value) {
        String str = (String) value;
        return (str == null) || (str.length() == 0);
    }

    @Override
    public boolean isEmpty(SerializerProvider prov, Object value) {
        String str = (String) value;
        return (str == null) || (str.length() == 0);
    }

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        String originalValue = (String) value;
        String encodedValue = Encode.forHtml(originalValue);
        if (!originalValue.equals(encodedValue)) {
            log.trace("Encoding av jsonString : fra '{}' til '{}'", LoggerUtils.removeLineBreaks(originalValue),
                    LoggerUtils.removeLineBreaks(encodedValue)); // NOSONAR
        }
        gen.writeString(encodedValue);
    }

    @Override
    public JsonNode getSchema(SerializerProvider provider, Type typeHint) {
        return this.createSchemaNode("string", true);
    }

    @Override
    public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint) throws JsonMappingException {
        this.visitStringFormat(visitor, typeHint);
    }
}
