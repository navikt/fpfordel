package no.nav.foreldrepenger.mottak.klient;

import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

import java.net.URI;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.client.ClientRequestFilter;

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
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyOidcRestClient;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
public class JerseyFagsakRestKlient extends AbstractJerseyOidcRestClient implements FagsakTjeneste {
    private static final String DEFAULT_FPSAK_BASE_URI = "http://fpsak";
    private static final String JOURNALPOSTTILKNYTNING_PATH = "/fpsak/api/fordel/fagsak/knyttJournalpost";
    private static final String FAGSAKINFORMASJON_PATH = "/fpsak/api/fordel/fagsak/informasjon";
    private static final String FAGSAK_OPPRETT_PATH = "/fpsak/api/fordel/fagsak/opprett";
    private static final String VURDER_FAGSYSTEM_PATH = "/fpsak/api/fordel/vurderFagsystem";

    private static final Logger LOG = LoggerFactory.getLogger(JerseyFagsakRestKlient.class);

    private URI endpoint;

    public JerseyFagsakRestKlient() {
    }

    @Inject
    public JerseyFagsakRestKlient(@KonfigVerdi(value = "fpsak.base.url", defaultVerdi = DEFAULT_FPSAK_BASE_URI) URI endpoint) {
        this(endpoint, new ClientRequestFilter[0]);
    }

    JerseyFagsakRestKlient(URI endpoint, ClientRequestFilter... filters) {
        super(filters);
        this.endpoint = endpoint;
    }

    @Override
    public Optional<FagsakInfomasjonDto> finnFagsakInfomasjon(SaksnummerDto saksnummerDto) {
        LOG.info("Finner fagsakinformasjon");
        var info = client.target(endpoint)
                .path(FAGSAKINFORMASJON_PATH)
                .request(APPLICATION_JSON_TYPE)
                .buildPost(json(saksnummerDto))
                .invoke(FagsakInfomasjonDto.class);
        LOG.info("Fant fagsakinformasjon OK");
        return Optional.of(info);
    }

    @Override
    public SaksnummerDto opprettSak(OpprettSakDto opprettSakDto) {
        LOG.info("Oppretter sak");
        var sak = client.target(endpoint)
                .path(FAGSAK_OPPRETT_PATH)
                .request(APPLICATION_JSON_TYPE)
                .buildPost(json(opprettSakDto))
                .invoke(SaksnummerDto.class);
        LOG.info("Opprettet sak OK");
        return sak;
    }

    @Override
    public void knyttSakOgJournalpost(JournalpostKnyttningDto journalpostKnyttningDto) {
        LOG.info("Knytter sak og journalpost");
        client.target(endpoint)
                .path(JOURNALPOSTTILKNYTNING_PATH)
                .request(APPLICATION_JSON_TYPE)
                .buildPost(json(journalpostKnyttningDto))
                .invoke(Void.class);
        LOG.info("Knyttet sak og journalpost OK");
    }

    @Override
    public VurderFagsystemResultat vurderFagsystem(MottakMeldingDataWrapper w) {
        String aktørId = w.getAktørId().get();
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
        var res = client.target(endpoint)
                .path(VURDER_FAGSYSTEM_PATH)
                .request(APPLICATION_JSON_TYPE)
                .buildPost(json(dto))
                .invoke(BehandlendeFagsystemDto.class);
        LOG.info("Vurderert resultat OK");
        return new VurderFagsystemResultat(res);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [endpoint=" + endpoint + "]";
    }

}
