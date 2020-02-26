package no.nav.foreldrepenger.mottak.felles.kafka;

import java.util.Arrays;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.util.env.Cluster;
import no.nav.vedtak.util.env.Environment;

public class HendelseProdusentSelektor {
    private static final Cluster CURRENT_CLUSTER = Environment.current().getCluster();
    private static final Logger LOG = LoggerFactory.getLogger(HendelseProdusentSelektor.class);

    @Produces
    @ApplicationScoped
    HendelseProdusent hentProdusent(Instance<HendelseProdusent> kandidater) {
        if (kandidater.isResolvable()) {
            HendelseProdusent kandidat = kandidater.select().get();
            LOG.info("Kun en kandidat {} er tilgjengelig", kandidat);
            return kandidat;
        }

        var iter = kandidater.iterator();
        while (iter.hasNext()) {
            var kandidat = iter.next();
            var clusters = Arrays
                    .asList(Optional.ofNullable(kandidat.getClass().getAnnotation(ConditionalOnClusters.class))
                            .map(ConditionalOnClusters::clusters).orElse(new Cluster[0]));

            if (clusters.contains(CURRENT_CLUSTER)) {
                LOG.info("Kandidat {} er annotert med {} og matcher current cluster {}", kandidat, clusters,
                        CURRENT_CLUSTER);
                return kandidat;
            } else {
                LOG.info("Kandidat {} er annotert med {} og matcher IKKE current cluster {}", kandidat, clusters,
                        CURRENT_CLUSTER);
            }
        }
        LOG.info("Ingen match via annoteringer, returnerer logging instans");
        return kandidater.select(LoggingHendelseProdusent.class).get();
    }
}
