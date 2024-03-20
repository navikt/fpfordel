package no.nav.foreldrepenger.mottak.behandlendeenhet;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.mottak.klient.Los;
import no.nav.foreldrepenger.mottak.klient.TilhørendeEnhetDto;
import no.nav.vedtak.util.LRUCache;

@ApplicationScoped
public class LosEnheterCachedTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(LosEnheterCachedTjeneste.class);

    private static final int DEFAULT_CACHE_SIZE = 200;
    private static final long DEFAULT_CACHE_TIMEOUT = TimeUnit.MILLISECONDS.convert(12, TimeUnit.HOURS);

    private LRUCache<String, List<TilhørendeEnhetDto>> enheterCache;

    private Los losKlient;

    LosEnheterCachedTjeneste() {
        // CDI
    }

    @Inject
    public LosEnheterCachedTjeneste(Los losKlient) {
        this.losKlient = losKlient;
        this.enheterCache = new LRUCache<>(DEFAULT_CACHE_SIZE, DEFAULT_CACHE_TIMEOUT);
    }

    public List<TilhørendeEnhetDto> hentLosEnheterFor(String ident) {
        try {
            return Optional.ofNullable(enheterCache.get(ident)).orElseGet(() -> hentEnheter(ident));
        } catch (NullPointerException e) {
            LOG.warn("Kunne ikke hente enheter fra Los for ident {}", ident, e);
            return List.of();
        }
    }

    private List<TilhørendeEnhetDto> hentEnheter(String ident) {
        var behandlersEnheter = Optional.of(losKlient.hentTilhørendeEnheter(ident));
        behandlersEnheter.ifPresent(pi -> enheterCache.put(ident, pi));
        return behandlersEnheter.get();
    }
}
