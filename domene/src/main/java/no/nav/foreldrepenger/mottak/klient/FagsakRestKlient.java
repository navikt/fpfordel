package no.nav.foreldrepenger.mottak.klient;

import java.net.URI;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentKategori;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.kontrakter.fordel.BehandlendeFagsystemDto;
import no.nav.foreldrepenger.kontrakter.fordel.FagsakInfomasjonDto;
import no.nav.foreldrepenger.kontrakter.fordel.JournalpostKnyttningDto;
import no.nav.foreldrepenger.kontrakter.fordel.OpprettSakDto;
import no.nav.foreldrepenger.kontrakter.fordel.SaksnummerDto;
import no.nav.foreldrepenger.kontrakter.fordel.VurderFagsystemDto;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
public class FagsakRestKlient {
    private static final String DEFAULT_FPSAK_BASE_URI = "http://fpsak";
    private static final String DEFAULT_JOURNALPOSTTILKNYTNING_PATH = "/fpsak/api/fordel/fagsak/knyttJournalpost";
    private static final String DEFAULT_FAGSAKINFORMASJON_PATH = "/fpsak/api/fordel/fagsak/informasjon";
    private static final String DEFAULT_FAGSAK_OPPRETT_PATH = "/fpsak/api/fordel/fagsak/opprett";
    private static final String DEFAULT_VURDER_FAGSYSTEM_PATH = "/fpsak/api/fordel/vurderFagsystem";

    private OidcRestClient oidcRestClient;
    private URI endpointSaksinfo;
    private URI endpointOpprett;
    private URI endpointJournalpostknyttning;
    private URI endpointVurderFagsystem;

    public FagsakRestKlient() {
    }

    @Inject
    public FagsakRestKlient(OidcRestClient oidcRestClient,
            @KonfigVerdi(value = "fpsak.base.url", defaultVerdi = DEFAULT_FPSAK_BASE_URI) URI endpoint) {
        this.oidcRestClient = oidcRestClient;
        this.endpointSaksinfo = URI.create(endpoint.toString() + DEFAULT_FAGSAKINFORMASJON_PATH);
        this.endpointOpprett = URI.create(endpoint.toString() + DEFAULT_FAGSAK_OPPRETT_PATH);
        this.endpointJournalpostknyttning = URI.create(endpoint.toString() + DEFAULT_JOURNALPOSTTILKNYTNING_PATH);
        this.endpointVurderFagsystem = URI.create(endpoint.toString() + DEFAULT_VURDER_FAGSYSTEM_PATH);
    }

    public Optional<FagsakInfomasjonDto> finnFagsakInfomasjon(SaksnummerDto saksnummerDto) {
        return oidcRestClient.postReturnsOptional(endpointSaksinfo, saksnummerDto, FagsakInfomasjonDto.class);
    }

    public SaksnummerDto opprettSak(OpprettSakDto opprettSakDto) {
        return oidcRestClient.post(endpointOpprett, opprettSakDto, SaksnummerDto.class);
    }

    public void knyttSakOgJournalpost(JournalpostKnyttningDto journalpostKnyttningDto) {
        oidcRestClient.post(endpointJournalpostknyttning, journalpostKnyttningDto);
    }

    public VurderFagsystemResultat vurderFagsystem(MottakMeldingDataWrapper dataWrapper) {
        String aktørId = dataWrapper.getAktørId().get();
        boolean strukturertSøknad = dataWrapper.erStrukturertDokument().orElse(Boolean.FALSE);
        DokumentTypeId dokumentTypeId = dataWrapper.getDokumentTypeId().orElse(DokumentTypeId.UDEFINERT);
        DokumentKategori dokumentKategori = dataWrapper.getDokumentKategori().orElse(DokumentKategori.UDEFINERT);
        String behandlingTemaString = BehandlingTema.UDEFINERT.equals(dataWrapper.getBehandlingTema())
                ? dataWrapper.getBehandlingTema().getKode()
                : dataWrapper.getBehandlingTema().getOffisiellKode();

        VurderFagsystemDto dto = new VurderFagsystemDto(dataWrapper.getArkivId(), strukturertSøknad, aktørId,
                behandlingTemaString);
        dto.setAdopsjonsBarnFodselsdatoer(dataWrapper.getAdopsjonsbarnFodselsdatoer());
        dataWrapper.getBarnTermindato().ifPresent(dto::setBarnTermindato);
        dataWrapper.getBarnFodselsdato().ifPresent(dto::setBarnFodselsdato);
        dataWrapper.getOmsorgsovertakelsedato().ifPresent(dto::setOmsorgsovertakelsedato);
        dataWrapper.getÅrsakTilInnsending().ifPresent(dto::setÅrsakInnsendingInntektsmelding);
        dataWrapper.getAnnenPartId().ifPresent(dto::setAnnenPart);
        dataWrapper.getSaksnummer().ifPresent(dto::setSaksnummer);
        dataWrapper.getVirksomhetsnummer().ifPresent(dto::setVirksomhetsnummer);
        dataWrapper.getArbeidsgiverAktørId().ifPresent(dto::setArbeidsgiverAktørId);
        dataWrapper.getArbeidsforholdsid().ifPresent(dto::setArbeidsforholdsid);
        dataWrapper.getInntektsmeldingStartDato().ifPresent(dto::setStartDatoForeldrepengerInntektsmelding);
        dataWrapper.getForsendelseMottattTidspunkt().ifPresent(dto::setForsendelseMottattTidspunkt);
        dto.setForsendelseMottatt(dataWrapper.getForsendelseMottatt());
        dto.setDokumentTypeIdOffisiellKode(dokumentTypeId.getOffisiellKode());
        dto.setDokumentKategoriOffisiellKode(dokumentKategori.getOffisiellKode());

        // VurderFagsystemDto burde hatt et felt for første uttaksdag for søknad. For å
        // ikke kaste
        // mottatt søknad til manuell journalføring i fpsak, sender vi her første
        // uttaksdag i et
        // felt som brukes til det samme for inntektsmelding. Kontrakten bør endres
        if (DokumentKategori.SØKNAD.equals(dataWrapper.getDokumentKategori().orElse(DokumentKategori.UDEFINERT))) {
            dataWrapper.getFørsteUttaksdag().ifPresent(dto::setStartDatoForeldrepengerInntektsmelding);
        }

        BehandlendeFagsystemDto res = oidcRestClient.post(endpointVurderFagsystem, dto, BehandlendeFagsystemDto.class);
        return new VurderFagsystemResultat(res);
    }

}
