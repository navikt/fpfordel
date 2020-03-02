package no.nav.foreldrepenger.mottak.felles.kafka;

public interface HendelseProdusentProvider {

    void send(Object hendelse, String nøkkel);

}
