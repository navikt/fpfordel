package no.nav.foreldrepenger.mottak.felles.kafka;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.literal.NamedLiteral;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.util.env.Environment;

public class HendelseProdusentSelektor {
    private static final Logger LOG = LoggerFactory.getLogger(HendelseProdusentSelektor.class);
    private static final String CURRENT_CLUSTER = Environment.current().clusterName();

    // @Produces
    // @ApplicationScoped
    HendelseProdusent hendelseProdusent(Instance<HendelseProdusent> instance) {
        if (instance.isResolvable()) {
            return instance.get();
        }
        if (instance.isAmbiguous()) {
            return instance.select(NamedLiteral.of(CURRENT_CLUSTER)).get();
        }
        throw new IllegalStateException("Ingen instanser : " + instance);
    }
}
