package no.nav.foreldrepenger.fordel.web.app.rest;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.felles.prosesstask.api.ProsessTaskEvent;

@ApplicationScoped
public class ProsessTaskObserver {

    private static final Logger LOG = LoggerFactory.getLogger(ProsessTaskObserver.class);

    public void observerProssestask(@Observes ProsessTaskEvent event) {
        LOG.info("{} observert transisjon {}->{} ", event.getTaskType(), event.getGammelStatus(), event.getNyStatus());
    }
}
