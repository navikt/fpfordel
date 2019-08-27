package no.nav.foreldrepenger.fordel.web.app.selftest.checks;

import no.nav.foreldrepenger.fordel.web.app.selftest.checks.InfotrygdSakWebServiceHealthCheck;
import no.nav.vedtak.felles.integrasjon.infotrygdsak.InfotrygdSakSelftestConsumer;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GsakSakWebServiceHealthCheckTest {

    @Test
    public void test_alt() {

        final String ENDPT = "http://test.foo";
        InfotrygdSakSelftestConsumer mockSelftestConsumer = mock(InfotrygdSakSelftestConsumer.class);
        when(mockSelftestConsumer.getEndpointUrl()).thenReturn(ENDPT);
        InfotrygdSakWebServiceHealthCheck check = new InfotrygdSakWebServiceHealthCheck(mockSelftestConsumer);

        assertThat(check.getDescription()).isNotNull();

        assertThat(check.getEndpoint()).isEqualTo(ENDPT);

        check.performWebServiceSelftest();

        new InfotrygdSakWebServiceHealthCheck(); // som trengs av CDI
    }
}
