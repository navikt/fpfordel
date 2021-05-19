package no.nav.foreldrepenger.mottak.person;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.benmanes.caffeine.cache.LoadingCache;

import no.nav.vedtak.felles.integrasjon.pdl.Pdl;
import no.nav.vedtak.felles.integrasjon.rest.jersey.Jersey;

@Dependent
@Jersey("onbehalf")
public class OnBehalfPersonTjeneste extends AbstractCachingPersonTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(OnBehalfPersonTjeneste.class);

    @Inject
    public OnBehalfPersonTjeneste(@Jersey("onbehalf") Pdl pdl) {
        this(pdl, cache(tilFnr(pdl)), cache(tilAktørId(pdl)));
    }

    private OnBehalfPersonTjeneste(Pdl pdl, LoadingCache<String, String> tilFnr, LoadingCache<String, String> tilAktør) {
        super(pdl, tilFnr, tilAktør);
        LOG.trace("Created");
    }

}
