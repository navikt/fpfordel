package no.nav.foreldrepenger.mottak.klient;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentKategori;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.kontrakter.fordel.BehandlendeFagsystemDto;
import no.nav.foreldrepenger.kontrakter.fordel.FagsakInfomasjonDto;
import no.nav.foreldrepenger.kontrakter.fordel.JournalpostKnyttningDto;
import no.nav.foreldrepenger.kontrakter.fordel.OpprettSakDto;
import no.nav.foreldrepenger.kontrakter.fordel.SaksnummerDto;
import no.nav.foreldrepenger.kontrakter.fordel.VurderFagsystemDto;
import no.nav.foreldrepenger.mottak.behandlendeenhet.JournalføringsOppgave;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.vedtak.felles.integrasjon.rest.FpApplication;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;

@RestClientConfig(tokenConfig = TokenFlow.ADAPTIVE, application = FpApplication.FPSAK)
@ApplicationScoped
public class FagsakKlient implements Fagsak {

    private static final String JOURNALPOSTTILKNYTNING_PATH = "/api/fordel/fagsak/knyttJournalpost";
    private static final String FAGSAKINFORMASJON_PATH = "/api/fordel/fagsak/informasjon";
    private static final String FAGSAK_OPPRETT_PATH = "/api/fordel/fagsak/opprett";
    private static final String VURDER_FAGSYSTEM_PATH = "/api/fordel/vurderFagsystem";
    private static final String KLAGEINSTANS_FAGSYSTEM_PATH = "/api/fordel/klageinstans";

    private static final String FINN_FAGSAKER_PATH = "/api/fordel/finnFagsaker";
    private static final Logger LOG = LoggerFactory.getLogger(FagsakKlient.class);

    private final URI knytningEndpoint;
    private final URI fagsakinfoEndpoint;
    private final URI opprettsakEndpoint;
    private final URI fagsystemEndpoint;
    private final URI klageinstansEndpoint;

    private final URI finnFagsakerEndpoint;
    private final RestClient klient;
    private final RestConfig restConfig;


    public FagsakKlient() {
        this.klient = RestClient.client();
        this.restConfig = RestConfig.forClient(this.getClass());
        var endpoint = restConfig.fpContextPath();
        this.knytningEndpoint = lagURI(endpoint, JOURNALPOSTTILKNYTNING_PATH);
        this.fagsakinfoEndpoint = lagURI(endpoint, FAGSAKINFORMASJON_PATH);
        this.opprettsakEndpoint = lagURI(endpoint, FAGSAK_OPPRETT_PATH);
        this.fagsystemEndpoint = lagURI(endpoint, VURDER_FAGSYSTEM_PATH);
        this.klageinstansEndpoint = lagURI(endpoint, KLAGEINSTANS_FAGSYSTEM_PATH);
        this.finnFagsakerEndpoint = lagURI(endpoint, FINN_FAGSAKER_PATH);
    }

    @Override
    public Optional<FagsakInfomasjonDto> finnFagsakInfomasjon(SaksnummerDto saksnummerDto) {
        LOG.info("Finner fagsakinformasjon");
        var request= RestRequest.newPOSTJson(saksnummerDto, fagsakinfoEndpoint, restConfig);
        var info = klient.send(request, FagsakInfomasjonDto.class);
        LOG.info("Fant fagsakinformasjon OK");
        return Optional.ofNullable(info);
    }

    @Override
    public SaksnummerDto opprettSak(OpprettSakDto opprettSakDto) {
        LOG.info("Oppretter sak");
        var request = RestRequest.newPOSTJson(opprettSakDto, opprettsakEndpoint, restConfig);
        var sak = klient.send(request, SaksnummerDto.class);
        LOG.info("Opprettet sak OK");
        return sak;
    }

    @Override
    public SaksnummerDto opprettSak(OpprettSakV2Dto opprettSakDto) {
        LOG.info("Oppretter sak");
        var request = RestRequest.newPOSTJson(opprettSakDto, lagURI(opprettsakEndpoint, "/v2"), restConfig);
        var sak = klient.send(request, SaksnummerDto.class);
        LOG.info("Opprettet sak OK");
        return sak;
    }

