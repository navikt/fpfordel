package no.nav.foreldrepenger.mottak.hendelse;


import static io.confluent.kafka.serializers.KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG;

import java.util.Map;
import java.util.Properties;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.errors.LogAndFailExceptionHandler;

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import no.nav.foreldrepenger.konfig.Environment;
import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.foreldrepenger.mottak.hendelse.test.VtpKafkaAvroSerde;
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord;

@Dependent
class JournalHendelseProperties {

    private static final Environment ENV = Environment.current();

    private final String clientId;
    private final String bootstrapServers;
    private final String applicationId;
    private final String trustStorePath;
    private final String keyStoreLocation;
    private final String credStorePassword;
    private final Topic<String, JournalfoeringHendelseRecord> topic;
    private final boolean isDeployment = ENV.isProd() || ENV.isDev();


    @Inject
    JournalHendelseProperties(@KonfigVerdi("kafka.topic.journal.hendelse") String topicName,
                              // De neste stammer fra Aivenator
                              @KonfigVerdi("KAFKA_BROKERS") String bootstrapServers,
                              @KonfigVerdi("KAFKA_SCHEMA_REGISTRY") String schemaRegistryUrl,
                              @KonfigVerdi("KAFKA_SCHEMA_REGISTRY_USER") String schemaRegistryUsername,
                              @KonfigVerdi("KAFKA_SCHEMA_REGISTRY_PASSWORD") String schemaRegistryPassword,
                              @KonfigVerdi("KAFKA_TRUSTSTORE_PATH") String trustStorePath,
                              @KonfigVerdi("KAFKA_KEYSTORE_PATH") String keyStoreLocation,
                              @KonfigVerdi("KAFKA_CREDSTORE_PASSWORD") String credStorePassword) {
        this.trustStorePath = trustStorePath;
        this.keyStoreLocation = keyStoreLocation;
        this.credStorePassword = credStorePassword;
        this.applicationId = "fpfordel";
        this.clientId = "fpfordel";
        this.bootstrapServers = bootstrapServers;
        this.topic = createConfiguredTopic(topicName, schemaRegistryUrl, getBasicAuth(schemaRegistryUsername, schemaRegistryPassword));
    }

    public Topic<String, JournalfoeringHendelseRecord> getTopic() {
        return topic;
    }

    Properties getProperties() {
        Properties props = new Properties();

        props.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
        props.put(StreamsConfig.CLIENT_ID_CONFIG, clientId);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        // Sikkerhet - miljø eller lokal
        if (isDeployment) {
            props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SecurityProtocol.SSL.name);
            props.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "");
            props.put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, "jks");
            props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, trustStorePath);
            props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, credStorePassword);
            props.put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, "PKCS12");
            props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, keyStoreLocation);
            props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, credStorePassword);
        } else {
            props.setProperty(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SecurityProtocol.SASL_SSL.name);
            props.setProperty(SaslConfigs.SASL_MECHANISM, "PLAIN");
            String jaasTemplate = "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\";";
            String jaasCfg = String.format(jaasTemplate, "vtp", "vtp");
            props.setProperty(SaslConfigs.SASL_JAAS_CONFIG, jaasCfg);
        }

        // Serde
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, topic.serdeKey().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, topic.serdeValue().getClass());
        props.put(StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG, LogAndFailExceptionHandler.class);

        // Polling
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "200");
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "60000");

        return props;
    }

    private Topic<String, JournalfoeringHendelseRecord> createConfiguredTopic(String topicName, String schemaRegistryUrl,
                                                                              String basicAuth) {
        var configuredTopic = new Topic<>(topicName, Serdes.String(), getSerde());
        if (schemaRegistryUrl != null && !schemaRegistryUrl.isEmpty()) {
            var schemaMap =
                Map.of(AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl,
                    AbstractKafkaSchemaSerDeConfig.BASIC_AUTH_CREDENTIALS_SOURCE, "USER_INFO",
                    AbstractKafkaSchemaSerDeConfig.USER_INFO_CONFIG, basicAuth,
                    SPECIFIC_AVRO_READER_CONFIG, true);
            configuredTopic.serdeKey().configure(schemaMap, true);
            configuredTopic.serdeValue().configure(schemaMap, false);
        }
        return configuredTopic;
    }

    private String getBasicAuth(String schemaRegistryUsername, String schemaRegistryPassword) {
        return schemaRegistryUsername+":"+schemaRegistryPassword;
    }

    private Serde<JournalfoeringHendelseRecord> getSerde() {
        return isDeployment ? new SpecificAvroSerde<>() : new VtpKafkaAvroSerde<>();
    }

}
