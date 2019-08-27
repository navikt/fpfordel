package no.nav.foreldrepenger.fordel.web.app.tjenester;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Iterator;

import javax.enterprise.inject.Instance;

import org.junit.Before;
import org.junit.Test;

import no.nav.vedtak.apptjeneste.AppServiceHandler;
import no.nav.vedtak.felles.testutilities.cdi.UnitTestInstanceImpl;

public class ApplicationServiceStarterImplTest {

    private ApplicationServiceStarter serviceStarter;

    private AppServiceHandler serviceMock = mock(AppServiceHandler.class);
    private Instance<AppServiceHandler> testInstance = new UnitTestInstanceImpl<>(serviceMock);
    private Instance<AppServiceHandler> instanceSpied = spy(testInstance);

    @SuppressWarnings("unchecked")
    private Iterator<AppServiceHandler> iteratorMock = mock(Iterator.class);

    @Before
    public void setup() {
        when(iteratorMock.hasNext()).thenReturn(true, false);
        when(iteratorMock.next()).thenReturn(serviceMock);
        doReturn(iteratorMock).when(instanceSpied).iterator();

        serviceStarter = new ApplicationServiceStarterImpl(instanceSpied);
    }

    @Test
    public void test_skal_kalle_AppServiceHandler_start_og_stop() {
        serviceStarter.startServices();
        serviceStarter.stopServices();

        verify(serviceMock).start();
        verify(serviceMock).start();
    }

}