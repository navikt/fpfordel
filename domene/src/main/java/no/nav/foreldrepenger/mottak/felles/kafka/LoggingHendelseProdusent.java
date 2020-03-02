package no.nav.foreldrepenger.mottak.felles.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//@ApplicationScoped
public class LoggingHendelseProdusent implements HendelseProdusent {
    private static final Logger LOG = LoggerFactory.getLogger(LoggingHendelseProdusent.class);

    @Override
    public void send(Object hendelse, String nøkkel) {
        LOG.info("Publiserer hendelse {} med nøkkel {} på topic {}", hendelse, nøkkel);
    }

}
