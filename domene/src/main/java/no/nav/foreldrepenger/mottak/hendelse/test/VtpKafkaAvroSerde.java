package no.nav.foreldrepenger.mottak.hendelse.test;

import java.util.Map;

import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;

import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerializer;

public class VtpKafkaAvroSerde<T extends SpecificRecord> implements Serde<T> {
    private final Serde<T> inner;

    public VtpKafkaAvroSerde() {
        this.inner = Serdes.serdeFrom(new SpecificAvroSerializer(), new VtpKafkaAvroDeserializer());
    }

    public Serializer<T> serializer() {
        return this.inner.serializer();
    }

    public Deserializer<T> deserializer() {
        return this.inner.deserializer();
    }

    public void configure(Map<String, ?> serdeConfig, boolean isSerdeForRecordKeys) {
        this.inner.serializer().configure(serdeConfig, isSerdeForRecordKeys);
        this.inner.deserializer().configure(serdeConfig, isSerdeForRecordKeys);
    }

    public void close() {
        this.inner.serializer().close();
        this.inner.deserializer().close();
    }
}
