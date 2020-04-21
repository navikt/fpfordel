package no.nav.foreldrepenger.metrikker;

import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.vedtak.felles.integrasjon.sensu.SensuEvent;
import no.nav.vedtak.felles.integrasjon.sensu.SensuKlient;

@ApplicationScoped
public class MetrikkerTjeneste {

    private SensuKlient sensuKlient;

    MetrikkerTjeneste() {
    } // WELD ctor

    @Inject
    public MetrikkerTjeneste(SensuKlient sensuKlient) {
        this.sensuKlient = sensuKlient;
    }

    public void logFeilProsessTask(String prosessTaskType, int antall) {
        send(opprettProsessTaskEvent("antall_feilende_prosesstask", prosessTaskType, antall));
    }

    public void logProsessTask(String prosessTaskType, int antall) {
        send(opprettProsessTaskEvent("antall_prosesstask", prosessTaskType, antall));
    }

    public void logProsessTask(String prosessTaskType) {
        logProsessTask(prosessTaskType, 1);
    }

    private static SensuEvent opprettProsessTaskEvent(String metrikkNavn, String prosessTaskType, int antall) {
        return SensuEvent.createSensuEvent(metrikkNavn,
                Map.of("prosesstask_type", prosessTaskType),
                Map.of("antall", antall));
    }

    private void send(SensuEvent event) {
        sensuKlient.logMetrics(event);
    }
}
