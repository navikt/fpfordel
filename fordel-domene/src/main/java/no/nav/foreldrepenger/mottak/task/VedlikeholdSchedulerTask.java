package no.nav.foreldrepenger.mottak.task;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.MonthDay;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.vedtak.felles.prosesstask.api.ProsessTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskGruppe;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;

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
@ProsessTask("vedlikehold.scheduler")
public class VedlikeholdSchedulerTask implements ProsessTaskHandler {

    private ProsessTaskTjeneste prosessTaskTjeneste;

    private static final Set<MonthDay> FASTE_STENGTE_DAGER = Set.of(
            MonthDay.of(1, 1),
            MonthDay.of(5, 1),
            MonthDay.of(5, 17),
            MonthDay.of(12, 25),
            MonthDay.of(12, 26),
            MonthDay.of(12, 31));

    VedlikeholdSchedulerTask() {
        // CDI
    }

    @Inject
    public VedlikeholdSchedulerTask(ProsessTaskTjeneste prosessTaskTjeneste) {
        this.prosessTaskTjeneste = prosessTaskTjeneste;
    }

    @Override
    public void doTask(ProsessTaskData prosessTaskData) {
        var dagensDato = LocalDate.now();
        // Lagre neste instans av daglig scheduler straks over midnatt
        var batchScheduler = ProsessTaskData.forProsessTaskHandler(VedlikeholdSchedulerTask.class);
        var nesteScheduler = dagensDato.plusDays(1).atStartOfDay().plusHours(7).plusMinutes(1);
        batchScheduler.setNesteKjøringEtter(nesteScheduler);
        var gruppeScheduler = new ProsessTaskGruppe(batchScheduler);
        prosessTaskTjeneste.lagre(gruppeScheduler);

        // Ingenting å kjøre i helgene enn så lenge
        if ((DayOfWeek.FRIDAY.getValue() < DayOfWeek.from(dagensDato).getValue()) || erFastInfotrygdStengtDag(dagensDato)) {
            return;
        }

        retryAlleProsessTasksFeilet();
    }

    private void retryAlleProsessTasksFeilet() {
        prosessTaskTjeneste.flaggAlleFeileteProsessTasksForRestart();
    }

    private static boolean erFastInfotrygdStengtDag(LocalDate dato) {
        return FASTE_STENGTE_DAGER.contains(MonthDay.from(dato));
    }
}
