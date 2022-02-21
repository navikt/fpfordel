package no.nav.foreldrepenger.mottak.behandlendeenhet;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.Tema;
import no.nav.foreldrepenger.mottak.behandlendeenhet.nom.SkjermetPersonKlient;
import no.nav.foreldrepenger.mottak.person.PersonInformasjon;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.integrasjon.arbeidsfordeling.rest.Arbeidsfordeling;
import no.nav.vedtak.felles.integrasjon.arbeidsfordeling.rest.ArbeidsfordelingRequest;
import no.nav.vedtak.felles.integrasjon.arbeidsfordeling.rest.ArbeidsfordelingResponse;
import no.nav.vedtak.felles.integrasjon.rest.jersey.Jersey;

@ApplicationScoped
public class EnhetsTjeneste implements EnhetsInfo {
    private static final Logger LOG = LoggerFactory.getLogger(EnhetsTjeneste.class);
    private PersonInformasjon pdl;
    private Arbeidsfordeling norgKlient;
    private SkjermetPersonKlient skjermetPersonKlient;

    private Set<String> alleJournalførendeEnheter = new HashSet<>(); // Med klageinstans og kode6 og skjermet
    private List<String> nfpJournalførendeEnheter = new ArrayList<>(); // Kun NFP
    private LocalDate sisteInnhenting = LocalDate.MIN;

    public EnhetsTjeneste() {
    }

    @Inject
    public EnhetsTjeneste(
            PersonInformasjon personTjeneste,
            @Jersey Arbeidsfordeling norgKlient,
            SkjermetPersonKlient skjermetPersonKlient) {
        this.pdl = personTjeneste;
        this.norgKlient = norgKlient;
        this.skjermetPersonKlient = skjermetPersonKlient;
    }

    @Override
    public String hentFordelingEnhetId(Tema tema, BehandlingTema behandlingTema, Optional<String> enhetInput, String aktørId) {
        LOG.info("Henter enhet id for {},{}", tema, behandlingTema);
        oppdaterEnhetCache();
        if (enhetInput.filter(alleJournalførendeEnheter::contains).isPresent()) {
            return enhetInput.get();
        }

        var id = Optional.ofNullable(aktørId)
                .map(a -> hentEnhetId(a, behandlingTema, tema))
                .orElseGet(this::tilfeldigNfpEnhet);
        LOG.info("returnerer enhet id  {}", id);
        return id;
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
        if (gt == null) {
            return tilfeldigNfpEnhet();
        }

        var request = ArbeidsfordelingRequest.ny()
                .medTemagruppe(TEMAGRUPPE)
                .medTema(tema.getOffisiellKode())
                .medBehandlingstema(behandlingTema.getOffisiellKode())
                .medBehandlingstype(BEHANDLINGTYPE)
                .medOppgavetype(OPPGAVETYPE_JFR)
                .medGeografiskOmraade(gt)
                .build();
        return validerOgVelgBehandlendeEnhet(norgKlient.finnEnhet(request), gt);
    }

    private static String validerOgVelgBehandlendeEnhet(List<ArbeidsfordelingResponse> response, String gt) {
        // Vi forventer å få én behandlende enhet.
        if (response == null || response.size() != 1) {
            throw new TekniskException("FP-669566", String.format("Finner ikke behandlende enhet for geografisk tilknytning %s", gt));
        }

        return response.get(0).enhetNr();
    }

    private String tilfeldigNfpEnhet() {
        return nfpJournalførendeEnheter.get(LocalDateTime.now().getSecond() % nfpJournalførendeEnheter.size());
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
            sisteInnhenting = LocalDate.now();
        }
    }

}
