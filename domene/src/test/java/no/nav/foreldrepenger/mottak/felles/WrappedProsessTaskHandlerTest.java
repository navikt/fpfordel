package no.nav.foreldrepenger.mottak.felles;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;

import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

public class WrappedProsessTaskHandlerTest {

    private WrappedProsessTaskHandler wrappedProsessTaskHandler; // objektet vi tester

    private ProsessTaskRepository mockProsessTaskRepository;
    private ProsessTaskData prosessTaskData;
    private MottakMeldingDataWrapper returnedDataWrapper;

    @Before
    public void setup() {
        mockProsessTaskRepository = mock(ProsessTaskRepository.class);
        wrappedProsessTaskHandler = new MyWrappedProsessTaskHandler(mockProsessTaskRepository);
        prosessTaskData = new ProsessTaskData("type");
        returnedDataWrapper = null;
    }

    @Test
    public void test_doTask_returnedWrapper() {
        returnedDataWrapper = new MottakMeldingDataWrapper(prosessTaskData);

        wrappedProsessTaskHandler.doTask(prosessTaskData);

        verify(mockProsessTaskRepository).lagre(prosessTaskData);
    }

    @Test
    public void test_doTask_returnedNull() {
        wrappedProsessTaskHandler.doTask(prosessTaskData);

        verify(mockProsessTaskRepository, never()).lagre(any(ProsessTaskData.class));
    }

    // -------

    private class MyWrappedProsessTaskHandler extends WrappedProsessTaskHandler {

        MyWrappedProsessTaskHandler(ProsessTaskRepository prosessTaskRepository) {
            super(prosessTaskRepository);
        }

        @Override
        public void precondition(MottakMeldingDataWrapper dataWrapper) {
            // Alt er OK her :)
        }

        @Override
        public MottakMeldingDataWrapper doTask(MottakMeldingDataWrapper dataWrapper) {
            return returnedDataWrapper;
        }
    }
}
