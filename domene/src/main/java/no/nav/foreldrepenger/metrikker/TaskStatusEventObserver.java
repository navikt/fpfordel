package no.nav.foreldrepenger.metrikker;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import no.nav.vedtak.felles.prosesstask.api.ProsessTaskEvent;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskStatus;

@ApplicationScoped
class TaskStatusEventObserver {

    private MetrikkerTjeneste metrikkerTjeneste;

    TaskStatusEventObserver() {
    }

    @Inject
    public TaskStatusEventObserver(MetrikkerTjeneste metrikkerTjeneste) {
        this.metrikkerTjeneste = metrikkerTjeneste;
    }

    void observerProsessTasks(@Observes ProsessTaskEvent event) {
        // Gjentaker - ignorer
        if (event.getGammelStatus() != null
                && ProsessTaskStatus.FEILET.equals(event.getGammelStatus())
                && ProsessTaskStatus.FEILET.equals(event.getNyStatus())) {
            return;
        }
        // Feiler ikke lenger
        if (event.getGammelStatus() != null && ProsessTaskStatus.FEILET.equals(event.getGammelStatus())) {
            metrikkerTjeneste.logFeilProsessTaskEvent(event.getTaskType(), -1);
        }
        // Ny feil
        if (ProsessTaskStatus.FEILET.equals(event.getNyStatus())) {
            metrikkerTjeneste.logFeilProsessTaskEvent(event.getTaskType(), 1);
        }
    }
}
