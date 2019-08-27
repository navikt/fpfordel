package no.nav.foreldrepenger.mottak.behandlendeenhet;

import java.util.List;

import no.nav.foreldrepenger.fordel.kodeverk.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverk.Tema;

public interface ArbeidsfordelingTjeneste {

    String finnBehandlendeEnhetId(String geografiskTilknytning, String diskresjonskode, BehandlingTema behandlingTema, Tema tema);

    List<String> finnAlleJournalførendeEnhetIdListe(BehandlingTema behandlingTema, boolean medSpesialenheter);
}
