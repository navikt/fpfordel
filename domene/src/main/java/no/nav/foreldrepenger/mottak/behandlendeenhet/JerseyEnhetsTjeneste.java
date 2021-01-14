package no.nav.foreldrepenger.mottak.behandlendeenhet;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.Tema;
import no.nav.foreldrepenger.mottak.person.GeoTilknytning;
import no.nav.foreldrepenger.mottak.person.PersonInformasjon;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.NorskIdent;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.PersonIdent;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Personidenter;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentGeografiskTilknytningRequest;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.ManglerTilgangFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;
import no.nav.vedtak.felles.integrasjon.arbeidsfordeling.rest.ArbeidsfordelingRequest;
import no.nav.vedtak.felles.integrasjon.arbeidsfordeling.rest.ArbeidsfordelingResponse;
import no.nav.vedtak.felles.integrasjon.arbeidsfordeling.rest.ArbeidsfordelingRestKlient;

//@ApplicationScoped
public class JerseyEnhetsTjeneste implements EnhetsTjeneste {

    private PersonInformasjon personTjeneste;
    private ArbeidsfordelingRestKlient norgKlient;

    private List<String> alleJournalførendeEnheter = new ArrayList<>(); // Med klageinstans og kode6
    private List<String> nfpJournalførendeEnheter = new ArrayList<>(); // Kun NFP
    private LocalDate sisteInnhenting = LocalDate.MIN;

    public JerseyEnhetsTjeneste() {
    }

    @Inject
    public JerseyEnhetsTjeneste(PersonInformasjon personTjeneste,
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

        return Optional.ofNullable(aktørId)
                .map(a -> hentEnhetId(a, tema, behandlingTema))
                .orElseGet(this::tilfeldigNfpEnhet);
    }

    private String hentEnhetId(String aktørId, Tema tema, BehandlingTema behandlingTema) {
        return personTjeneste.hentPersonIdentForAktørId(aktørId)
                .map(this::hentGeografiskTilknytning)
                .map(gt -> finnEnhet(tema, behandlingTema, gt))
                .orElse(tilfeldigNfpEnhet());

    }

    private static String validerOgVelgBehandlendeEnhet(List<ArbeidsfordelingResponse> response, String diskresjonskode, String geoTilknytning) {
        // Vi forventer å få én behandlende enhet.
        if (response == null || response.size() != 1) {
            throw JerseyEnhetsTjeneste.EnhetsTjenesteFeil.FACTORY.finnerIkkeBehandlendeEnhet(geoTilknytning, diskresjonskode).toException();
        }

        return response.get(0).getEnhetNr();
    }

    private String finnEnhet(Tema tema, BehandlingTema behandlingTema, GeoTilknytning gt) {
        var request = ArbeidsfordelingRequest.ny()
                .medTemagruppe(TEMAGRUPPE)
                .medTema(tema.getOffisiellKode())
                .medBehandlingstema(behandlingTema.getOffisiellKode())
                .medBehandlingstype(BEHANDLINGTYPE)
                .medOppgavetype(OPPGAVETYPE_JFR)
                .medDiskresjonskode(gt.getDiskresjonskode())
                .medGeografiskOmraade(gt.getTilknytning())
                .build();
        var respons = norgKlient.finnEnhet(request);
        return validerOgVelgBehandlendeEnhet(respons, gt.getDiskresjonskode(), gt.getTilknytning());

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
        HentGeografiskTilknytningRequest request = new HentGeografiskTilknytningRequest();
        request.setAktoer(lagPersonIdent(fnr));
        return personTjeneste.hentGeografiskTilknytning(fnr);
    }

    private static PersonIdent lagPersonIdent(String fnr) {
        if ((fnr == null) || fnr.isEmpty()) {
            throw new IllegalArgumentException("Fødselsnummer kan ikke være null eller tomt");
        }

        PersonIdent personIdent = new PersonIdent();
        NorskIdent norskIdent = new NorskIdent();
        norskIdent.setIdent(fnr);

        Personidenter type = new Personidenter();
        type.setValue(erDNr(fnr) ? "DNR" : "FNR");
        norskIdent.setType(type);

        personIdent.setIdent(norskIdent);
        return personIdent;
    }

    private static boolean erDNr(String fnr) {
        // D-nummer kan indentifiseres ved at første siffer er 4 større enn hva som
        // finnes i fødselsnumre
        char førsteTegn = fnr.charAt(0);
        return (førsteTegn >= '4') && (førsteTegn <= '7');
    }

    private interface EnhetsTjenesteFeil extends DeklarerteFeil {

        JerseyEnhetsTjeneste.EnhetsTjenesteFeil FACTORY = FeilFactory.create(JerseyEnhetsTjeneste.EnhetsTjenesteFeil.class);

        @TekniskFeil(feilkode = "FP-669566", feilmelding = "Finner ikke behandlende enhet for geografisk tilknytning %s, diskresjonskode %s", logLevel = LogLevel.ERROR)
        Feil finnerIkkeBehandlendeEnhet(String geografiskTilknytning, String diskresjonskode);

        @TekniskFeil(feilkode = "FP-070668", feilmelding = "Person ikke funnet ved hentGeografiskTilknytning eller relasjoner", logLevel = LogLevel.ERROR)
        Feil enhetsTjenestePersonIkkeFunnet(Exception e);

        @ManglerTilgangFeil(feilkode = "FP-509290", feilmelding = "Mangler tilgang til å utføre hentGeografiskTilknytning eller hentrelasjoner", logLevel = LogLevel.ERROR)
        Feil enhetsTjenesteSikkerhetsbegrensing(Exception e);
    }
}
