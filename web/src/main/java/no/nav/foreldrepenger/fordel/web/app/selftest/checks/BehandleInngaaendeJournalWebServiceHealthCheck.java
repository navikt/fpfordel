package no.nav.foreldrepenger.fordel.web.app.selftest.checks;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.vedtak.felles.integrasjon.behandleinngaaendejournal.BehandleInngaaendeJournalSelftestConsumer;

@ApplicationScoped
public class BehandleInngaaendeJournalWebServiceHealthCheck extends WebServiceHealthCheck {

    private BehandleInngaaendeJournalSelftestConsumer selftestConsumer;

    BehandleInngaaendeJournalWebServiceHealthCheck() {
        // for CDI proxy
    }

    @Inject
    public BehandleInngaaendeJournalWebServiceHealthCheck(BehandleInngaaendeJournalSelftestConsumer selftestConsumer) {
        this.selftestConsumer = selftestConsumer;
    }

    @Override
    protected void performWebServiceSelftest() {
        selftestConsumer.ping();
    }

    @Override
    protected String getDescription() {
        return "Test av web service Behandle Inngaaende Journal";
    }

    @Override
    protected String getEndpoint() {
        return selftestConsumer.getEndpointUrl();
    }

    @Override
    public boolean erKritiskTjeneste() {
        return false;
    }
}
