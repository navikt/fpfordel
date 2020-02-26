package no.nav.foreldrepenger.mottak.felles.kafka;

import static no.nav.foreldrepenger.mottak.felles.kafka.KafkaProperties.properties;
import static no.nav.vedtak.log.mdc.MDCOperations.generateCallId;
import static no.nav.vedtak.log.mdc.MDCOperations.getCallId;

import java.util.Optional;

import javax.inject.Inject;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//@ApplicationScoped
public class HendelseProdusent {

    private static final Logger LOG = LoggerFactory.getLogger(HendelseProdusent.class);

    private static final String CALLID_NAME = "Nav-CallId";
    private Producer<String, String> producer;
    private String topic;

    HendelseProdusent() {
    }

    @Inject
    public HendelseProdusent(String topic) {
        this.producer = new KafkaProducer<>(properties());
        this.topic = topic;
    }

    public void send(String json, String nøkkel) {
        producer.send(melding(json, nøkkel), new Callback() {
            @Override
            public void onCompletion(RecordMetadata md, Exception e) {
                if (e == null) {
                    LOG.info("Sendte melding {} med offset {} på {}", json, md.offset(), md.topic());
                } else {
                    LOG.warn("Kunne ikke sende melding {} på {}", json, md.topic(), e);
                }
            }
        });
    }

    public ProducerRecord<String, String> melding(String json, String nøkkel) {
       return new ProducerRecord<>(topic, null, nøkkel, json,
                new RecordHeaders().add(CALLID_NAME, Optional.ofNullable(getCallId()).orElse(generateCallId()).getBytes()));
    }
}
