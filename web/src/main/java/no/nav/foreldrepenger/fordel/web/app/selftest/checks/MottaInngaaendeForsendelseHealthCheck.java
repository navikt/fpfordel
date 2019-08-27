package no.nav.foreldrepenger.fordel.web.app.selftest.checks;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.vedtak.felles.integrasjon.mottainngaaendeforsendelse.MottaInngaaendeForsendelseRestSelftestKlient;

@ApplicationScoped
public class MottaInngaaendeForsendelseHealthCheck extends WebServiceHealthCheck {

    private MottaInngaaendeForsendelseRestSelftestKlient selftestKlient;

    MottaInngaaendeForsendelseHealthCheck() {
        // CDI
    }

    @Inject
    public MottaInngaaendeForsendelseHealthCheck(MottaInngaaendeForsendelseRestSelftestKlient selftestKlient) {
        this.selftestKlient = selftestKlient;
    }

    @Override
    protected void performWebServiceSelftest() {
        selftestKlient.ping();
    }

    @Override
    protected String getDescription() {
        return "Test av REST service Motta Inngaaende Forsendelse";
    }

    @Override
    protected String getEndpoint() {
        return selftestKlient.getEndpointUrl();
    }

    @Override
    public boolean erKritiskTjeneste() {
        return false;
    }
}
