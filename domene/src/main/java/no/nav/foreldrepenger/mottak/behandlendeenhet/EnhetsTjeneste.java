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
import no.nav.foreldrepenger.fordel.kodeverdi.Temagrupper;
import no.nav.foreldrepenger.mottak.person.GeoTilknytning;
import no.nav.foreldrepenger.mottak.person.PersonTjeneste;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;
import no.nav.vedtak.felles.integrasjon.arbeidsfordeling.rest.ArbeidsfordelingRequest;
import no.nav.vedtak.felles.integrasjon.arbeidsfordeling.rest.ArbeidsfordelingResponse;
import no.nav.vedtak.felles.integrasjon.arbeidsfordeling.rest.ArbeidsfordelingRestKlient;

@ApplicationScoped
public class EnhetsTjeneste {

    private PersonTjeneste personConsumer;
    private ArbeidsfordelingRestKlient norgKlient;

    private static final String TEMAGRUPPE = Temagrupper.FAMILIEYTELSER.getKode(); // Kodeverk Temagrupper - dekker FOR + OMS
    private static final String TEMA = Tema.FORELDRE_OG_SVANGERSKAPSPENGER.getOffisiellKode();
    private static final String OPPGAVETYPE_JFR = "JFR"; // Kodeverk Oppgavetyper - NFP , uten spesialenheter
    private static final String ENHET_TYPE_NFP = "FPY"; // Kodeverk EnhetstyperNORG - NFP , uten spesialenheter
    private static final String BEHANDLINGTYPE = "ae0034"; // Kodeverk Behandlingstype, bruker søknad
    private static final String NK_ENHET_ID = "4292";

    private List<String> alleJournalførendeEnheter = new ArrayList<>(); // Med klageinstans og kode6
    private List<String> nfpJournalførendeEnheter = new ArrayList<>(); // Kun NFP
    private LocalDate sisteInnhenting = LocalDate.MIN;

    public EnhetsTjeneste() {
    }

    @Inject
    public EnhetsTjeneste(PersonTjeneste personConsumer,
            ArbeidsfordelingRestKlient norgKlient) {
        this.personConsumer = personConsumer;
        this.norgKlient = norgKlient;
    }

    public String hentFordelingEnhetId(Tema tema, BehandlingTema behandlingTema, Optional<String> enhetInput,
            Optional<String> fnr) {
        oppdaterEnhetCache();
        if (enhetInput.map(alleJournalførendeEnheter::contains).orElse(Boolean.FALSE)) {
            return enhetInput.get();
        }

        return fnr.map(f -> hentEnhetId(f, behandlingTema, tema))
                .orElse(nfpJournalførendeEnheter.get(LocalDateTime.now().getSecond() % nfpJournalførendeEnheter.size()));
    }

    private String hentEnhetId(String fnr, BehandlingTema behandlingTema, Tema tema) {
        GeoTilknytning geoTilknytning = personConsumer.hentGeografiskTilknytning(fnr);

        var request = ArbeidsfordelingRequest.ny()
                .medTemagruppe(TEMAGRUPPE)
                .medTema(tema.getOffisiellKode())
                .medBehandlingstema(behandlingTema.getOffisiellKode())
                .medBehandlingstype(BEHANDLINGTYPE)
                .medOppgavetype(OPPGAVETYPE_JFR)
                .medDiskresjonskode(geoTilknytning.getDiskresjonskode())
                .medGeografiskOmraade(geoTilknytning.getTilknytning())
                .build();
        var respons = norgKlient.finnEnhet(request);
        return validerOgVelgBehandlendeEnhet(respons, geoTilknytning.getDiskresjonskode(), geoTilknytning.getTilknytning());
    }

    private static String validerOgVelgBehandlendeEnhet(List<ArbeidsfordelingResponse> response, String diskresjonskode, String geoTilknytning) {
        // Vi forventer å få én behandlende enhet.
        if ((response == null) || (response.size() != 1)) {
            throw EnhetsTjeneste.EnhetsTjenesteFeil.FACTORY.finnerIkkeBehandlendeEnhet(geoTilknytning, diskresjonskode).toException();
        }

        return response.get(0).getEnhetNr();
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

    private interface EnhetsTjenesteFeil extends DeklarerteFeil {

        EnhetsTjeneste.EnhetsTjenesteFeil FACTORY = FeilFactory.create(EnhetsTjeneste.EnhetsTjenesteFeil.class);

        @TekniskFeil(feilkode = "FP-669566", feilmelding = "Finner ikke behandlende enhet for geografisk tilknytning %s, diskresjonskode %s", logLevel = LogLevel.ERROR)
        Feil finnerIkkeBehandlendeEnhet(String geografiskTilknytning, String diskresjonskode);
    }
}
