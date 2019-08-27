package no.nav.foreldrepenger.fordel.web.app.selftest.checks;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.vedtak.felles.integrasjon.sak.SakSelftestConsumer;

@ApplicationScoped
public class SakWebServiceHealthCheck extends WebServiceHealthCheck {

    private SakSelftestConsumer selftestConsumer;

    SakWebServiceHealthCheck() {
        // for CDI proxy
    }
    
    @Inject
    public SakWebServiceHealthCheck(SakSelftestConsumer selftestConsumer) {
        this.selftestConsumer = selftestConsumer;
    }

    @Override
    protected String getDescription() {
        return "Test av web service GSAK - Sak";
    }

    @Override
    protected String getEndpoint() {
        return selftestConsumer.getEndpointUrl();
    }

    @Override
    protected void performWebServiceSelftest() {
        selftestConsumer.ping();
    }

    @Override
    public boolean erKritiskTjeneste() {
        return false;
    }
}


