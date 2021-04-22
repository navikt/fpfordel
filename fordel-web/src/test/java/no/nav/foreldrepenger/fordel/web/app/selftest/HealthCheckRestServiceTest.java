package no.nav.foreldrepenger.fordel.web.app.selftest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import javax.ws.rs.core.Response;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.foreldrepenger.fordel.web.app.selftest.checks.DatabaseHealthCheck;
import no.nav.foreldrepenger.mottak.hendelse.JournalføringHendelseStream;

@ExtendWith(MockitoExtension.class)
public class HealthCheckRestServiceTest {

    private HealthCheckRestService restTjeneste;

    @Mock
    private JournalføringHendelseStream kafka;
    @Mock
    private DatabaseHealthCheck db;

    @BeforeEach
    public void setup() {
        restTjeneste = new HealthCheckRestService(List.of(db), List.of(kafka));
    }

    @Test
    public void test_isAlive_skal_returnere_status_200() {
        when(kafka.isAlive()).thenReturn(true);

        Response response = restTjeneste.isAlive();

        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void test_isReady_skal_returnere_service_unavailable_når_kritiske_selftester_feiler() {
        when(kafka.isAlive()).thenReturn(false);
        Response responseReady = restTjeneste.isReady();
        Response responseAlive = restTjeneste.isAlive();

        assertThat(responseReady.getStatus()).isEqualTo(Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
        assertThat(responseAlive.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Test
    public void test_isReady_skal_returnere_status_delvis_når_db_feiler() {
        when(kafka.isAlive()).thenReturn(true);
        when(db.isReady()).thenReturn(false);

        Response responseReady = restTjeneste.isReady();
        Response responseAlive = restTjeneste.isAlive();

        assertThat(responseReady.getStatus()).isEqualTo(Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
        assertThat(responseAlive.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

    @Test
    public void test_isReady_skal_returnere_status_ok_når_selftester_er_ok() {
        when(kafka.isAlive()).thenReturn(true);
        when(db.isReady()).thenReturn(true);

        Response responseReady = restTjeneste.isReady();
        Response responseAlive = restTjeneste.isAlive();

        assertThat(responseReady.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(responseAlive.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }
}