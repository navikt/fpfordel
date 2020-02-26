package no.nav.foreldrepenger.mottak.felles.kafka;

import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named("dev-fss")
public class LoggingHendelseProdusent implements HendelseProdusent {
    private static final Logger LOG = LoggerFactory.getLogger(LoggingHendelseProdusent.class);

    @Override
    public void send(Object hendelse, String nøkkel) {
        LOG.info("Publiserer hendelse {} med nøkkel {}", hendelse, nøkkel);
    }
}
