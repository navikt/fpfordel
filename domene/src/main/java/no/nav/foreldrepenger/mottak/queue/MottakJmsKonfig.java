package no.nav.foreldrepenger.mottak.queue;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import no.nav.vedtak.felles.integrasjon.jms.BaseJmsKonfig;

@Named("mottak")
@ApplicationScoped
public class MottakJmsKonfig extends BaseJmsKonfig {

    public static final String JNDI_QUEUE = "jms/QueueMottak";

    private static final String QUEUE_PREFIX = "mottak_queue";

    public MottakJmsKonfig() {
        super(QUEUE_PREFIX);
    }
}
