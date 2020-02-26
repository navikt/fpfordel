package no.nav.foreldrepenger.mottak.felles.kafka;

import javax.enterprise.context.ApplicationScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
public class LoggingHendelseProdusent implements HendelseProdusent {
    private static final Logger LOG = LoggerFactory.getLogger(LoggingHendelseProdusent.class);
    private final String topic;

    public LoggingHendelseProdusent(@KonfigVerdi("kafka.topics.fordeling") String topic) {
        this.topic = topic;
    }

    @Override
    public void send(Object hendelse, String nøkkel) {
        LOG.info("Publiserer hendelse {} med nøkkel {} på topic {}", hendelse, nøkkel, topic);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[topic=" + topic + "]";
    }
}
