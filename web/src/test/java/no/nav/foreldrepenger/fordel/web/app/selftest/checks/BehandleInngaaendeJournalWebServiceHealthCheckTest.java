package no.nav.foreldrepenger.fordel.web.app.selftest.checks;

import no.nav.foreldrepenger.fordel.web.app.selftest.checks.BehandleInngaaendeJournalWebServiceHealthCheck;
import no.nav.foreldrepenger.fordel.web.app.selftest.checks.InngaaendeJournalWebServiceHealthCheck;
import no.nav.vedtak.felles.integrasjon.behandleinngaaendejournal.BehandleInngaaendeJournalSelftestConsumer;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BehandleInngaaendeJournalWebServiceHealthCheckTest {

    @Test
    public void test_alt() {
        final String ENDPT = "http://test.foo";
        BehandleInngaaendeJournalSelftestConsumer mockSelftestConsumer = mock(BehandleInngaaendeJournalSelftestConsumer.class);
        when(mockSelftestConsumer.getEndpointUrl()).thenReturn(ENDPT);
        BehandleInngaaendeJournalWebServiceHealthCheck check = new BehandleInngaaendeJournalWebServiceHealthCheck(mockSelftestConsumer);

        assertThat(check.getDescription()).isNotNull();

        assertThat(check.getEndpoint()).isEqualTo(ENDPT);

        check.performWebServiceSelftest();

        new InngaaendeJournalWebServiceHealthCheck(); // som trengs av CDI
    }
}
