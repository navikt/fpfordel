package no.nav.foreldrepenger.mottak.felles.kafka;

import static no.nav.foreldrepenger.mottak.felles.kafka.EnvironmentAlternative.DEFAULT;

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
    private static final Environment ENV = Environment.current();
    private static final Logger LOG = LoggerFactory.getLogger(HendelseProdusentSelektor.class);

    private Instance<HendelseProdusent> instances;

    @Inject
    public HendelseProdusentSelektor(@Any Instance<HendelseProdusent> instances) {
        this.instances = instances;
    }

    @Produces
    @ApplicationScoped
    public HendelseProdusent hendelseProdusent() {
        String namespace = ENV.namespace();
        var instans = instances.select(HendelseProdusent.class, new EnvironmentAlternative.Literal(namespace));
        if (instans.isResolvable()) {
            LOG.info("Bruker eneste kandidat {} i {}-{}", instans.get().getClass().getSimpleName(), ENV.clusterName(),
                    ENV.namespace());
            return instans.get();
        }
        var valgt = instances.select(HendelseProdusent.class, new EnvironmentAlternative.Literal(DEFAULT)).get();
        LOG.info("Bruker  kandidat {} i {}-{}", valgt.getClass().getSimpleName(), ENV.clusterName(),
                ENV.namespace());
        return valgt;
    }
}
