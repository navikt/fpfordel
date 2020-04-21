package no.nav.foreldrepenger.metrikker;

import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import no.nav.vedtak.felles.prosesstask.api.ProsessTaskEvent;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskStatus;

@ApplicationScoped
class TaskStatusEventObserver {

    private SensuKlient sensuKlient;

    TaskStatusEventObserver() {
    }

    @Inject
    TaskStatusEventObserver(SensuKlient sensuKlient) {
        this.sensuKlient = sensuKlient;
    }

    void observerProsessTasks(@Observes ProsessTaskEvent event) {
        if (event.getGammelStatus() != null
                && ProsessTaskStatus.FEILET.equals(event.getGammelStatus())
                && ProsessTaskStatus.FEILET.equals(event.getNyStatus())) {
            return;
        }
        if (event.getGammelStatus() != null && ProsessTaskStatus.FEILET.equals(event.getGammelStatus())) {
            final SensuEvent sensuEvent = SensuEvent.createSensuEvent(
                    "k9-fordel.antall_feilende_prosesstask",
                    Map.of("prosesstask_type", event.getTaskType()),
                    Map.of("antall", -1));

            sensuKlient.logMetrics(sensuEvent);
        }
        if (ProsessTaskStatus.FEILET.equals(event.getNyStatus())) {
            final SensuEvent sensuEvent = SensuEvent.createSensuEvent(
                    "k9-fordel.antall_feilende_prosesstask",
                    Map.of("prosesstask_type", event.getTaskType()),
                    Map.of("antall", 1));

            sensuKlient.logMetrics(sensuEvent);
        }
    }
}
