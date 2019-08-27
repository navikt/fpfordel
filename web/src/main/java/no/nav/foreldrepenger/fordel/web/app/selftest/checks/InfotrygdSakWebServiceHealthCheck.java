package no.nav.foreldrepenger.fordel.web.app.selftest.checks;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.vedtak.felles.integrasjon.infotrygdsak.InfotrygdSakSelftestConsumer;

@ApplicationScoped
public class InfotrygdSakWebServiceHealthCheck extends WebServiceHealthCheck {

    InfotrygdSakSelftestConsumer infotrygdSakSelftestConsumer;

    InfotrygdSakWebServiceHealthCheck(){
        //For CDI proxy
    }

    @Inject
    public InfotrygdSakWebServiceHealthCheck(InfotrygdSakSelftestConsumer infotrygdSakSelftestConsumer){
        this.infotrygdSakSelftestConsumer = infotrygdSakSelftestConsumer;
    }

    @Override
    protected void performWebServiceSelftest() {
        infotrygdSakSelftestConsumer.ping();
    }

    @Override
    protected String getDescription() {
        return "Test av web service InfotrygdSak";
    }

    @Override
    protected String getEndpoint() {
        return infotrygdSakSelftestConsumer.getEndpointUrl();
    }

    @Override
    public boolean erKritiskTjeneste() {
        return false;
    }
}
