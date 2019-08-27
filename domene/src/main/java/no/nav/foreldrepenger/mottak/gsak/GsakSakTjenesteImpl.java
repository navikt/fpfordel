package no.nav.foreldrepenger.mottak.gsak;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.mottak.gsak.api.GsakSak;
import no.nav.foreldrepenger.mottak.gsak.api.GsakSakAdapter;
import no.nav.foreldrepenger.mottak.gsak.api.GsakSakTjeneste;

@ApplicationScoped
class GsakSakTjenesteImpl implements GsakSakTjeneste {

    private GsakSakAdapter gsakSakAdapter;

    public GsakSakTjenesteImpl() {
    }

    @Inject
    public GsakSakTjenesteImpl(GsakSakAdapter gsakSakAdapter) {
        this.gsakSakAdapter = gsakSakAdapter;
    }

    @Override
    public List<GsakSak> finnSaker(String fnr) {
        return gsakSakAdapter.finnSaker(fnr);
    }
}
