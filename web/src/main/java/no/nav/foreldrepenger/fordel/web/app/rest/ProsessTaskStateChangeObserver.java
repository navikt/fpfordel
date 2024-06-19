package no.nav.foreldrepenger.fordel.web.app.rest;

import static io.micrometer.core.instrument.Metrics.counter;
import static no.nav.vedtak.felles.prosesstask.api.ProsessTaskStatus.FEILET;
import static no.nav.vedtak.felles.prosesstask.api.ProsessTaskStatus.FERDIG;
import static no.nav.vedtak.felles.prosesstask.api.ProsessTaskStatus.KJOERT;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

import no.nav.vedtak.felles.prosesstask.api.ProsessTaskEvent;

@ApplicationScoped
public class ProsessTaskStateChangeObserver {

    public void observerProssestask(
        @Observes
        ProsessTaskEvent event) {
        var nyStatus = event.getNyStatus();
        if (FEILET.equals(nyStatus) || KJOERT.equals(nyStatus) || FERDIG.equals(nyStatus)) {
            counter("task_transitions", "type", event.getTaskType(), "status", nyStatus.getDbKode()).increment();
        }
    }
}
