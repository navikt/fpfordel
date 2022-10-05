package no.nav.foreldrepenger.mottak.behandlendeenhet;

import java.util.Optional;
import java.util.Set;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.Tema;
import no.nav.foreldrepenger.fordel.kodeverdi.Temagrupper;

public interface JournalføringsOppgave {
    static final String TEMAGRUPPE = Temagrupper.FAMILIEYTELSER.getKode(); // Kodeverk Temagrupper - dekker FOR + OMS
    static final String TEMA = Tema.FORELDRE_OG_SVANGERSKAPSPENGER.getOffisiellKode();
    static final String OPPGAVETYPE_JFR = "JFR"; // Kodeverk Oppgavetyper - NFP , uten spesialenheter
    static final String ENHET_TYPE_NFP = "FPY"; // Kodeverk EnhetstyperNORG - NFP , uten spesialenheter
    static final String BEHANDLINGTYPE = "ae0034"; // Kodeverk Behandlingstype, bruker søknad
    static final String NK_ENHET_ID = "4292"; // Enhetsnummer NAV Klageinstans Midt-Norge
    static final String SKJERMET_ENHET_ID = "4883"; // Enhetsnummer NAV Familie og Pensjon Skjermet
    static final String SF_ENHET_ID = "2103"; // Enhetsnummer NAV K6 enhet
    static final Set<String> SPESIALENHETER = Set.of(NK_ENHET_ID, SKJERMET_ENHET_ID, SF_ENHET_ID);

    String hentFordelingEnhetId(Tema tema, BehandlingTema behandlingTema, Optional<String> enhetInput, String aktørId);

    String opprettJournalføringsOppgave(String journalpostId, String enhetId, String aktørId, String saksref, String behandlingTema, String beskrivelse);

}
