package no.nav.foreldrepenger.mottak.felles.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.util.env.Cluster;

@ConditionalOnClusters(clusters = Cluster.DEV_FSS)
public class LoggingHendelseProdusent implements HendelseProdusent {
    private static final Logger LOG = LoggerFactory.getLogger(LoggingHendelseProdusent.class);

    @Override
    public void send(Object hendelse, String nøkkel) {
        LOG.info("Publiserer hendelse {} med nøkkel {}", hendelse, nøkkel);
    }
}
