package no.nav.foreldrepenger.mottak.behandlendeenhet;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.Tema;
import no.nav.foreldrepenger.fordel.kodeverdi.Temagrupper;
import no.nav.foreldrepenger.mottak.person.PersonInformasjon;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.integrasjon.arbeidsfordeling.Arbeidsfordeling;
import no.nav.vedtak.felles.integrasjon.arbeidsfordeling.ArbeidsfordelingRequest;
import no.nav.vedtak.felles.integrasjon.arbeidsfordeling.ArbeidsfordelingResponse;

@ApplicationScoped
public class EnhetsTjeneste {
    public static final String NK_ENHET_ID = "4292"; // Enhetsnummer NAV Klageinstans Midt-Norge
    public static final String SKJERMET_ENHET_ID = "4883"; // Enhetsnummer NAV Familie og Pensjon Skjermet
    public static final String SF_ENHET_ID = "2103"; // Enhetsnummer NAV K6 enhet
    public static final String UTLAND_ENHET_ID = "4806"; // Enhetsnummer NAV K6 enhet
    public static final Set<String> SPESIALENHETER = Set.of(NK_ENHET_ID, SKJERMET_ENHET_ID, SF_ENHET_ID, UTLAND_ENHET_ID);
    private static final String TEMAGRUPPE = Temagrupper.FAMILIEYTELSER.getKode(); // Kodeverk Temagrupper - dekker FOR + OMS
    private static final String TEMA = Tema.FORELDRE_OG_SVANGERSKAPSPENGER.getOffisiellKode();
    private static final String OPPGAVETYPE_JFR = "JFR"; // Kodeverk Oppgavetyper - NFP , uten spesialenheter
    private static final String ENHET_TYPE_NFP = "FPY"; // Kodeverk EnhetstyperNORG - NFP , uten spesialenheter
    private static final String BEHANDLINGTYPE = "ae0034"; // Kodeverk Behandlingstype, bruker søknad
    private static final String NASJONAL_ENHET_ID = "4867";

    private static final Logger LOG = LoggerFactory.getLogger(EnhetsTjeneste.class);
    private static final Set<String> FLYTTET = Set.of("4806", "4833", "4849", "4812", "4817", "4842");
    private PersonInformasjon pdl;
    private Arbeidsfordeling norgKlient;
    private SkjermetPersonKlient skjermetPersonKlient;

    private final Set<String> alleJournalførendeEnheter = new HashSet<>(); // Med klageinstans og kode6 og skjermet
    private final Set<String> nfpJournalførendeEnheter = new HashSet<>(); // Kun NFP
    private LocalDate sisteInnhenting = LocalDate.MIN;

    public EnhetsTjeneste() {
    }

    @Inject
    public EnhetsTjeneste(PersonInformasjon personTjeneste,
                          Arbeidsfordeling norgKlient,
                          SkjermetPersonKlient skjermetPersonKlient) {
        this.pdl = personTjeneste;
        this.norgKlient = norgKlient;
        this.skjermetPersonKlient = skjermetPersonKlient;
    }

    // Behold ut 2023
    private static String validerOgVelgBehandlendeEnhet(List<ArbeidsfordelingResponse> response, String gt) { //NOSONAR
        // Vi forventer å få én behandlende enhet.
        if (response == null || response.size() != 1) {
            throw new TekniskException("FP-669566", String.format("Finner ikke behandlende enhet for geografisk tilknytning %s", gt));
        }
        var enhet = response.get(0).enhetNr();
        return FLYTTET.contains(enhet) ? NASJONAL_ENHET_ID : enhet;
    }

    public String hentFordelingEnhetId(Tema tema, BehandlingTema behandlingTema, Optional<String> enhetInput, String aktørId) {
        LOG.info("Henter enhet id for {},{}", tema, behandlingTema);
        //oppdaterEnhetCache(); LA STÅ UT 2023
        if (enhetInput.isPresent()) {
            return SPESIALENHETER.contains(enhetInput.get()) ? enhetInput.get() : NASJONAL_ENHET_ID;
        }


        var id = Optional.ofNullable(aktørId).map(a -> hentEnhetId(a, behandlingTema, tema)).orElse(NASJONAL_ENHET_ID);
        LOG.info("returnerer enhet id {}", id);
        return id;

    }

    private String hentEnhetId(String aktørId, BehandlingTema behandlingTema, Tema tema) { //NOSONAR
        if (pdl.harStrengDiskresjonskode(behandlingTema, aktørId)) {
            return SF_ENHET_ID;
        }

        var personIdent = pdl.hentPersonIdentForAktørId(aktørId);
        if (personIdent.filter(skjermetPersonKlient::erSkjermet).isPresent()) {
            return SKJERMET_ENHET_ID;
        }

        var gt = pdl.hentGeografiskTilknytning(behandlingTema, aktørId);
        if (gt == null) { // Udefinert og utland likebehandles
            return UTLAND_ENHET_ID;
        }

        return NASJONAL_ENHET_ID;
        /* LA STÅ UT 2023
        var request = ArbeidsfordelingRequest.ny()
            .medTemagruppe(TEMAGRUPPE)
            .medTema(tema.getOffisiellKode())
            .medBehandlingstema(behandlingTema.getOffisiellKode())
            .medBehandlingstype(BEHANDLINGTYPE)
            .medOppgavetype(OPPGAVETYPE_JFR)
            .medGeografiskOmraade(gt)
            .build(); //NOSONAR
        return validerOgVelgBehandlendeEnhet(norgKlient.finnEnhet(request), gt); //NOSONAR

         */
    }

    private void oppdaterEnhetCache() { // NOSONAR
        if (sisteInnhenting.isBefore(LocalDate.now())) {
            var request = ArbeidsfordelingRequest.ny()
                .medTemagruppe(TEMAGRUPPE)
                .medTema(TEMA)
                .medBehandlingstype(BEHANDLINGTYPE)
                .medBehandlingstema(BehandlingTema.FORELDREPENGER.getOffisiellKode())
                .medOppgavetype(OPPGAVETYPE_JFR)
                .build();
            var respons = norgKlient.hentAlleAktiveEnheter(request);
            alleJournalførendeEnheter.clear();
            nfpJournalførendeEnheter.clear();
            respons.stream()
                .filter(e -> ENHET_TYPE_NFP.equalsIgnoreCase(e.enhetType()))
                .map(ArbeidsfordelingResponse::enhetNr)
                .filter(e -> !SPESIALENHETER.contains(e))
                .forEach(nfpJournalførendeEnheter::add);
            alleJournalførendeEnheter.addAll(respons.stream().map(ArbeidsfordelingResponse::enhetNr).toList());
            alleJournalførendeEnheter.addAll(SPESIALENHETER);
            alleJournalførendeEnheter.add(NASJONAL_ENHET_ID);
            nfpJournalførendeEnheter.add(NASJONAL_ENHET_ID);
            sisteInnhenting = LocalDate.now();
        }
    }

}
