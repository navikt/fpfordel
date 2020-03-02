package no.nav.foreldrepenger.mottak.felles.kafka;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import no.nav.vedtak.util.env.Environment;

@ApplicationScoped
public class HendelseProdusentSelektor {
    private static final Environment ENV = Environment.current();

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
            return instans.get();
        } else {
            return instances.select(HendelseProdusent.class, new EnvironmentAlternative.Literal(EnvironmentAlternative.DEFAULT)).get();
        }
    }

}
