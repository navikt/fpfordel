package no.nav.foreldrepenger.mottak.person;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.vedtak.felles.integrasjon.pdl.Pdl;
import no.nav.vedtak.felles.integrasjon.rest.jersey.Jersey;

@Dependent
@Jersey("onbehalf")
public class OnBehalfPersonTjeneste extends AbstractCachingPersonTjeneste {

    @Inject
    public OnBehalfPersonTjeneste(@Jersey("onbehalf") Pdl pdl) {
        super(pdl);
    }
}
