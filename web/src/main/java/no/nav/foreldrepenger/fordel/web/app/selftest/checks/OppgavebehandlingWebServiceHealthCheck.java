package no.nav.foreldrepenger.fordel.web.app.selftest.checks;


import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.vedtak.felles.integrasjon.behandleoppgave.BehandleoppgaveSelftestConsumer;


@ApplicationScoped
public class OppgavebehandlingWebServiceHealthCheck extends WebServiceHealthCheck {

    private BehandleoppgaveSelftestConsumer selftestConsumer;

    OppgavebehandlingWebServiceHealthCheck() {
       // for CDI proxy
    }
    
    @Inject
    public OppgavebehandlingWebServiceHealthCheck(BehandleoppgaveSelftestConsumer selftestConsumer) {
        this.selftestConsumer = selftestConsumer;
    }

    @Override
    protected String getDescription() {
        return "Test av web selftestConsumer GSAK - Oppgavebehandling";
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


