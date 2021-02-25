package no.nav.foreldrepenger.mottak.felles.kafka;

public interface HendelseProdusent {

    void send(Object hendelse, String nøkkel);

}
