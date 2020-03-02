package no.nav.foreldrepenger.mottak.felles.kafka;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.util.env.Environment;

@ApplicationScoped
public class HendelseProdusentSelektor {
    private static final Logger LOG = LoggerFactory.getLogger(HendelseProdusentSelektor.class);
    private static final Environment ENV = Environment.current();

    private Instance<HendelseProdusentProvider> instances;

    @Inject
    public HendelseProdusentSelektor(@Any Instance<HendelseProdusentProvider> instances) {
        this.instances = instances;
    }

    @Produces
    @ApplicationScoped
    public HendelseProdusent hendelseProdusent() {
        String namespace = ENV.namespace();
        if ("t4".equals(namespace)) {
            var instans = instances.select(LoggingHendelseProdusent.class);
            LOG.info("Bruker logging-instans {} i {}-{}", instans.getClass().getSimpleName(), ENV.clusterName(), namespace);
            return (Object hendelse, String nøkkel) -> instans.get().send(hendelse, nøkkel);
        } else {
            var kafkaInstans = instances.select(KafkaHendelseProdusent.class).get();
            LOG.info("Bruker kafka-instans {} i {}-{}", kafkaInstans.getClass().getSimpleName(), ENV.clusterName(), namespace);
            return (Object hendelse, String nøkkel) -> kafkaInstans.send(hendelse, nøkkel);
        }
    }

}
