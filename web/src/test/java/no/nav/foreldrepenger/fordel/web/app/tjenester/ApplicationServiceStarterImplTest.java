package no.nav.foreldrepenger.fordel.web.app.tjenester;

import no.nav.vedtak.apptjeneste.AppServiceHandler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ApplicationServiceStarterImplTest {

    private ApplicationServiceStarter serviceStarter;

    @Mock
    private AppServiceHandler service;

    @BeforeEach
    public void setup() {
        serviceStarter = new ApplicationServiceStarter(service);
    }

    @Test
    public void test_skal_kalle_AppServiceHandler_start_og_stop() {
        serviceStarter.startServices();
        serviceStarter.stopServices();
        verify(service).start();
        verify(service).stop();
    }

}
