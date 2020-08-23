package no.nav.foreldrepenger.mottak.felles.kafka;

public interface KafkaIntegration {

    /**
     * Er integrasjonen i live.
     *
     * @return true / false
     */
    boolean isAlive();
}