    @Override
    public void knyttSakOgJournalpost(JournalpostKnyttningDto dto) {
        LOG.info("Knytter sak og journalpost");
        var request = RestRequest.newPOSTJson(dto, knytningEndpoint, restConfig);
        klient.sendReturnOptional(request, String.class);
    }

    @Override
    public VurderFagsystemResultat vurderFagsystem(MottakMeldingDataWrapper w) {
        var aktørId = w.getAktørId().orElseThrow();
        boolean strukturertSøknad = w.erStrukturertDokument().orElse(Boolean.FALSE);
        var dokumentTypeId = w.getDokumentTypeId().orElse(DokumentTypeId.UDEFINERT);
        var dokumentKategori = w.getDokumentKategori().orElse(DokumentKategori.UDEFINERT);
        String behandlingTemaString = BehandlingTema.UDEFINERT.equals(w.getBehandlingTema())
                ? w.getBehandlingTema().getKode()
                : w.getBehandlingTema().getOffisiellKode();

        var dto = new VurderFagsystemDto(w.getArkivId(), strukturertSøknad, aktørId,
                behandlingTemaString);
        dto.setAdopsjonsBarnFodselsdatoer(w.getAdopsjonsbarnFodselsdatoer());
        w.getBarnTermindato().ifPresent(dto::setBarnTermindato);
        w.getBarnFodselsdato().ifPresent(dto::setBarnFodselsdato);
        w.getOmsorgsovertakelsedato().ifPresent(dto::setOmsorgsovertakelsedato);
        w.getÅrsakTilInnsending().ifPresent(dto::setÅrsakInnsendingInntektsmelding);
        w.getAnnenPartId().ifPresent(dto::setAnnenPart);
        w.getSaksnummer().ifPresent(dto::setSaksnummer);
        w.getVirksomhetsnummer().ifPresent(dto::setVirksomhetsnummer);
        w.getArbeidsgiverAktørId().ifPresent(dto::setArbeidsgiverAktørId);
        w.getArbeidsforholdsid().ifPresent(dto::setArbeidsforholdsid);
        w.getInntektsmeldingStartDato().ifPresent(dto::setStartDatoForeldrepengerInntektsmelding);
        w.getForsendelseMottattTidspunkt().ifPresent(dto::setForsendelseMottattTidspunkt);
        dto.setForsendelseMottatt(w.getForsendelseMottatt());
        dto.setDokumentTypeIdOffisiellKode(dokumentTypeId.getOffisiellKode());
        dto.setDokumentKategoriOffisiellKode(dokumentKategori.getOffisiellKode());

        // VurderFagsystemDto burde hatt et felt for første uttaksdag for søknad. For å
        // ikke kaste
        // mottatt søknad til manuell journalføring i fpsak, sender vi her første
        // uttaksdag i et
        // felt som brukes til det samme for inntektsmelding. Kontrakten bør endres
        if (DokumentKategori.SØKNAD.equals(w.getDokumentKategori().orElse(DokumentKategori.UDEFINERT))) {
            w.getFørsteUttaksdag().ifPresent(dto::setStartDatoForeldrepengerInntektsmelding);
        }
        LOG.info("Vurderer resultat");

        var brukPath = w.getJournalførendeEnhet().filter(JournalføringsOppgave.NK_ENHET_ID::equals).isPresent() ?
                klageinstansEndpoint : fagsystemEndpoint;

        var request = RestRequest.newPOSTJson(dto, brukPath, restConfig);
        var respons = klient.send(request, BehandlendeFagsystemDto.class);

        var vurdering = VurderFagsystemResultat.fra(respons);

        LOG.info("Vurderert resultat OK");
        return vurdering;
    }

    @Override
    public List<FagSakInfoDto> hentBrukersSaker(AktørIdDto dto) {
        LOG.info("Henter alle saker for en bruker");
        var target = UriBuilder.fromUri(finnFagsakerEndpoint).build();
        var request = RestRequest.newPOSTJson(dto, target, restConfig);
        return klient.sendReturnList(request, FagSakInfoDto.class);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [endpoint=" + fagsystemEndpoint + "]";
    }

    private URI lagURI(URI context, String api) {
        return UriBuilder.fromUri(context).path(api).build();
    }

}
