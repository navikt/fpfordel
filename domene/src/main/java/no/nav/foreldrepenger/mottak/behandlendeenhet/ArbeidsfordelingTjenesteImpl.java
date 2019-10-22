package no.nav.foreldrepenger.mottak.behandlendeenhet;

import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.fordel.kodeverk.BehandlingTema;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.binding.FinnAlleBehandlendeEnheterListeUgyldigInput;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.binding.FinnBehandlendeEnhetListeUgyldigInput;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.ArbeidsfordelingKriterier;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.Behandlingstema;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.Diskresjonskoder;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.Enhetstyper;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.Geografi;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.Oppgavetyper;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.Organisasjonsenhet;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.Tema;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.Temagrupper;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.meldinger.FinnAlleBehandlendeEnheterListeRequest;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.meldinger.FinnAlleBehandlendeEnheterListeResponse;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.meldinger.FinnBehandlendeEnhetListeRequest;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.meldinger.FinnBehandlendeEnhetListeResponse;
import no.nav.vedtak.felles.integrasjon.arbeidsfordeling.klient.ArbeidsfordelingConsumer;

@Dependent
public class ArbeidsfordelingTjenesteImpl implements ArbeidsfordelingTjeneste {

    private static final Logger logger = LoggerFactory.getLogger(ArbeidsfordelingTjenesteImpl.class);
    private static final String TEMAGRUPPE = "FMLI";//FMLI = Familie
    private static final String TEMA = "FOR"; //FOR = Foreldre- og svangerskapspenger
    private static final String OPPGAVETYPE_JFR = "JFR"; //
    private static final String ENHET_TYPE_FP = "FPY"; // Normale NFP-enheter , uten Viken og spesialenheter
    private static final String NK_ENHET_ID = "4292";

    private ArbeidsfordelingConsumer consumer;

    @Inject
    public ArbeidsfordelingTjenesteImpl(ArbeidsfordelingConsumer consumer) {
        this.consumer = consumer;
    }

    @Override
    public String finnBehandlendeEnhetId(String geografiskTilknytning, String diskresjonskode, BehandlingTema behandlingTema, no.nav.foreldrepenger.fordel.kodeverk.Tema tema) {
        FinnBehandlendeEnhetListeRequest request = lagRequestForHentBehandlendeEnhet(tema, behandlingTema, diskresjonskode, geografiskTilknytning);

        try {
            FinnBehandlendeEnhetListeResponse response = consumer.finnBehandlendeEnhetListe(request);
            Organisasjonsenhet valgtEnhet = validerOgVelgBehandlendeEnhet(geografiskTilknytning, diskresjonskode, behandlingTema, response);
            return valgtEnhet.getEnhetId();
        } catch (FinnBehandlendeEnhetListeUgyldigInput e) {
            throw ArbeidsfordelingFeil.FACTORY.finnBehandlendeEnhetListeUgyldigInput(e).toException();
        }
    }

    @Override
    public List<String> finnAlleJournalførendeEnhetIdListe(BehandlingTema behandlingTema, boolean medSpesialenheter) {
        FinnAlleBehandlendeEnheterListeRequest request = lagRequestForHentAlleBehandlendeEnheter(behandlingTema, OPPGAVETYPE_JFR, medSpesialenheter);

        try {
            FinnAlleBehandlendeEnheterListeResponse response = consumer.finnAlleBehandlendeEnheterListe(request);
            return tilOrganisasjonsEnhetListe(response, behandlingTema, medSpesialenheter);
        } catch (FinnAlleBehandlendeEnheterListeUgyldigInput e) {
            throw ArbeidsfordelingFeil.FACTORY.finnAlleBehandlendeEnheterListeUgyldigInput(e).toException();
        }
    }

    private FinnBehandlendeEnhetListeRequest lagRequestForHentBehandlendeEnhet(no.nav.foreldrepenger.fordel.kodeverk.Tema tema, BehandlingTema behandlingTema, String diskresjonskode, String geografiskTilknytning) {
        FinnBehandlendeEnhetListeRequest request = new FinnBehandlendeEnhetListeRequest();
        ArbeidsfordelingKriterier kriterier = new ArbeidsfordelingKriterier();

        Temagrupper temagruppe = new Temagrupper();
        temagruppe.setValue(TEMAGRUPPE);
        kriterier.setTemagruppe(temagruppe);

        Tema tem = new Tema();
        tem.setValue(tema.getOffisiellKode());
        kriterier.setTema(tem);

        Behandlingstema behandlingstemaRequestObject = new Behandlingstema();
        behandlingstemaRequestObject.setValue(behandlingTema.getOffisiellKode());
        kriterier.setBehandlingstema(behandlingstemaRequestObject);

        Diskresjonskoder diskresjonskoder = new Diskresjonskoder();
        diskresjonskoder.setValue(diskresjonskode);
        kriterier.setDiskresjonskode(diskresjonskoder);

        Geografi geografi = new Geografi();
        geografi.setValue(geografiskTilknytning);
        kriterier.setGeografiskTilknytning(geografi);

        request.setArbeidsfordelingKriterier(kriterier);
        return request;
    }

