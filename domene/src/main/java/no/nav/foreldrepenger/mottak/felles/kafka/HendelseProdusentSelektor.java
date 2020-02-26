package no.nav.foreldrepenger.mottak.felles.kafka;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HendelseProdusentSelektor {
    private static final Logger LOG = LoggerFactory.getLogger(HendelseProdusentSelektor.class);
    private String type;

    @Produces
    @ApplicationScoped
    HendelseProdusent hentProdusent(Instance<HendelseProdusent> produsenter) {
        var iter = produsenter.iterator();
        while (iter.hasNext()) {
            LOG.info("Kandidat er {}", iter.next());
        }
        // Set<HendelseProdusent> candidates = CDI.current().getBeanManager().get
        return new LoggingHendelseProdusent();
        // return produsenter.select(new NamedLiteral(parameter)).get();
    }
}
