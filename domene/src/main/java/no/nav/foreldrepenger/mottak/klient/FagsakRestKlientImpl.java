package no.nav.foreldrepenger.mottak.klient;

import java.net.URI;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.fordel.kodeverk.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverk.DokumentKategori;
import no.nav.foreldrepenger.fordel.kodeverk.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverk.KodeverkRepository;
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
public class FagsakRestKlientImpl implements FagsakRestKlient {
    private static final String ENDPOINT_KEY_FINN_FAGSAK_INFOMASJON = "fpsak_saksinformasjon.url";
    private static final String ENDPOINT_KEY_OPPRETT_SAK = "fpsak_opprett_sak.url";
    private static final String ENDPOINT_KEY_KNYTT_SAK_OG_JOURNALPOST = "fpsak_journalpostknyttning.url";
    private static final String ENDPOINT_KEY_VURDER_FAGSYSTEM = "fpsak_vurderFagsystem.url";

    private OidcRestClient oidcRestClient;
    private KodeverkRepository kodeverkRepository;
    private URI endpointSaksinfo;
    private URI endpointOpprett;
    private URI endpointJournalpostknyttning;
    private URI endpointVurderFagsystem;

    public FagsakRestKlientImpl() {
    }

    @Inject
    public FagsakRestKlientImpl(OidcRestClient oidcRestClient, KodeverkRepository kodeverkRepository,
            @KonfigVerdi(ENDPOINT_KEY_FINN_FAGSAK_INFOMASJON) URI endpoint,
            @KonfigVerdi(ENDPOINT_KEY_OPPRETT_SAK) URI endpoint2,
            @KonfigVerdi(ENDPOINT_KEY_KNYTT_SAK_OG_JOURNALPOST) URI endpoint3,
            @KonfigVerdi(ENDPOINT_KEY_VURDER_FAGSYSTEM) URI endpoint4) {
        this.oidcRestClient = oidcRestClient;
        this.kodeverkRepository = kodeverkRepository;
        this.endpointSaksinfo = endpoint;
        this.endpointOpprett = endpoint2;
        this.endpointJournalpostknyttning = endpoint3;
        this.endpointVurderFagsystem = endpoint4;
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
    public VurderFagsystemResultat vurderFagsystem(MottakMeldingDataWrapper dataWrapper) {
        String aktørId = dataWrapper.getAktørId().get();
        boolean strukturertSøknad = dataWrapper.erStrukturertDokument().orElse(Boolean.FALSE);
        DokumentTypeId dokumentTypeId = dataWrapper.getDokumentTypeId()
                .map(id -> kodeverkRepository.finn(DokumentTypeId.class, id)).orElse(DokumentTypeId.UDEFINERT);
        DokumentKategori dokumentKategori = dataWrapper.getDokumentKategori()
                .map(kat -> kodeverkRepository.finn(DokumentKategori.class, kat)).orElse(DokumentKategori.UDEFINERT);
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