    private Organisasjonsenhet validerOgVelgBehandlendeEnhet(String geografiskTilknytning, String diskresjonskode,
                                                             BehandlingTema behandlingTema, FinnBehandlendeEnhetListeResponse response) {
        List<Organisasjonsenhet> behandlendeEnheter = response.getBehandlendeEnhetListe();

        //Vi forventer å få én behandlende enhet.
        if (behandlendeEnheter == null || behandlendeEnheter.isEmpty()) {
            throw ArbeidsfordelingFeil.FACTORY.finnerIkkeBehandlendeEnhet(geografiskTilknytning, diskresjonskode, behandlingTema).toException();
        }

        //Vi forventer å få én behandlende enhet.
        Organisasjonsenhet valgtBehandlendeEnhet = behandlendeEnheter.get(0);
        if (behandlendeEnheter.size() > 1) {
            List<String> enheter = behandlendeEnheter.stream().map(Organisasjonsenhet::getEnhetId).collect(Collectors.toList());
            ArbeidsfordelingFeil.FACTORY.fikkFlereBehandlendeEnheter(geografiskTilknytning, diskresjonskode, behandlingTema, enheter,
                    valgtBehandlendeEnhet.getEnhetId()).log(logger);
        }
        return valgtBehandlendeEnhet;
    }

    private FinnAlleBehandlendeEnheterListeRequest lagRequestForHentAlleBehandlendeEnheter(BehandlingTema behandlingTema, String oppgaveType, boolean alleTyper){
        FinnAlleBehandlendeEnheterListeRequest request = new FinnAlleBehandlendeEnheterListeRequest();
        ArbeidsfordelingKriterier kriterier = new ArbeidsfordelingKriterier();

        Temagrupper temagruppe = new Temagrupper();
        temagruppe.setValue(TEMAGRUPPE);
        kriterier.setTemagruppe(temagruppe);

        Tema tema = new Tema();
        tema.setValue(TEMA);
        kriterier.setTema(tema);

        Oppgavetyper oppgavetyper = new Oppgavetyper();
        oppgavetyper.setValue(oppgaveType);
        kriterier.setOppgavetype(oppgavetyper);

        if (!BehandlingTema.UDEFINERT.equals(behandlingTema)) {
            Behandlingstema behandlingstemaRequestObject = new Behandlingstema();
            behandlingstemaRequestObject.setValue(behandlingTema.getOffisiellKode());
            kriterier.setBehandlingstema(behandlingstemaRequestObject);
        }

        request.setArbeidsfordelingKriterier(kriterier);

        if (!alleTyper) {
            Enhetstyper nfp = new Enhetstyper();
            nfp.setValue(ENHET_TYPE_FP);
            request.getTypeListe().add(nfp);
        }
        return request;
    }

    private List<String> tilOrganisasjonsEnhetListe(FinnAlleBehandlendeEnheterListeResponse response, BehandlingTema behandlingTema, boolean medKlage){
        List<Organisasjonsenhet> responsEnheter = response.getBehandlendeEnhetListe();

        if (responsEnheter == null || responsEnheter.isEmpty()) {
            throw ArbeidsfordelingFeil.FACTORY.finnerIkkeAlleBehandlendeEnheter(behandlingTema).toException();
        }

        List<String> organisasjonsEnhetListe = responsEnheter.stream()
                .map(Organisasjonsenhet::getEnhetId)
                .collect(Collectors.toList());

        // TODO(DIAMANT): Midlertidig hardkodet inn for Klageinstans da den ikke kommer med i response fra NORG. Fjern dette når det er på plass.
        if (medKlage) {
            organisasjonsEnhetListe.add(NK_ENHET_ID);
        }
        return organisasjonsEnhetListe;
    }

}
