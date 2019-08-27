package no.nav.foreldrepenger.fordel.web.app.selftest.checks;

import no.nav.foreldrepenger.fordel.web.app.selftest.checks.OppgavebehandlingWebServiceHealthCheck;
import no.nav.vedtak.felles.integrasjon.behandleoppgave.BehandleoppgaveSelftestConsumer;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OppgavebehandlingWebServiceHealthCheckTest {

    @Test
    public void test_alt() {
        final String ENDPT = "http://test.foo";
        BehandleoppgaveSelftestConsumer mockSelftestConsumer = mock(BehandleoppgaveSelftestConsumer.class);
        when(mockSelftestConsumer.getEndpointUrl()).thenReturn(ENDPT);
        OppgavebehandlingWebServiceHealthCheck check = new OppgavebehandlingWebServiceHealthCheck(mockSelftestConsumer);

        assertThat(check.getDescription()).isNotNull();

        assertThat(check.getEndpoint()).isEqualTo(ENDPT);

        check.performWebServiceSelftest();

        new OppgavebehandlingWebServiceHealthCheck(); // som trengs av CDI
    }
}
