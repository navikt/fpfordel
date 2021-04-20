package no.nav.foreldrepenger.mottak.task;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.MonthDay;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskGruppe;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskStatus;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTypeInfo;

/**
 * Enkel scheduler for dagens situasjon der man kjører mandag-fredag og det er
 * noe variasjon i parametere.
 *
 * Denne vil p.t. planlegge neste instans og sette alle feilete tasks til klar.
 *
 * Den bør utvides med sletting av prosesstasks, dokument og dokument_metadata.
 * Da kan man låne BatchRunner fra fpsak
 *
 * For månedlige oppgaver - se FC's crom-bibliotek på GitHub for spesifikasjon
 */
@ApplicationScoped
@ProsessTask(VedlikeholdSchedulerTask.TASKTYPE)
public class VedlikeholdSchedulerTask implements ProsessTaskHandler {

    public static final String TASKTYPE = "vedlikehold.scheduler";

    private ProsessTaskRepository prosessTaskRepository;

    private static final Set<MonthDay> FASTE_STENGTE_DAGER = Set.of(
            MonthDay.of(1, 1),
            MonthDay.of(5, 1),
            MonthDay.of(5, 17),
            MonthDay.of(12, 25),
            MonthDay.of(12, 26),
            MonthDay.of(12, 31));

    VedlikeholdSchedulerTask() {
    }

    @Inject
    public VedlikeholdSchedulerTask(ProsessTaskRepository prosessTaskRepository) {
        this.prosessTaskRepository = prosessTaskRepository;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        var dagensDato = LocalDate.now();
        // Lagre neste instans av daglig scheduler straks over midnatt
        var batchScheduler = new ProsessTaskData(VedlikeholdSchedulerTask.TASKTYPE);
        var nesteScheduler = dagensDato.plusDays(1).atStartOfDay().plusHours(7).plusMinutes(1);
        batchScheduler.setNesteKjøringEtter(nesteScheduler);
        var gruppeScheduler = new ProsessTaskGruppe(batchScheduler);
        prosessTaskRepository.lagre(gruppeScheduler);

        // Ingenting å kjøre i helgene enn så lenge
        if ((DayOfWeek.FRIDAY.getValue() < DayOfWeek.from(dagensDato).getValue()) || erFastInfotrygdStengtDag(dagensDato)) {
            return;
        }

        retryAlleProsessTasksFeilet();
    }

    private void retryAlleProsessTasksFeilet() {
        var ptdList = this.prosessTaskRepository.finnAlle(ProsessTaskStatus.FEILET);
        if (ptdList.isEmpty()) {
            return;
        }
        resetTilStatusKlar(ptdList, tasktype -> prosessTaskRepository.finnProsessTaskType(tasktype).map(ProsessTaskTypeInfo::getMaksForsøk).orElse(1));
        ptdList.forEach(ptd -> this.prosessTaskRepository.lagre(ptd));
    }

    public static void resetTilStatusKlar(List<ProsessTaskData> tasks, Function<String, Integer> forsøkFinder) {
        LocalDateTime nå = LocalDateTime.now();
        Map<String, Integer> taskTypesMaxForsøk = new HashMap<>();
        tasks.stream().map(ProsessTaskData::getTaskType).forEach(tasktype -> {
            if (taskTypesMaxForsøk.get(tasktype) == null) {
                int forsøk = forsøkFinder.apply(tasktype);
                taskTypesMaxForsøk.put(tasktype, forsøk);
            }
        });
        tasks.forEach(ptd -> {
            ptd.setStatus(ProsessTaskStatus.KLAR);
            ptd.setNesteKjøringEtter(nå);
            ptd.setSisteFeilKode(null);
            ptd.setSisteFeil(null);
            if (taskTypesMaxForsøk.get(ptd.getTaskType()).equals(ptd.getAntallFeiledeForsøk())) {
                ptd.setAntallFeiledeForsøk(ptd.getAntallFeiledeForsøk() - 1);
            }
        });
    }

    private static boolean erFastInfotrygdStengtDag(LocalDate dato) {
        return FASTE_STENGTE_DAGER.contains(MonthDay.from(dato));
    }
}
