package no.nav.foreldrepenger.mottak.hendelse.test;

import org.apache.avro.Schema;

import io.confluent.kafka.schemaregistry.avro.AvroSchema;
import io.confluent.kafka.schemaregistry.client.MockSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord;

public class VtpKafkaAvroDeserializer extends KafkaAvroDeserializer {

    @Override
    public Object deserialize(String topic, byte[] bytes) {
        this.schemaRegistry = getMockClient(JournalfoeringHendelseRecord.SCHEMA$);
        return super.deserialize(topic, bytes);
    }

    private static SchemaRegistryClient getMockClient(final Schema schema$) {
        return new MockSchemaRegistryClient() {
            @Override
            public synchronized AvroSchema getSchemaById(int id) {
                return new AvroSchema(schema$);
            }
        };
    }
}
