package no.nav.foreldrepenger.mottak.felles.kafka;

import static org.apache.kafka.clients.admin.AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.admin.AdminClientConfig.CLIENT_ID_CONFIG;
import static org.apache.kafka.clients.admin.AdminClientConfig.SECURITY_PROTOCOL_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.common.config.SaslConfigs.SASL_JAAS_CONFIG;
import static org.apache.kafka.common.config.SaslConfigs.SASL_MECHANISM;

import java.util.Properties;

import org.apache.kafka.common.serialization.StringSerializer;

import no.nav.foreldrepenger.konfig.Environment;

final class KafkaProperties {
    private static final String KAFKA = "kafka.";
    private static final Environment ENV = Environment.current();

    private KafkaProperties() {
    }

    static Properties properties() {
        Properties properties = new Properties();
        properties.setProperty(BOOTSTRAP_SERVERS_CONFIG, getRequiredProperty(BOOTSTRAP_SERVERS_CONFIG));
        properties.setProperty(CLIENT_ID_CONFIG, getRequiredProperty(CLIENT_ID_CONFIG));
        properties.setProperty(SASL_JAAS_CONFIG, jaasCfg());
        properties.setProperty(SECURITY_PROTOCOL_CONFIG, getProperty(SECURITY_PROTOCOL_CONFIG, "SASL_SSL"));
        properties.setProperty(SASL_MECHANISM, getProperty(SASL_MECHANISM, "PLAIN"));
        properties.setProperty(KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        return properties;
    }

    private static String jaasCfg() {
        return String.format(
                "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\";",
                ENV.getRequiredProperty("systembruker.username"),
                ENV.getRequiredProperty("systembruker.password"));

    }

    private static String getRequiredProperty(String key) {
        return ENV.getRequiredProperty(KAFKA + key);
    }

    private static String getProperty(String key, String defaultVerdi) {
        return ENV.getProperty(KAFKA + key, defaultVerdi);
    }

}
