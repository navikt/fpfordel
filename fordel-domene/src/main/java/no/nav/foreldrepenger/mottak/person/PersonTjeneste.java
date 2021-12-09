package no.nav.foreldrepenger.mottak.person;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.vedtak.felles.integrasjon.pdl.Pdl;
import no.nav.vedtak.felles.integrasjon.rest.jersey.Jersey;

@Dependent
public class PersonTjeneste extends AbstractCachingPersonTjeneste {

    @Inject
    public PersonTjeneste(@Jersey Pdl pdl) {
        super(pdl);
    }

    PersonTjeneste(Pdl pdl, long timeoutMs) {
        super(pdl, timeoutMs);
    }

}
