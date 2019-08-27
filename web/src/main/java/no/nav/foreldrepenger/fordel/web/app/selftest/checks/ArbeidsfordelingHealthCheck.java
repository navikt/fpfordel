package no.nav.foreldrepenger.fordel.web.app.selftest.checks;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.vedtak.felles.integrasjon.arbeidsfordeling.klient.ArbeidsfordelingSelftestConsumer;

@ApplicationScoped
public class ArbeidsfordelingHealthCheck extends WebServiceHealthCheck {

    private ArbeidsfordelingSelftestConsumer consumer;

    public ArbeidsfordelingHealthCheck() {
    }

    @Inject
    public ArbeidsfordelingHealthCheck(ArbeidsfordelingSelftestConsumer consumer) {
        this.consumer = consumer;
    }

    @Override
    protected void performWebServiceSelftest() {
        consumer.ping();
    }


    @Override
    protected String getDescription() {
        return "Test av web service Arbeidsfordeling";
    }

    @Override
    protected String getEndpoint() {
        return consumer.getEndpointUrl();
    }

    @Override
    public boolean erKritiskTjeneste() {
        return false;
    }
}
