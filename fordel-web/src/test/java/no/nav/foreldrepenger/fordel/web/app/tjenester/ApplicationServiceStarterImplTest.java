package no.nav.foreldrepenger.fordel.web.app.tjenester;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Iterator;

import javax.enterprise.inject.Instance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import no.nav.vedtak.apptjeneste.AppServiceHandler;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ApplicationServiceStarterImplTest {

    private ApplicationServiceStarter serviceStarter;

    @Mock
    private AppServiceHandler service;
    @Mock
    private Instance<AppServiceHandler> instance;
    @Mock
    private Iterator<AppServiceHandler> iterator;

    @BeforeEach
    public void setup() {
        when(instance.iterator()).thenReturn(iterator);
        when(instance.get()).thenReturn(service);
        when(iterator.hasNext()).thenReturn(true, false);
        when(iterator.next()).thenReturn(service);
        serviceStarter = new ApplicationServiceStarter(instance);
    }

    @Test
    public void test_skal_kalle_AppServiceHandler_start_og_stop() {
        serviceStarter.startServices();
        serviceStarter.stopServices();
        verify(service).start();
        verify(service).stop();
    }

}