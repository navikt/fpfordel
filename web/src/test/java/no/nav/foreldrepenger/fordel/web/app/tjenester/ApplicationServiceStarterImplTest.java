package no.nav.foreldrepenger.fordel.web.app.tjenester;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.vedtak.log.metrics.Controllable;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceStarterImplTest {

    private ApplicationServiceStarter serviceStarter;

    @Mock
    private Controllable service;

    @BeforeEach
    public void setup() {
        serviceStarter = new ApplicationServiceStarter(service);
    }

    @Test
    void test_skal_kalle_AppServiceHandler_start_og_stop() {
        serviceStarter.startServices();
        serviceStarter.stopServices();
        verify(service).start();
        verify(service).stop();
    }

}
