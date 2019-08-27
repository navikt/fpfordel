package no.nav.foreldrepenger.fordel.web.app.selftest.checks;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.mottak.queue.MottakAsyncJmsConsumer;


@ApplicationScoped
public class MottakQueueHealthCheck extends QueueHealthCheck {

    MottakQueueHealthCheck() {
        // for CDI proxy
    }

    @Inject
    public MottakQueueHealthCheck(MottakAsyncJmsConsumer mottakAsyncJmsConsumer) {
        super(mottakAsyncJmsConsumer);
    }

    @Override
    protected String getDescriptionSuffix() {
        return "mottak";
    }

    @Override
    public boolean erKritiskTjeneste() {
        return false;
    }
}
