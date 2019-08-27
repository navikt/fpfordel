package no.nav.foreldrepenger.fordel.web.app.selftest.checks;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.vedtak.felles.integrasjon.person.PersonSelftestConsumer;

@ApplicationScoped
public class PersonWebServiceHealthCheck extends WebServiceHealthCheck {

    private PersonSelftestConsumer selftestConsumer;

    PersonWebServiceHealthCheck() {
        // for CDI proxy
    }

    @Inject
    public PersonWebServiceHealthCheck(PersonSelftestConsumer selftestConsumer) {
        this.selftestConsumer = selftestConsumer;
    }

    @Override
    protected String getDescription() {
        return "Test av web service Person (TPS)";
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
