package no.nav.foreldrepenger.mottak.person;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.vedtak.felles.integrasjon.pdl.Pdl;
import no.nav.vedtak.felles.integrasjon.rest.jersey.Jersey;

@Dependent
@Jersey("system")
public class SystemPersonTjeneste extends AbstractCachingPersonTjeneste {

    @Inject
    public SystemPersonTjeneste(@Jersey("system") Pdl pdl) {
        super(pdl);
    }
}
