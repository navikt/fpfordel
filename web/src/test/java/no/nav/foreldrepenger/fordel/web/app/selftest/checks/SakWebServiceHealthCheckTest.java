package no.nav.foreldrepenger.fordel.web.app.selftest.checks;

import no.nav.foreldrepenger.fordel.web.app.selftest.checks.SakWebServiceHealthCheck;
import no.nav.vedtak.felles.integrasjon.sak.SakSelftestConsumer;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SakWebServiceHealthCheckTest {

    @Test
    public void test_alt() {
        final String ENDPT = "http://test.foo";
        SakSelftestConsumer mockSelftestConsumer = mock(SakSelftestConsumer.class);
        when(mockSelftestConsumer.getEndpointUrl()).thenReturn(ENDPT);
        SakWebServiceHealthCheck check = new SakWebServiceHealthCheck(mockSelftestConsumer);

        assertThat(check.getDescription()).isNotNull();

        assertThat(check.getEndpoint()).isEqualTo(ENDPT);

        check.performWebServiceSelftest();

        new SakWebServiceHealthCheck(); // som trengs av CDI
    }
}
