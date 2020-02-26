package no.nav.foreldrepenger.mottak.felles.kafka;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
@Default
public class LoggingHendelseProdusent implements HendelseProdusent {
    private static final Logger LOG = LoggerFactory.getLogger(LoggingHendelseProdusent.class);

    @Inject
    @KonfigVerdi("kafka.topics.fordeling")
    private String topic;

    @Override
    public void send(Object hendelse, String nøkkel) {
        LOG.info("Publiserer hendelse {} med nøkkel {} på topic {}", hendelse, nøkkel, topic);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[topic=" + topic + "]";
    }
}
