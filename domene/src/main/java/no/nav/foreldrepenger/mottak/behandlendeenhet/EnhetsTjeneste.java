package no.nav.foreldrepenger.mottak.behandlendeenhet;

import java.util.Optional;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.Tema;
import no.nav.foreldrepenger.fordel.kodeverdi.Temagrupper;

public interface EnhetsTjeneste {
    static final String TEMAGRUPPE = Temagrupper.FAMILIEYTELSER.getKode(); // Kodeverk Temagrupper - dekker FOR + OMS
    static final String TEMA = Tema.FORELDRE_OG_SVANGERSKAPSPENGER.getOffisiellKode();
    static final String OPPGAVETYPE_JFR = "JFR"; // Kodeverk Oppgavetyper - NFP , uten spesialenheter
    static final String ENHET_TYPE_NFP = "FPY"; // Kodeverk EnhetstyperNORG - NFP , uten spesialenheter
    static final String BEHANDLINGTYPE = "ae0034"; // Kodeverk Behandlingstype, bruker søknad
    static final String NK_ENHET_ID = "4292";

    String hentFordelingEnhetId(Tema tema, BehandlingTema behandlingTema, Optional<String> enhetInput, String aktørId);

}
