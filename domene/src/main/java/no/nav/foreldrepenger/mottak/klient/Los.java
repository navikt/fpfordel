package no.nav.foreldrepenger.mottak.klient;

import java.util.List;

public interface Los {
    List<TilhørendeEnhetDto> hentTilhørendeEnheter(String ident);
}
