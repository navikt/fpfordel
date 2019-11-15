package no.nav.foreldrepenger.fordel.web.app.selftest.checks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;

import org.junit.Test;

import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;

public class InfotrygdSakRestHealthCheckTest {

    @Test
    public void test_alt() {
        final URI uri = URI.create("http://test.foo");
        var restClient = mock(OidcRestClient.class);
        when(restClient.get(eq(uri), eq(String.class))).thenReturn("OK");
        InfotrygdSakRestHealthCheck check = new InfotrygdSakRestHealthCheck(restClient, uri);
        assertThat(check.getDescription()).isNotNull();
        assertThat(check.getEndpoint()).isEqualTo(uri.toString());
        check.performWebServiceSelftest();
        new InfotrygdSakRestHealthCheck();
    }
}
