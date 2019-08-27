package no.nav.foreldrepenger.mottak.behandlendeenhet;

import java.util.Optional;

import no.nav.foreldrepenger.fordel.kodeverk.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverk.Tema;

public interface EnhetsTjeneste {

    String hentFordelingEnhetId(Tema tema, BehandlingTema behandlingTema, Optional<String> enhetInput, Optional<String> fnr);
}
