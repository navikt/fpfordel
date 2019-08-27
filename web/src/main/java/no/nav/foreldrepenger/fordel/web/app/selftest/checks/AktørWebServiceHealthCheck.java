package no.nav.foreldrepenger.fordel.web.app.selftest.checks;


import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørSelftestConsumer;

@ApplicationScoped
public class AktørWebServiceHealthCheck extends WebServiceHealthCheck {

    private AktørSelftestConsumer aktorSelftestConsumer;

    AktørWebServiceHealthCheck() {
        // for CDI proxy
    }

    @Inject
    public AktørWebServiceHealthCheck(AktørSelftestConsumer aktorSelftestConsumer) {
        this.aktorSelftestConsumer = aktorSelftestConsumer;
    }

    @Override
    protected String getDescription() {
        return "Test av web service Aktør (TPS)";
    }

    @Override
    protected String getEndpoint() {
        return aktorSelftestConsumer.getEndpointUrl();
    }

    @Override
    protected void performWebServiceSelftest() {
        aktorSelftestConsumer.ping();
    }

    @Override
    public boolean erKritiskTjeneste() {
        return false;
    }
}
