package no.nav.foreldrepenger.fordel.web.app.selftest.checks;

import no.nav.foreldrepenger.fordel.web.app.selftest.checks.ArbeidsfordelingHealthCheck;
import no.nav.vedtak.felles.integrasjon.arbeidsfordeling.klient.ArbeidsfordelingSelftestConsumer;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ArbeidsfordelingHealthCheckTest {

    @Test
    public void test_alt() {
        final String ENDPT = "http://test.foo";
        ArbeidsfordelingSelftestConsumer mockSelftestConsumer = mock(ArbeidsfordelingSelftestConsumer.class);
        when(mockSelftestConsumer.getEndpointUrl()).thenReturn(ENDPT);
        ArbeidsfordelingHealthCheck check = new ArbeidsfordelingHealthCheck(mockSelftestConsumer);

        assertThat(check.getDescription()).isNotNull();

        assertThat(check.getEndpoint()).isEqualTo(ENDPT);

        check.performWebServiceSelftest();

        new ArbeidsfordelingHealthCheck(); // som trengs av CDI
    }
}
