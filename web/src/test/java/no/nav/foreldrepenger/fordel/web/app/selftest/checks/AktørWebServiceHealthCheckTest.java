package no.nav.foreldrepenger.fordel.web.app.selftest.checks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørSelftestConsumer;

public class AktørWebServiceHealthCheckTest {

    @Test
    public void test_alt() {
        final String ENDPT = "http://test.foo";
        AktørSelftestConsumer mockSelftestConsumer = mock(AktørSelftestConsumer.class);
        when(mockSelftestConsumer.getEndpointUrl()).thenReturn(ENDPT);
        AktørWebServiceHealthCheck check = new AktørWebServiceHealthCheck(mockSelftestConsumer);

        assertThat(check.getDescription()).isNotNull();

        assertThat(check.getEndpoint()).isEqualTo(ENDPT);

        check.performWebServiceSelftest();

        new AktørWebServiceHealthCheck(); // som trengs av CDI
    }
}
