package no.nav.foreldrepenger.fordel.web.app.selftest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;

import no.nav.foreldrepenger.fordel.web.app.tjenester.ApplicationServiceStarter;

@SuppressWarnings("resource")
public class HealthCheckRestServiceTest {

    private HealthCheckRestService restTjeneste;

    private ApplicationServiceStarter serviceStarterMock = mock(ApplicationServiceStarter.class);
    private SelftestService selftestServiceMock = mock(SelftestService.class);

    @Before
    public void setup() {
        restTjeneste = new HealthCheckRestService(serviceStarterMock, selftestServiceMock);
    }

    @Test
    public void test_isAlive_skal_returnere_status_200() {
        Response response = restTjeneste.isAlive();

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void test_isReady_skal_returnere_service_unavailable_når_kritiske_selftester_feiler() {
        when(selftestServiceMock.kritiskTjenesteFeilet()).thenReturn(true);

        Response response = restTjeneste.isReady();

        assertThat(response.getStatus()).isEqualTo(Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
    }

    @Test
    public void test_isReady_skal_returnere_status_ok_når_selftester_er_ok() {
        when(selftestServiceMock.kritiskTjenesteFeilet()).thenReturn(false);

        Response response = restTjeneste.isReady();

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void test_preStop_skal_kalle_stopServices_og_returnere_status_ok() {
        Response response = restTjeneste.preStop();

        verify(serviceStarterMock).stopServices();
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }
}