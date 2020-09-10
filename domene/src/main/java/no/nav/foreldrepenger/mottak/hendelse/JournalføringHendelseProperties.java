package no.nav.foreldrepenger.mottak.hendelse;

import java.util.Properties;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.errors.LogAndFailExceptionHandler;

import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord;
import no.nav.vedtak.konfig.KonfigVerdi;
import no.nav.vedtak.util.env.Environment;

@Dependent
public class JournalføringHendelseProperties {
    private static final Environment ENV = Environment.current();
    private final String bootstrapServers;
    private final String schemaRegistryUrl;
    private final Topic<String, JournalfoeringHendelseRecord> journalfoeringHendelseTopic;
    private final String username;
    private final String password;
    private String applicationId;
    private final String trustStorePath;
    private final String trustStorePassword;

    @Inject
    public JournalføringHendelseProperties(@KonfigVerdi("kafka.bootstrap.servers") String bootstrapServers,
            @KonfigVerdi("kafka.schema.registry.url") String schemaRegistry,
            @KonfigVerdi("systembruker.username") String username,
            @KonfigVerdi("systembruker.password") String password,
            @KonfigVerdi(value = "javax.net.ssl.trustStore") String trustStorePath,
            @KonfigVerdi(value = "javax.net.ssl.trustStorePassword") String trustStorePassword,
            @KonfigVerdi("kafka.topic.journal.hendelse") String topic) {
        this.journalfoeringHendelseTopic = new Topic<>(topic, Serdes.String(), new SpecificAvroSerde<>());
        this.bootstrapServers = bootstrapServers;
        this.schemaRegistryUrl = schemaRegistry;
        this.username = username;
        this.password = password;
        this.trustStorePath = trustStorePath;
        this.trustStorePassword = trustStorePassword;
        this.applicationId = applicationId();
    }

    private static String applicationId() {
        String prefix = ENV.getProperty("nais.app.name", "fpfordel");
        if (ENV.isProd()) {
            return prefix + "-default";
        }
        return prefix + "-" + ENV.namespace();
    }

    public Topic<String, JournalfoeringHendelseRecord> getTopic() {
        return journalfoeringHendelseTopic;
    }

    public String getSchemaRegistryUrl() {
        return schemaRegistryUrl;
    }

    String getBootstrapServers() {
        return bootstrapServers;
    }

    String getUsername() {
        return username;
    }

    String getPassword() {
        return password;
    }

    boolean harSattBrukernavn() {
        return (username != null) && !username.isEmpty();
    }

    String getApplicationId() {
        return applicationId;
    }

    private boolean harSattTrustStore() {
        return (trustStorePath != null) && !trustStorePath.isEmpty()
                && (trustStorePassword != null) && !trustStorePassword.isEmpty();
    }

    private String getTrustStorePath() {
        return trustStorePath;
    }

    private String getTrustStorePassword() {
        return trustStorePassword;
    }

    public Properties getProperties() {
        final Properties props = new Properties();

        /*
         * Application ID må være unik per strøm for å unngå en feilsituasjon der man
         * enkelte ganger får feil partition (dvs partitions fra annen topic enn den man
         * skal ha).
         */
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, getApplicationId() + "-" + journalfoeringHendelseTopic.getConsumerClientId());
        props.put(StreamsConfig.CLIENT_ID_CONFIG, journalfoeringHendelseTopic.getConsumerClientId());
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, getBootstrapServers());

        // Sikkerhet
        if (harSattBrukernavn()) {
            props.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
            props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");
            String jaasTemplate = "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\";";
            props.put(SaslConfigs.SASL_JAAS_CONFIG, String.format(jaasTemplate, getUsername(), getPassword()));
        }

        if (harSattTrustStore()) {
            props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
            props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, getTrustStorePath());
            props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, getTrustStorePassword());
        }

        // Setup schema-registry
        if (getSchemaRegistryUrl() != null) {
            props.put("schema.registry.url", getSchemaRegistryUrl());
        }

        // Serde
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, journalfoeringHendelseTopic.getSerdeKey().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, journalfoeringHendelseTopic.getSerdeValue().getClass());
        props.put(StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG, LogAndFailExceptionHandler.class);

        return props;
    }

}
