package no.nav.foreldrepenger.mottak.hendelse;


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

import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
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
    private final Topic<String, JournalfoeringHendelseRecord> journalfoeringHendelseTopic;
    private final String schemaRegistryUrl;
    private final String schemaRegistryUsername;
    private final String schemaRegistryPassword;
    private final boolean isDeployment = ENV.isProd() || ENV.isDev();


    @SuppressWarnings("resource")
    @Inject
    JournalHendelseProperties(@KonfigVerdi(value = "KAFKA_JOURNAL_TOPIC") String topicName,
                              @KonfigVerdi(value = "KAFKA_BROKERS") String bootstrapServers,
                              @KonfigVerdi(value = "KAFKA_SCHEMA_REGISTRY") String schemaRegistryUrl,
                              @KonfigVerdi(value = "KAFKA_SCHEMA_REGISTRY_USER") String schemaRegistryUsername,
                              @KonfigVerdi(value = "KAFKA_SCHEMA_REGISTRY_PASSWORD") String schemaRegistryPassword,
                              @KonfigVerdi(value = "KAFKA_TRUSTSTORE_PATH", required = false) String trustStorePath,
                              @KonfigVerdi(value = "KAFKA_KEYSTORE_PATH", required = false) String keyStoreLocation,
                              @KonfigVerdi(value = "KAFKA_CREDSTORE_PASSWORD", required = false) String credStorePassword) {
        this.trustStorePath = trustStorePath;
        this.keyStoreLocation = keyStoreLocation;
        this.credStorePassword = credStorePassword;
        this.journalfoeringHendelseTopic = new Topic<>(topicName, Serdes.String(), getSerde());
        this.applicationId = "fpfordel";
        this.clientId = "fpfordel";
        this.bootstrapServers = bootstrapServers;
        this.schemaRegistryUrl = schemaRegistryUrl;
        this.schemaRegistryUsername = schemaRegistryUsername;
        this.schemaRegistryPassword = schemaRegistryPassword;
    }

    public Topic<String, JournalfoeringHendelseRecord> getTopic() {
            return journalfoeringHendelseTopic;
    }

    String getSchemaRegistryUrl() {
        return schemaRegistryUrl;
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
            props.put(KafkaAvroDeserializerConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl);
            props.put(KafkaAvroDeserializerConfig.BASIC_AUTH_CREDENTIALS_SOURCE, "USER_INFO");
            props.put(KafkaAvroDeserializerConfig.USER_INFO_CONFIG, schemaRegistryUsername+":"+schemaRegistryPassword);
        } else {
            props.setProperty(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SecurityProtocol.SASL_SSL.name);
            props.setProperty(SaslConfigs.SASL_MECHANISM, "PLAIN");
            String jaasTemplate = "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\";";
            String jaasCfg = String.format(jaasTemplate, "vtp", "vtp");
            props.setProperty(SaslConfigs.SASL_JAAS_CONFIG, jaasCfg);
        }

        // Serde
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, journalfoeringHendelseTopic.serdeKey().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, journalfoeringHendelseTopic.serdeValue().getClass());
        props.put(StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG, LogAndFailExceptionHandler.class);

        // Polling
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "200");
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "60000");

        return props;
    }

    private Serde<JournalfoeringHendelseRecord> getSerde() {
        return isDeployment ? new SpecificAvroSerde<>() : new VtpKafkaAvroSerde<>();
    }

}
