package no.nav.foreldrepenger.mottak.felles;

import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

public abstract class WrappedProsessTaskHandler implements ProsessTaskHandler {

    protected ProsessTaskRepository prosessTaskRepository;

    public WrappedProsessTaskHandler(ProsessTaskRepository prosessTaskRepository) {
        this.prosessTaskRepository = prosessTaskRepository;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        MottakMeldingDataWrapper dataWrapper = new MottakMeldingDataWrapper(prosessTaskData);
        precondition(dataWrapper);

        MottakMeldingDataWrapper prosessTaskDataNesteMedDataFraInput = doTask(dataWrapper);

        if (prosessTaskDataNesteMedDataFraInput != null) {
            postcondition(prosessTaskDataNesteMedDataFraInput);
            prosessTaskRepository.lagre(prosessTaskDataNesteMedDataFraInput.getProsessTaskData());
        }
    }

    public abstract void precondition(MottakMeldingDataWrapper dataWrapper);

    public void postcondition(@SuppressWarnings("unused") MottakMeldingDataWrapper dataWrapper) {
        //Override i subtasks hvor det er krav til precondition. Det er typisk i tasker hvor tasken henter data og det er behov for å sjekke at alt er OK etter at task er kjørt.
    }

    public abstract MottakMeldingDataWrapper doTask(MottakMeldingDataWrapper dataWrapper);

    public static String metricMeterNameForProsessTasksFraTil(String fraTaskType, String tilTaskType) {
        return "mottak.tasks.fra." + fraTaskType + ".til." + tilTaskType;
    }
}
