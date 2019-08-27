package no.nav.foreldrepenger.fordel.web.app.selftest.checks;

import no.nav.foreldrepenger.fordel.web.app.selftest.checks.InngaaendeJournalWebServiceHealthCheck;
import no.nav.vedtak.felles.integrasjon.inngaaendejournal.InngaaendeJournalSelftestConsumer;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InngaaendeJournalWebServiceHealthCheckTest {

    @Test
    public void test_alt() {
        final String ENDPT = "http://test.foo";
        InngaaendeJournalSelftestConsumer mockSelftestConsumer = mock(InngaaendeJournalSelftestConsumer.class);
        when(mockSelftestConsumer.getEndpointUrl()).thenReturn(ENDPT);
        InngaaendeJournalWebServiceHealthCheck check = new InngaaendeJournalWebServiceHealthCheck(mockSelftestConsumer);

        assertThat(check.getDescription()).isNotNull();

        assertThat(check.getEndpoint()).isEqualTo(ENDPT);

        check.performWebServiceSelftest();

        new InngaaendeJournalWebServiceHealthCheck(); // som trengs av CDI
    }
}
