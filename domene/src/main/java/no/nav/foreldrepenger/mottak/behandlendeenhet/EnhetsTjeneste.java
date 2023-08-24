package no.nav.foreldrepenger.mottak.behandlendeenhet;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.Tema;
import no.nav.foreldrepenger.mottak.person.PersonInformasjon;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.integrasjon.arbeidsfordeling.Arbeidsfordeling;
import no.nav.vedtak.felles.integrasjon.arbeidsfordeling.ArbeidsfordelingRequest;
import no.nav.vedtak.felles.integrasjon.arbeidsfordeling.ArbeidsfordelingResponse;
import no.nav.vedtak.felles.integrasjon.oppgave.v1.Oppgaver;
import no.nav.vedtak.felles.integrasjon.oppgave.v1.Oppgavetype;
import no.nav.vedtak.felles.integrasjon.oppgave.v1.OpprettOppgave;
import no.nav.vedtak.felles.integrasjon.oppgave.v1.Prioritet;

@ApplicationScoped
public class EnhetsTjeneste implements JournalføringsOppgave {
    private static final Logger LOG = LoggerFactory.getLogger(EnhetsTjeneste.class);
    private static final Set<String> FLYTTET = Set.of("4806", "4833", "4849", "4812", "4817", "4842");
    private PersonInformasjon pdl;
    private Arbeidsfordeling norgKlient;
    private SkjermetPersonKlient skjermetPersonKlient;
    private Oppgaver oppgaver;

    private final Set<String> alleJournalførendeEnheter = new HashSet<>(); // Med klageinstans og kode6 og skjermet
    private final Set<String> nfpJournalførendeEnheter = new HashSet<>(); // Kun NFP
    private LocalDate sisteInnhenting = LocalDate.MIN;

    public EnhetsTjeneste() {
    }

    @Inject
    public EnhetsTjeneste(PersonInformasjon personTjeneste,
                          Arbeidsfordeling norgKlient,
                          SkjermetPersonKlient skjermetPersonKlient,
                          Oppgaver oppgaver) {
        this.pdl = personTjeneste;
        this.norgKlient = norgKlient;
        this.skjermetPersonKlient = skjermetPersonKlient;
        this.oppgaver = oppgaver;
    }

    // Behold ut 2023
    private static String validerOgVelgBehandlendeEnhet(List<ArbeidsfordelingResponse> response, String gt) {
        // Vi forventer å få én behandlende enhet.
        if (response == null || response.size() != 1) {
            throw new TekniskException("FP-669566", String.format("Finner ikke behandlende enhet for geografisk tilknytning %s", gt));
        }
        var enhet = response.get(0).enhetNr();
        return FLYTTET.contains(enhet) ? NASJONAL_ENHET_ID : enhet;
    }

    @Override
    public String hentFordelingEnhetId(Tema tema, BehandlingTema behandlingTema, Optional<String> enhetInput, String aktørId) {
        LOG.info("Henter enhet id for {},{}", tema, behandlingTema);
        //oppdaterEnhetCache(); LA STÅ UT 2023
        if (enhetInput.isPresent()) {
            return SPESIALENHETER.contains(enhetInput.get()) ? enhetInput.get() : NASJONAL_ENHET_ID;
        }


        var id = Optional.ofNullable(aktørId).map(a -> hentEnhetId(a, behandlingTema, tema)).orElse(NASJONAL_ENHET_ID);
        LOG.info("returnerer enhet id  {}", id);
        return id;

    }

    @Override
    public String opprettJournalføringsOppgave(String journalpostId,
                                               String enhetId,
                                               String aktørId,
                                               String saksref,
                                               String behandlingTema,
                                               String beskrivelse) {
        var request = OpprettOppgave.getBuilderTemaFOR(Oppgavetype.JOURNALFØRING, Prioritet.NORM, 1)
            .medAktoerId(aktørId)
            .medSaksreferanse(saksref)
            .medTildeltEnhetsnr(enhetId)
            .medOpprettetAvEnhetsnr(enhetId)
            .medJournalpostId(journalpostId)
            .medBeskrivelse(beskrivelse)
            .medBehandlingstema(behandlingTema);
        var oppgave = oppgaver.opprettetOppgave(request.build());
        LOG.info("FPFORDEL GOSYS opprettet oppgave {}", oppgave);
        return oppgave.id().toString();
    }

    @Override
    public boolean finnesÅpenJournalføringsoppgaveForJournalpost(String journalpostId) {
        return !oppgaver.finnÅpneJournalføringsoppgaverForJournalpost(journalpostId).isEmpty();
    }

    private String hentEnhetId(String aktørId, BehandlingTema behandlingTema, Tema tema) {
        if (pdl.harStrengDiskresjonskode(aktørId)) {
            return SF_ENHET_ID;
        }

        var personIdent = pdl.hentPersonIdentForAktørId(aktørId);
        if (personIdent.filter(skjermetPersonKlient::erSkjermet).isPresent()) {
            return SKJERMET_ENHET_ID;
        }

        var gt = pdl.hentGeografiskTilknytning(aktørId);
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
            .build();
        return validerOgVelgBehandlendeEnhet(norgKlient.finnEnhet(request), gt);

         */
    }

    private void oppdaterEnhetCache() {
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
