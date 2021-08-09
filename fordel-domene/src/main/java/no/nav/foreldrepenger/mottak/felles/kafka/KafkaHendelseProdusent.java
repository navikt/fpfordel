package no.nav.foreldrepenger.mottak.felles.kafka;

import static no.nav.foreldrepenger.mottak.felles.kafka.KafkaProperties.properties;
import static no.nav.vedtak.log.mdc.MDCOperations.generateCallId;
import static no.nav.vedtak.log.mdc.MDCOperations.getCallId;

import java.util.Optional;
import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.vedtak.exception.TekniskException;

@ApplicationScoped
public class KafkaHendelseProdusent implements HendelseProdusent {

    private static final Logger LOG = LoggerFactory.getLogger(KafkaHendelseProdusent.class);

    private static final String CALLID_NAME = "Nav-CallId";
    private final Producer<String, String> producer;
    private static final ObjectMapper OM = new ObjectMapper();

    @Inject
    @KonfigVerdi("kafka.topics.fordeling")
    private String topic;

    public KafkaHendelseProdusent() {
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
        return new ProducerRecord<>(topic, null, nøkkel, jsonFra(objekt, KafkaFeil::kanIkkeSerialisere),
                new RecordHeaders().add(CALLID_NAME,
                        Optional.ofNullable(getCallId()).orElseGet(() -> generateCallId()).getBytes()));
    }

    private static String jsonFra(Object object, Function<JsonProcessingException, TekniskException> feilFactory) {
        try {
            return OM.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw feilFactory.apply(e);
        }
    }
}
