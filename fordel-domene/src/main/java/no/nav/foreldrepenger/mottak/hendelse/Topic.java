package no.nav.foreldrepenger.mottak.hendelse;

import java.util.Objects;

import org.apache.kafka.common.serialization.Serde;

public record Topic<K, V>(String topic, Serde<K> serdeKey, Serde<V> serdeValue) {

    public Topic {
        Objects.requireNonNull(topic, "topic");
        Objects.requireNonNull(serdeKey, "serdeKey");
        Objects.requireNonNull(serdeValue, "serdeValue");
    }

    /**
     * Genererer clientId basert på standard definert på
     * https://confluence.adeo.no/display/AURA/Kafka#Kafka-TopicogSikkerhetskonfigurasjon
     *
     * @return clientId
     */
    public String getConsumerClientId() {
        return "KC-" + topic;
    }

    @Override
    public String toString() {
        return "Topic{" +
                "topic='" + topic + '\'' +
                ", serdeKey=" + serdeKey +
                ", serdeValue=" + serdeValue +
                '}';
    }
}
