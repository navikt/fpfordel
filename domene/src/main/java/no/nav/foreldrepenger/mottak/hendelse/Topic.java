package no.nav.foreldrepenger.mottak.hendelse;

import static io.confluent.kafka.serializers.KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG;

import java.util.Map;
import java.util.Objects;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import no.nav.foreldrepenger.konfig.Environment;
import no.nav.foreldrepenger.mottak.hendelse.test.VtpKafkaAvroSerde;
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord;
import no.nav.vedtak.felles.integrasjon.kafka.KafkaProperties;

record Topic<K, V>(String topic, Serde<K> serdeKey, Serde<V> serdeValue) {

    private static final Environment ENV = Environment.current();

    Topic {
        Objects.requireNonNull(topic, "topic");
        Objects.requireNonNull(serdeKey, "serdeKey");
        Objects.requireNonNull(serdeValue, "serdeValue");
    }

    static Topic<String, JournalfoeringHendelseRecord> createConfiguredTopic(String topicName) {
        var configuredTopic = new Topic<>(topicName, Serdes.String(), getSerde());
        var schemaRegistryUrl = KafkaProperties.getAvroSchemaRegistryURL();
        if (schemaRegistryUrl != null && !schemaRegistryUrl.isEmpty()) {
            var schemaMap = Map.of(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl,
                AbstractKafkaSchemaSerDeConfig.BASIC_AUTH_CREDENTIALS_SOURCE, "USER_INFO", AbstractKafkaSchemaSerDeConfig.USER_INFO_CONFIG,
                KafkaProperties.getAvroSchemaRegistryBasicAuth(), SPECIFIC_AVRO_READER_CONFIG, true);
            configuredTopic.serdeKey().configure(schemaMap, true);
            configuredTopic.serdeValue().configure(schemaMap, false);
        }
        return configuredTopic;
    }

    private static Serde<JournalfoeringHendelseRecord> getSerde() {
        return ENV.isProd() || ENV.isDev() ? new SpecificAvroSerde<>() : new VtpKafkaAvroSerde<>();
    }

}
