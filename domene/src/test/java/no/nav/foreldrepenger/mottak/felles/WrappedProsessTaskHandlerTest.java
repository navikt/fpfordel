package no.nav.foreldrepenger.mottak.felles;

import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;
import no.nav.vedtak.felles.prosesstask.api.TaskType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WrappedProsessTaskHandlerTest {

    private WrappedProsessTaskHandler wrappedProsessTaskHandler;

    @Mock
    private ProsessTaskTjeneste taskTjenesteMock;
    private ProsessTaskData prosessTaskData;
    private MottakMeldingDataWrapper returnedDataWrapper;

    @BeforeEach
    void setup() {
        wrappedProsessTaskHandler = new MyWrappedProsessTaskHandler(taskTjenesteMock);
        prosessTaskData = ProsessTaskData.forTaskType(new TaskType("type"));
        returnedDataWrapper = null;
    }

    @Test
    void test_doTask_returnedWrapper() {
        returnedDataWrapper = new MottakMeldingDataWrapper(prosessTaskData);
        wrappedProsessTaskHandler.doTask(prosessTaskData);
        verify(taskTjenesteMock).lagre(prosessTaskData);
    }

    @Test
    void test_doTask_returnedNull() {
        wrappedProsessTaskHandler.doTask(prosessTaskData);
        verify(taskTjenesteMock, never()).lagre(any(ProsessTaskData.class));
    }

    private class MyWrappedProsessTaskHandler extends WrappedProsessTaskHandler {

        MyWrappedProsessTaskHandler(ProsessTaskTjeneste taskTjeneste) {
            super(taskTjeneste);
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
