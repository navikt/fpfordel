package no.nav.foreldrepenger.mottak.behandlendeenhet;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.Tema;
import no.nav.foreldrepenger.mottak.person.GeoTilknytning;
import no.nav.foreldrepenger.mottak.person.PersonInformasjon;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.ManglerTilgangFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;
import no.nav.vedtak.felles.integrasjon.arbeidsfordeling.rest.ArbeidsfordelingRequest;
import no.nav.vedtak.felles.integrasjon.arbeidsfordeling.rest.ArbeidsfordelingResponse;
import no.nav.vedtak.felles.integrasjon.arbeidsfordeling.rest.ArbeidsfordelingRestKlient;

@ApplicationScoped
public class LegacyEnhetsTjeneste implements EnhetsTjeneste {

    private PersonInformasjon personTjeneste;
    private ArbeidsfordelingRestKlient norgKlient;

    private List<String> alleJournalførendeEnheter = new ArrayList<>(); // Med klageinstans og kode6
    private List<String> nfpJournalførendeEnheter = new ArrayList<>(); // Kun NFP
    private LocalDate sisteInnhenting = LocalDate.MIN;

    public LegacyEnhetsTjeneste() {
    }

    @Inject
    public LegacyEnhetsTjeneste(
            PersonInformasjon personTjeneste,
            ArbeidsfordelingRestKlient norgKlient) {
        this.personTjeneste = personTjeneste;
        this.norgKlient = norgKlient;
    }

    @Override
    public String hentFordelingEnhetId(Tema tema, BehandlingTema behandlingTema, Optional<String> enhetInput, String aktørId) {
        oppdaterEnhetCache();
        if (enhetInput.map(alleJournalførendeEnheter::contains).orElse(Boolean.FALSE)) {
            return enhetInput.get();
        }

        return Optional.ofNullable(aktørId).map(a -> hentEnhetId(a, behandlingTema, tema))
                .orElseGet(this::tilfeldigNfpEnhet);
    }

    private String hentEnhetId(String aktørId, BehandlingTema behandlingTema, Tema tema) {
        var gt = personTjeneste.hentPersonIdentForAktørId(aktørId)
                .map(this::hentGeografiskTilknytning)
                .orElse(new GeoTilknytning(null, null));

        if (gt.getDiskresjonskode() == null && gt.getTilknytning() == null) {
            return tilfeldigNfpEnhet();
        }

        var request = ArbeidsfordelingRequest.ny()
                .medTemagruppe(TEMAGRUPPE)
                .medTema(tema.getOffisiellKode())
                .medBehandlingstema(behandlingTema.getOffisiellKode())
                .medBehandlingstype(BEHANDLINGTYPE)
                .medOppgavetype(OPPGAVETYPE_JFR)
                .medDiskresjonskode(gt.getDiskresjonskode())
                .medGeografiskOmraade(gt.getTilknytning())
                .build();
        return validerOgVelgBehandlendeEnhet(norgKlient.finnEnhet(request), gt);
    }

    private static String validerOgVelgBehandlendeEnhet(List<ArbeidsfordelingResponse> response, GeoTilknytning gt) {
        // Vi forventer å få én behandlende enhet.
        if (response == null || response.size() != 1) {
            throw LegacyEnhetsTjeneste.EnhetsTjenesteFeil.FACTORY.finnerIkkeBehandlendeEnhet(gt.getTilknytning(), gt.getDiskresjonskode())
                    .toException();
        }

        return response.get(0).getEnhetNr();
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
                    .medOppgavetype(OPPGAVETYPE_JFR)
                    .build();
            var respons = norgKlient.hentAlleAktiveEnheter(request);
            alleJournalførendeEnheter.clear();
            nfpJournalførendeEnheter.clear();
            respons.stream().map(ArbeidsfordelingResponse::getEnhetNr).forEach(alleJournalførendeEnheter::add);
            respons.stream().filter(e -> ENHET_TYPE_NFP.equalsIgnoreCase(e.getEnhetType()))
                    .map(ArbeidsfordelingResponse::getEnhetNr).forEach(nfpJournalførendeEnheter::add);
            alleJournalførendeEnheter.add(NK_ENHET_ID);
            sisteInnhenting = LocalDate.now();
        }
    }

    private GeoTilknytning hentGeografiskTilknytning(String fnr) {
        return personTjeneste.hentGeografiskTilknytning(fnr);
    }

    private interface EnhetsTjenesteFeil extends DeklarerteFeil {

        LegacyEnhetsTjeneste.EnhetsTjenesteFeil FACTORY = FeilFactory.create(LegacyEnhetsTjeneste.EnhetsTjenesteFeil.class);

        @TekniskFeil(feilkode = "FP-669566", feilmelding = "Finner ikke behandlende enhet for geografisk tilknytning %s, diskresjonskode %s", logLevel = LogLevel.ERROR)
        Feil finnerIkkeBehandlendeEnhet(String geografiskTilknytning, String diskresjonskode);

        @TekniskFeil(feilkode = "FP-070668", feilmelding = "Person ikke funnet ved hentGeografiskTilknytning eller relasjoner", logLevel = LogLevel.ERROR)
        Feil enhetsTjenestePersonIkkeFunnet(Exception e);

        @ManglerTilgangFeil(feilkode = "FP-509290", feilmelding = "Mangler tilgang til å utføre hentGeografiskTilknytning eller hentrelasjoner", logLevel = LogLevel.ERROR)
        Feil enhetsTjenesteSikkerhetsbegrensing(Exception e);
    }
}
