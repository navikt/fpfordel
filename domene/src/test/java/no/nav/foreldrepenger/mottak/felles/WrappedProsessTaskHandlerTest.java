package no.nav.foreldrepenger.mottak.felles;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

@ExtendWith(MockitoExtension.class)
class WrappedProsessTaskHandlerTest {

    private WrappedProsessTaskHandler wrappedProsessTaskHandler;

    @Mock
    private ProsessTaskRepository mockProsessTaskRepository;
    private ProsessTaskData prosessTaskData;
    private MottakMeldingDataWrapper returnedDataWrapper;

    @BeforeEach
    void setup() {
        wrappedProsessTaskHandler = new MyWrappedProsessTaskHandler(mockProsessTaskRepository);
        prosessTaskData = new ProsessTaskData("type");
        returnedDataWrapper = null;
    }

    @Test
    void test_doTask_returnedWrapper() {
        returnedDataWrapper = new MottakMeldingDataWrapper(prosessTaskData);
        wrappedProsessTaskHandler.doTask(prosessTaskData);
        verify(mockProsessTaskRepository).lagre(prosessTaskData);
    }

    @Test
    void test_doTask_returnedNull() {
        wrappedProsessTaskHandler.doTask(prosessTaskData);
        verify(mockProsessTaskRepository, never()).lagre(any(ProsessTaskData.class));
    }

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
