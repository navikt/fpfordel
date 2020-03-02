package no.nav.foreldrepenger.mottak.felles.kafka;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.util.env.Environment;

public class HendelseProdusentSelektor {
    private static final Logger LOG = LoggerFactory.getLogger(HendelseProdusentSelektor.class);
    private static final Environment ENV = Environment.current();

    @Produces
    @ApplicationScoped
    HendelseProdusent hendelseProdusent(Instance<HendelseProdusent> instance) {
        LOG.info("Finner hendelsesprodusent-instans i {}", ENV.namespace());
        if ("t4".equals(ENV.namespace())) {
            var instans = instance.select(LoggingHendelseProdusent.class).get();
            LOG.info("Bruker logging-instans {} i {}-{}", instans.getClass().getSimpleName(), ENV.clusterName(),
                    ENV.namespace());
        }
        var instans = instance.select(KafkaHendelseProdusent.class).get();
        LOG.info("Bruker kafka-instans {} i {}-{}", instans.getClass().getSimpleName(), ENV.clusterName(),
                ENV.namespace());
        return instans;
    }
}
