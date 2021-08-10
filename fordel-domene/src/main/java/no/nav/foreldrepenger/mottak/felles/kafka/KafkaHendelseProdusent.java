package no.nav.foreldrepenger.mottak.felles.kafka;

import static no.nav.foreldrepenger.felles.integrasjon.rest.DefaultJsonMapper.MAPPER;
import static no.nav.foreldrepenger.mottak.felles.kafka.KafkaProperties.properties;
import static no.nav.vedtak.log.mdc.MDCOperations.HTTP_HEADER_CALL_ID;
import static no.nav.vedtak.log.mdc.MDCOperations.generateCallId;
import static no.nav.vedtak.log.mdc.MDCOperations.getCallId;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.vedtak.exception.TekniskException;
@ApplicationScoped
public class KafkaHendelseProdusent implements HendelseProdusent {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaHendelseProdusent.class);

    private final Producer<String, String> producer;

    private final String topic;


    @Inject
    public KafkaHendelseProdusent(@KonfigVerdi("kafka.topics.fordeling") String topic) {
        this.topic = topic;
        this.producer = new KafkaProducer<>(properties());
    }

    @Override
    public void send(Object objekt, String nøkkel) {
        LOG.info("Sender melding {}", objekt);
        producer.send(meldingFra(objekt, nøkkel), (md, e) -> {
            if (e == null) {
                LOG.info("Sendte melding {} med offset {} på {}", objekt, md.offset(), topic);
            } else {
                LOG.warn("Kunne ikke sende melding {} på {}", objekt, topic, e);
            }
        });
    }

    private ProducerRecord<String, String> meldingFra(Object objekt, String nøkkel) {
        return new ProducerRecord<>(topic, null, nøkkel, jsonFra(objekt),
                new RecordHeaders().add(HTTP_HEADER_CALL_ID,
                        Optional.ofNullable(getCallId()).orElseGet(() -> generateCallId()).getBytes()));
    }

    private static String jsonFra(Object object) {
        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new TekniskException("FP-190496", "Kunne ikke serialisere til json", e);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [producer=" + producer + ", topic=" + topic + "]";
    }
}
