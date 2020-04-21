package no.nav.foreldrepenger.metrikker;

import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class MetrikkerTjeneste {

    private SensuKlient sensuKlient;

    MetrikkerTjeneste() {} // WELD ctor

    @Inject
    public MetrikkerTjeneste(SensuKlient sensuKlient) {
        this.sensuKlient = sensuKlient;
    }

    public void logFeilProsessTaskEvent(String prosessTaskType, int antall) {
        final SensuEvent sensuEvent = SensuEvent.createSensuEvent(
                "antall_feilende_prosesstask",
                Map.of("prosesstask_type", prosessTaskType),
                Map.of("antall", antall));

        sensuKlient.logMetrics(sensuEvent);
    }
}
