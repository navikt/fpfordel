package no.nav.foreldrepenger.mottak.gsak.api;

import java.util.List;

public interface GsakSakTjeneste {

    List<GsakSak> finnSaker(String fnr);
}
