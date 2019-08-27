package no.nav.foreldrepenger.fordel.web.app.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import no.nav.foreldrepenger.fordel.kodeverk.Kodeliste;

/**
 * Enkel serialisering av KodeverkTabell klasser, uten at disse trenger @JsonIgnore eller lignende. Deserialisering går
 * av seg selv normalt (får null for andre felter).
 */
public class KodelisteSerializer extends StdSerializer<Kodeliste> {

    public KodelisteSerializer() {
        super(Kodeliste.class);
    }

    @Override
    public void serialize(Kodeliste value, JsonGenerator jgen, SerializerProvider provider) throws IOException {

        jgen.writeStartObject();

        jgen.writeStringField("kode", value.getKode());
        jgen.writeStringField("navn", value.getNavn());
        jgen.writeStringField("kodeverk", value.getKodeverk());

        jgen.writeEndObject();
    }

}
