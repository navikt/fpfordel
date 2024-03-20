package no.nav.foreldrepenger.mottak.hendelse;

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;

import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

public class WrappedAvroDeserializer<T extends SpecificRecord> implements Deserializer<T> {
    private final KafkaAvroDeserializer inner;

    public WrappedAvroDeserializer() {
        this.inner = new KafkaAvroDeserializer();
    }

    public WrappedAvroDeserializer(KafkaAvroDeserializer deser) {
        this.inner = deser;
    }

    WrappedAvroDeserializer(SchemaRegistryClient client) {
        this.inner = new KafkaAvroDeserializer(client);
    }

    public void configure(Map<String, ?> deserializerConfig, boolean isDeserializerForRecordKeys) {
        this.inner.configure(deserializerConfig, isDeserializerForRecordKeys);
    }

    public T deserialize(String topic, byte[] bytes) {
        return this.deserialize(topic, (Headers)null, bytes);
    }

    @SuppressWarnings("unchecked")
    public T deserialize(String topic, Headers headers, byte[] bytes) {
        return (T) this.inner.deserialize(topic, headers, bytes);
    }

    public void close() {
        this.inner.close();
    }
}
