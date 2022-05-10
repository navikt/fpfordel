package no.nav.foreldrepenger.mottak.hendelse;

import java.util.Objects;

import org.apache.kafka.common.serialization.Serde;

public record Topic<K, V>(String topic, Serde<K> serdeKey, Serde<V> serdeValue) {

    public Topic {
        Objects.requireNonNull(topic, "topic");
        Objects.requireNonNull(serdeKey, "serdeKey");
        Objects.requireNonNull(serdeValue, "serdeValue");
    }

}
