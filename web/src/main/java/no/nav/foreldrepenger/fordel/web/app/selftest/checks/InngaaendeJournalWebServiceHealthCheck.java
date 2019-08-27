package no.nav.foreldrepenger.fordel.web.app.selftest.checks;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.vedtak.felles.integrasjon.inngaaendejournal.InngaaendeJournalSelftestConsumer;

@ApplicationScoped
public class InngaaendeJournalWebServiceHealthCheck extends WebServiceHealthCheck {

    private InngaaendeJournalSelftestConsumer selftestConsumer;

    InngaaendeJournalWebServiceHealthCheck() {
        // for CDI proxy
    }

    @Inject
    public InngaaendeJournalWebServiceHealthCheck(InngaaendeJournalSelftestConsumer selftestConsumer) {
        this.selftestConsumer = selftestConsumer;
    }

    @Override
    protected void performWebServiceSelftest() {
        selftestConsumer.ping();
    }

    @Override
    protected String getDescription() {
        return "Test av web service Inngaaende Journal";
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
