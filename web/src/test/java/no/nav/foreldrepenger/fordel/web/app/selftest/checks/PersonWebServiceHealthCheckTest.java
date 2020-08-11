package no.nav.foreldrepenger.fordel.web.app.selftest.checks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import no.nav.vedtak.felles.integrasjon.person.PersonSelftestConsumer;

public class PersonWebServiceHealthCheckTest {

    @Test
    public void test_alt() {
        final String ENDPT = "http://test.foo";
        PersonSelftestConsumer mockSelftestConsumer = mock(PersonSelftestConsumer.class);
        when(mockSelftestConsumer.getEndpointUrl()).thenReturn(ENDPT);
        PersonWebServiceHealthCheck check = new PersonWebServiceHealthCheck(mockSelftestConsumer);

        assertThat(check.getDescription()).isNotNull();

        assertThat(check.getEndpoint()).isEqualTo(ENDPT);

        check.performWebServiceSelftest();

        new PersonWebServiceHealthCheck(); // som trengs av CDI
    }
}
