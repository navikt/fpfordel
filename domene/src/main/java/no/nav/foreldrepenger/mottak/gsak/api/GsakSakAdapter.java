package no.nav.foreldrepenger.mottak.gsak.api;

import java.util.List;

public interface GsakSakAdapter {

    void ping();

    List<GsakSak> finnSaker(String fnr);
}
