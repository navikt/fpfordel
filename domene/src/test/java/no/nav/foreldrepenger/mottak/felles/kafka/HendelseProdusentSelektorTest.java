package no.nav.foreldrepenger.mottak.felles.kafka;

import javax.inject.Inject;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class HendelseProdusentSelektorTest {

    @Inject 
    private HendelseProdusent hendelseProdusent;

    @Test
    public void sjekk_får_injected_hendelseProdusent() {
        Assertions.assertThat(hendelseProdusent).isNotNull();
    }
}
