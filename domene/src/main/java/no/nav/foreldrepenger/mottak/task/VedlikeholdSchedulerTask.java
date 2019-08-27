package no.nav.foreldrepenger.mottak.task;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.MonthDay;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskGruppe;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskStatus;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTypeInfo;
import no.nav.vedtak.util.FPDateUtil;

/**
 * Enkel scheduler for dagens situasjon der man kjører mandag-fredag og det er noe variasjon i parametere.
 *
 * Denne vil p.t. planlegge neste instans og sette alle feilete tasks til klar.
 *
 * Den bør utvides med sletting av prosesstasks, dokument og dokument_metadata. Da kan man låne BatchRunner fra fpsak
 *
 * For månedlige oppgaver - se FC's crom-bibliotek på GitHub for spesifikasjon
 */
@ApplicationScoped
@ProsessTask(VedlikeholdSchedulerTask.TASKTYPE)
public class VedlikeholdSchedulerTask implements ProsessTaskHandler {

    public static final String TASKTYPE = "vedlikehold.scheduler";

    private ProsessTaskRepository prosessTaskRepository;

    private final Set<MonthDay> fasteStengteDager = Set.of(
        MonthDay.of(1, 1),
        MonthDay.of(5, 1),
        MonthDay.of(5, 17),
        MonthDay.of(12, 25),
        MonthDay.of(12, 26),
        MonthDay.of(12, 31)
    );

    VedlikeholdSchedulerTask() {
        // for CDI proxy
    }

    @Inject
    public VedlikeholdSchedulerTask(ProsessTaskRepository prosessTaskRepository) {
        this.prosessTaskRepository = prosessTaskRepository;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        LocalDate dagensDato = FPDateUtil.iDag();
        DayOfWeek dagensUkedag = DayOfWeek.from(dagensDato);

        // Lagre neste instans av daglig scheduler straks over midnatt
        ProsessTaskData batchScheduler = new ProsessTaskData(VedlikeholdSchedulerTask.TASKTYPE);
        LocalDateTime nesteScheduler = dagensDato.plusDays(1).atStartOfDay().plusHours(7).plusMinutes(1);
        batchScheduler.setNesteKjøringEtter(nesteScheduler);
        ProsessTaskGruppe gruppeScheduler = new ProsessTaskGruppe(batchScheduler);
        prosessTaskRepository.lagre(gruppeScheduler);

        // Ingenting å kjøre i helgene enn så lenge
        if (DayOfWeek.FRIDAY.getValue() < dagensUkedag.getValue() || erFastInfotrygdStengtDag(dagensDato)) {
            return;
        }

        retryAlleProsessTasksFeilet();
    }

    private void retryAlleProsessTasksFeilet() {
        List<ProsessTaskData> ptdList = this.prosessTaskRepository.finnAlle(ProsessTaskStatus.FEILET);
        if (ptdList.isEmpty()) {
            return;
        }
        LocalDateTime nå = FPDateUtil.nå();
        Map<String, Integer> taskTypesMaxForsøk = new HashMap<>();
        ptdList.stream().map(ProsessTaskData::getTaskType).forEach(tasktype -> {
            if (taskTypesMaxForsøk.get(tasktype) == null) {
                int forsøk = prosessTaskRepository.finnProsessTaskType(tasktype).map(ProsessTaskTypeInfo::getMaksForsøk).orElse(1);
                taskTypesMaxForsøk.put(tasktype, forsøk);
            }
        });
        ptdList.forEach((ptd) -> {
            ptd.setStatus(ProsessTaskStatus.KLAR);
            ptd.setNesteKjøringEtter(nå);
            ptd.setSisteFeilKode(null);
            ptd.setSisteFeil(null);
            if (taskTypesMaxForsøk.get(ptd.getTaskType()).equals(ptd.getAntallFeiledeForsøk())) {
                ptd.setAntallFeiledeForsøk(ptd.getAntallFeiledeForsøk() - 1);
            }
            this.prosessTaskRepository.lagre(ptd);
        });
    }

    private boolean erFastInfotrygdStengtDag(LocalDate dato) {
        return fasteStengteDager.contains(MonthDay.from(dato));
    }
}
