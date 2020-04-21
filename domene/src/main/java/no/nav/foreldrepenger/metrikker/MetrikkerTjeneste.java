package no.nav.foreldrepenger.metrikker;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class MetrikkerTjeneste {

    private SensuKlient sensuKlient;

    MetrikkerTjeneste() {} // WELD ctor

    @Inject
    public MetrikkerTjeneste(SensuKlient sensuKlient) {
        this.sensuKlient = sensuKlient;
    }
}
