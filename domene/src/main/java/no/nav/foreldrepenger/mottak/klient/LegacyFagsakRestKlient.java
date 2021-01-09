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
@Deprecated
public class LegacyFagsakRestKlient implements FagsakTjeneste {
    private static final String DEFAULT_FPSAK_BASE_URI = "http://fpsak";
    private static final String JOURNALPOSTTILKNYTNING_PATH = "/fpsak/api/fordel/fagsak/knyttJournalpost";
    private static final String FAGSAKINFORMASJON_PATH = "/fpsak/api/fordel/fagsak/informasjon";
    private static final String FAGSAK_OPPRETT_PATH = "/fpsak/api/fordel/fagsak/opprett";
    private static final String VURDER_FAGSYSTEM_PATH = "/fpsak/api/fordel/vurderFagsystem";

    private OidcRestClient oidcRestClient;
    private URI endpointSaksinfo;
    private URI endpointOpprett;
    private URI endpointJournalpostknyttning;
    private URI endpointVurderFagsystem;

    public LegacyFagsakRestKlient() {
    }

    @Inject
    public LegacyFagsakRestKlient(OidcRestClient oidcRestClient,
            @KonfigVerdi(value = "fpsak.base.url", defaultVerdi = DEFAULT_FPSAK_BASE_URI) URI endpoint) {
        this.oidcRestClient = oidcRestClient;
        this.endpointSaksinfo = URI.create(endpoint.toString() + FAGSAKINFORMASJON_PATH);
        this.endpointOpprett = URI.create(endpoint.toString() + FAGSAK_OPPRETT_PATH);
        this.endpointJournalpostknyttning = URI.create(endpoint.toString() + JOURNALPOSTTILKNYTNING_PATH);
        this.endpointVurderFagsystem = URI.create(endpoint.toString() + VURDER_FAGSYSTEM_PATH);
    }

    @Override
    public Optional<FagsakInfomasjonDto> finnFagsakInfomasjon(SaksnummerDto saksnummerDto) {
        return oidcRestClient.postReturnsOptional(endpointSaksinfo, saksnummerDto, FagsakInfomasjonDto.class);
    }

    @Override
    public SaksnummerDto opprettSak(OpprettSakDto opprettSakDto) {
        return oidcRestClient.post(endpointOpprett, opprettSakDto, SaksnummerDto.class);
    }

    @Override
    public void knyttSakOgJournalpost(JournalpostKnyttningDto journalpostKnyttningDto) {
        oidcRestClient.post(endpointJournalpostknyttning, journalpostKnyttningDto);
    }

    @Override
    public VurderFagsystemResultat vurderFagsystem(MottakMeldingDataWrapper w) {
        String aktørId = w.getAktørId().get();
        boolean strukturertSøknad = w.erStrukturertDokument().orElse(Boolean.FALSE);
        DokumentTypeId dokumentTypeId = w.getDokumentTypeId().orElse(DokumentTypeId.UDEFINERT);
        DokumentKategori dokumentKategori = w.getDokumentKategori().orElse(DokumentKategori.UDEFINERT);
        String behandlingTemaString = BehandlingTema.UDEFINERT.equals(w.getBehandlingTema())
                ? w.getBehandlingTema().getKode()
                : w.getBehandlingTema().getOffisiellKode();

        VurderFagsystemDto dto = new VurderFagsystemDto(w.getArkivId(), strukturertSøknad, aktørId,
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

        BehandlendeFagsystemDto res = oidcRestClient.post(endpointVurderFagsystem, dto, BehandlendeFagsystemDto.class);
        return new VurderFagsystemResultat(res);
    }

}
