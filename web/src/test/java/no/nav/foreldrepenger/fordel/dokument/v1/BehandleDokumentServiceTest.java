package no.nav.foreldrepenger.fordel.dokument.v1;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentMatchers;

import no.nav.foreldrepenger.fordel.kodeverdi.ArkivFilType;
import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentKategori;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverdi.VariantFormat;
import no.nav.foreldrepenger.kontrakter.fordel.FagsakInfomasjonDto;
import no.nav.foreldrepenger.kontrakter.fordel.SaksnummerDto;
import no.nav.foreldrepenger.mottak.journal.JournalDokument;
import no.nav.foreldrepenger.mottak.journal.JournalMetadata;
import no.nav.foreldrepenger.mottak.klient.FagsakRestKlient;
import no.nav.foreldrepenger.mottak.tjeneste.HentDataFraJoarkTjeneste;
import no.nav.foreldrepenger.mottak.tjeneste.KlargjørForVLTjeneste;
import no.nav.foreldrepenger.mottak.tjeneste.TilJournalføringTjeneste;
import no.nav.tjeneste.virksomhet.behandledokumentforsendelse.v1.OppdaterOgFerdigstillJournalfoeringUgyldigInput;
import no.nav.tjeneste.virksomhet.behandledokumentforsendelse.v1.meldinger.OppdaterOgFerdigstillJournalfoeringRequest;
import no.nav.vedtak.exception.FunksjonellException;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumer;

public class BehandleDokumentServiceTest {

    static {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Oslo"));
    }

    private BehandleDokumentService behandleDokumentService;

    private static final String JOURNALPOST_ID = "123";
    private static final String ENHETID = "4567";
    private static final String SAKSNUMMER = "789";
    private static final String AKTØR_ID = "9000000000009";
    private static final String BRUKER_FNR = "99999999899";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private TilJournalføringTjeneste tilJournalføringTjenesteMock;
    private HentDataFraJoarkTjeneste hentDataFraJoarkTjenesteMock;
    private KlargjørForVLTjeneste klargjørForVLTjenesteMock;
    private FagsakRestKlient fagsakRestKlientMock;
    private AktørConsumer aktørConsumer;

    private BehandlingTema engangsstønadFødsel;
    private BehandlingTema foreldrepengerFødsel;
    private BehandlingTema foreldrepengerAdopsjon;
    private BehandlingTema foreldrepenger;
    private BehandlingTema engangsstønad;
    private String navnDokumentTypeId;
    private JournalMetadata journalMetadata;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        engangsstønadFødsel = BehandlingTema.ENGANGSSTØNAD_FØDSEL;
        foreldrepengerFødsel = BehandlingTema.FORELDREPENGER_FØDSEL;
        foreldrepengerAdopsjon = BehandlingTema.FORELDREPENGER_ADOPSJON;
        engangsstønad = BehandlingTema.ENGANGSSTØNAD;
        foreldrepenger = BehandlingTema.FORELDREPENGER;
        DokumentTypeId dokumentTypeId = DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL;
        navnDokumentTypeId = dokumentTypeId.getTermNavn();

        fagsakRestKlientMock = mock(FagsakRestKlient.class);
        when(fagsakRestKlientMock.finnFagsakInfomasjon(ArgumentMatchers.<SaksnummerDto>any()))
                .thenReturn(Optional.of(new FagsakInfomasjonDto(AKTØR_ID, engangsstønad.getOffisiellKode(), false)));

        journalMetadata = mock(JournalMetadata.class);
        when(journalMetadata.getJournaltilstand()).thenReturn(JournalMetadata.Journaltilstand.MIDLERTIDIG);
        when(journalMetadata.getDokumentTypeId()).thenReturn(dokumentTypeId);

        hentDataFraJoarkTjenesteMock = mock(HentDataFraJoarkTjeneste.class);
        when(hentDataFraJoarkTjenesteMock.hentHoveddokumentMetadata(JOURNALPOST_ID))
                .thenReturn(Optional.of(journalMetadata));

        JournalDokument journalDokument = mock(JournalDokument.class);
        when(hentDataFraJoarkTjenesteMock.hentStrukturertJournalDokument(any(JournalMetadata.class)))
                .thenReturn(Optional.of(journalDokument));

        tilJournalføringTjenesteMock = mock(TilJournalføringTjeneste.class);
        klargjørForVLTjenesteMock = mock(KlargjørForVLTjeneste.class);

        aktørConsumer = mock(AktørConsumer.class);
        when(aktørConsumer.hentAktørIdForPersonIdent(any())).thenReturn(Optional.empty());
        when(aktørConsumer.hentAktørIdForPersonIdent(BRUKER_FNR)).thenReturn(Optional.of(AKTØR_ID));

        behandleDokumentService = new BehandleDokumentService(tilJournalføringTjenesteMock,
                hentDataFraJoarkTjenesteMock, klargjørForVLTjenesteMock,
                fagsakRestKlientMock, aktørConsumer);
    }

    @Test
    public void skalValiderePåkrevdInput_enhetId() throws Exception {
        expectedException.expect(OppdaterOgFerdigstillJournalfoeringUgyldigInput.class);
        expectedException.expectMessage(BehandleDokumentService.ENHET_MANGLER);

        OppdaterOgFerdigstillJournalfoeringRequest request = lagRequest(null, JOURNALPOST_ID, SAKSNUMMER);
        behandleDokumentService.oppdaterOgFerdigstillJournalfoering(request);
    }

    @Test
    public void skalValiderePåkrevdInput_journalpostId() throws Exception {
        expectedException.expect(OppdaterOgFerdigstillJournalfoeringUgyldigInput.class);
        expectedException.expectMessage(BehandleDokumentService.JOURNALPOST_MANGLER);

        OppdaterOgFerdigstillJournalfoeringRequest request = lagRequest(ENHETID, null, SAKSNUMMER);
        behandleDokumentService.oppdaterOgFerdigstillJournalfoering(request);
    }

    @Test
    public void skalValiderePåkrevdInput_saksnummer() throws Exception {
        expectedException.expect(OppdaterOgFerdigstillJournalfoeringUgyldigInput.class);
        expectedException.expectMessage(BehandleDokumentService.SAKSNUMMER_UGYLDIG);

        OppdaterOgFerdigstillJournalfoeringRequest request = lagRequest(ENHETID, JOURNALPOST_ID, null);
        behandleDokumentService.oppdaterOgFerdigstillJournalfoering(request);
    }

    @Test
    public void skalValidereAtFagsakFinnes() throws Exception {
        expectedException.expect(FunksjonellException.class);
        expectedException.expectMessage("Kan ikke journalføre på saksnummer");
        when(fagsakRestKlientMock.finnFagsakInfomasjon(any()))
                .thenReturn(Optional.empty());

        OppdaterOgFerdigstillJournalfoeringRequest request = lagRequest(ENHETID, JOURNALPOST_ID, SAKSNUMMER);
        behandleDokumentService.oppdaterOgFerdigstillJournalfoering(request);
    }

    @Test(expected = FunksjonellException.class)
    public void skalIkkeJournalføreKlagerPåSakUtenBehandling() throws Exception {

        OppdaterOgFerdigstillJournalfoeringRequest request = lagRequest(ENHETID, JOURNALPOST_ID, SAKSNUMMER);
        when(fagsakRestKlientMock.finnFagsakInfomasjon(ArgumentMatchers.<SaksnummerDto>any()))
                .thenReturn(Optional
                        .of(new FagsakInfomasjonDto(AKTØR_ID, BehandlingTema.UDEFINERT.getOffisiellKode(), false)));

        when(journalMetadata.getDokumentTypeId()).thenReturn(DokumentTypeId.KLAGE_DOKUMENT);
        when(journalMetadata.getDokumentKategori()).thenReturn(Optional.of(DokumentKategori.KLAGE_ELLER_ANKE));
        behandleDokumentService.oppdaterOgFerdigstillJournalfoering(request);
    }

    @Test
    public void skalKunneJournalføreKlagerPåSakMedBehandling() throws Exception {

        OppdaterOgFerdigstillJournalfoeringRequest request = lagRequest(ENHETID, JOURNALPOST_ID, SAKSNUMMER);

        when(journalMetadata.getDokumentTypeId()).thenReturn(DokumentTypeId.KLAGE_DOKUMENT);
        when(journalMetadata.getDokumentKategori()).thenReturn(Optional.of(DokumentKategori.KLAGE_ELLER_ANKE));
        when(tilJournalføringTjenesteMock.tilJournalføring(any(), any(), any(), any(), any())).thenReturn(true);

        behandleDokumentService.oppdaterOgFerdigstillJournalfoering(request);
    }

    @Test
    public void skalKjøreHeltIgjennomNaarJournaltilstandIkkeErEndelig() throws Exception {
        OppdaterOgFerdigstillJournalfoeringRequest request = lagRequest(ENHETID, JOURNALPOST_ID, SAKSNUMMER);
        when(tilJournalføringTjenesteMock.tilJournalføring(JOURNALPOST_ID, SAKSNUMMER, AKTØR_ID, ENHETID,
                navnDokumentTypeId)).thenReturn(true);
        behandleDokumentService.oppdaterOgFerdigstillJournalfoering(request);

        verify(tilJournalføringTjenesteMock).tilJournalføring(JOURNALPOST_ID, SAKSNUMMER, AKTØR_ID, ENHETID,
                navnDokumentTypeId);
        verify(klargjørForVLTjenesteMock).klargjørForVL(any(), eq(SAKSNUMMER), eq(JOURNALPOST_ID), any(), any(),
                eq(engangsstønadFødsel), any(), any(), any(), any());
    }

    @Test(expected = OppdaterOgFerdigstillJournalfoeringUgyldigInput.class)
    public void skalGiUnntakNårDetFinnesManglerSomIkkeKanRettes() throws Exception {
        OppdaterOgFerdigstillJournalfoeringRequest request = lagRequest(ENHETID, JOURNALPOST_ID, SAKSNUMMER);
        when(tilJournalføringTjenesteMock.tilJournalføring(JOURNALPOST_ID, SAKSNUMMER, AKTØR_ID, ENHETID,
                navnDokumentTypeId)).thenReturn(false);
        behandleDokumentService.oppdaterOgFerdigstillJournalfoering(request);
    }

    @Test
    public void skalTillateJournalførinAvInntektsmeldingForeldrepender() throws Exception {
        OppdaterOgFerdigstillJournalfoeringRequest request = lagRequest(ENHETID, JOURNALPOST_ID, SAKSNUMMER);
        when(tilJournalføringTjenesteMock.tilJournalføring(any(), any(), any(), any(), any())).thenReturn(true);
        DokumentTypeId dokumentTypeId = DokumentTypeId.INNTEKTSMELDING;
        when(journalMetadata.getDokumentTypeId()).thenReturn(dokumentTypeId);
        when(fagsakRestKlientMock.finnFagsakInfomasjon(ArgumentMatchers.<SaksnummerDto>any()))
                .thenReturn(
                        Optional.of(new FagsakInfomasjonDto(AKTØR_ID, foreldrepengerFødsel.getOffisiellKode(), false)));

        JournalMetadata dokument = lagJournalMetadata(DokumentTypeId.INNTEKTSMELDING);
        String xml = readFile("testdata/inntektsmelding-foreldrepenger.xml");
        JournalDokument jdMock = new JournalDokument(dokument, xml);

        doReturn(Optional.of(jdMock)).when(hentDataFraJoarkTjenesteMock).hentStrukturertJournalDokument(any());

        behandleDokumentService.oppdaterOgFerdigstillJournalfoering(request);

        verify(tilJournalføringTjenesteMock).tilJournalføring(JOURNALPOST_ID, SAKSNUMMER, AKTØR_ID, ENHETID,
                dokumentTypeId.getTermNavn());
        verify(klargjørForVLTjenesteMock).klargjørForVL(any(), eq(SAKSNUMMER), eq(JOURNALPOST_ID), any(), any(),
                eq(foreldrepengerFødsel), any(), any(), any(), any());
    }

    @Test(expected = FunksjonellException.class)
    public void skalIkkeTillateJournalførinAvInntektsmeldingSvangerskapspenger() throws Exception {
        OppdaterOgFerdigstillJournalfoeringRequest request = lagRequest(ENHETID, JOURNALPOST_ID, SAKSNUMMER);

        DokumentTypeId dokumentTypeId = DokumentTypeId.INNTEKTSMELDING;
        when(journalMetadata.getDokumentTypeId()).thenReturn(dokumentTypeId);
        when(fagsakRestKlientMock.finnFagsakInfomasjon(ArgumentMatchers.<SaksnummerDto>any()))
                .thenReturn(
                        Optional.of(new FagsakInfomasjonDto(AKTØR_ID, foreldrepengerFødsel.getOffisiellKode(), false)));

        JournalMetadata dokument = lagJournalMetadata(DokumentTypeId.INNTEKTSMELDING);
        String xml = readFile("testdata/inntektsmelding-svangerskapspenger.xml");
        JournalDokument jdMock = new JournalDokument(dokument, xml);

        doReturn(Optional.of(jdMock)).when(hentDataFraJoarkTjenesteMock).hentStrukturertJournalDokument(any());

        behandleDokumentService.oppdaterOgFerdigstillJournalfoering(request);
    }

    @Test(expected = FunksjonellException.class)
    public void skalIkkeTillateJournalførinAvSøknadMedUttakFørGrense() throws Exception {
        OppdaterOgFerdigstillJournalfoeringRequest request = lagRequest(ENHETID, JOURNALPOST_ID, SAKSNUMMER);

        DokumentTypeId dokumentTypeId = DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL;
        when(journalMetadata.getDokumentTypeId()).thenReturn(dokumentTypeId);
        when(fagsakRestKlientMock.finnFagsakInfomasjon(ArgumentMatchers.<SaksnummerDto>any()))
                .thenReturn(Optional.of(new FagsakInfomasjonDto(AKTØR_ID, foreldrepenger.getOffisiellKode(), false)));

        JournalMetadata dokument = lagJournalMetadata(DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL);
        String xml = readFile("testdata/selvb-soeknad-forp-uttak-før-konfigverdi.xml");
        JournalDokument jdMock = new JournalDokument(dokument, xml);

        doReturn(Optional.of(jdMock)).when(hentDataFraJoarkTjenesteMock).hentStrukturertJournalDokument(any());

        try {
            behandleDokumentService.oppdaterOgFerdigstillJournalfoering(request);
        } catch (FunksjonellException e) {
            assertThat(e.getFeil().getLøsningsforslag()).contains("2018");
            throw e;
        }
    }

    @Test(expected = FunksjonellException.class)
    public void skalIkkeTillateJournalførinAvSøknadMedOmsorgFørGrense() throws Exception {
        OppdaterOgFerdigstillJournalfoeringRequest request = lagRequest(ENHETID, JOURNALPOST_ID, SAKSNUMMER);

        DokumentTypeId dokumentTypeId = DokumentTypeId.SØKNAD_FORELDREPENGER_ADOPSJON;
        when(journalMetadata.getDokumentTypeId()).thenReturn(dokumentTypeId);
        when(fagsakRestKlientMock.finnFagsakInfomasjon(ArgumentMatchers.<SaksnummerDto>any()))
                .thenReturn(Optional.of(new FagsakInfomasjonDto(AKTØR_ID, foreldrepenger.getOffisiellKode(), false)));

        JournalMetadata dokument = lagJournalMetadata(DokumentTypeId.SØKNAD_FORELDREPENGER_ADOPSJON);
        String xml = readFile("testdata/fp-adopsjon-far.xml");
        JournalDokument jdMock = new JournalDokument(dokument, xml);

        doReturn(Optional.of(jdMock)).when(hentDataFraJoarkTjenesteMock).hentStrukturertJournalDokument(any());

        try {
            behandleDokumentService.oppdaterOgFerdigstillJournalfoering(request);
        } catch (FunksjonellException e) {
            assertThat(e.getFeil().getLøsningsforslag()).contains("2018");
            throw e;
        }
    }

    @Test
    public void skalTillateJournalførinAvSøknadMedUttakEtterGrense() throws Exception {
        OppdaterOgFerdigstillJournalfoeringRequest request = lagRequest(ENHETID, JOURNALPOST_ID, SAKSNUMMER);
        when(tilJournalføringTjenesteMock.tilJournalføring(any(), any(), any(), any(), any())).thenReturn(true);
        DokumentTypeId dokumentTypeId = DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL;
        when(journalMetadata.getDokumentTypeId()).thenReturn(dokumentTypeId);
        when(fagsakRestKlientMock.finnFagsakInfomasjon(ArgumentMatchers.<SaksnummerDto>any()))
                .thenReturn(Optional.of(new FagsakInfomasjonDto(AKTØR_ID, foreldrepenger.getOffisiellKode(), false)));

        JournalMetadata dokument = lagJournalMetadata(DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL);
        String xml = readFile("testdata/selvb-soeknad-forp.xml");
        JournalDokument jdMock = new JournalDokument(dokument, xml);

        doReturn(Optional.of(jdMock)).when(hentDataFraJoarkTjenesteMock).hentStrukturertJournalDokument(any());

        behandleDokumentService.oppdaterOgFerdigstillJournalfoering(request);

        verify(tilJournalføringTjenesteMock).tilJournalføring(JOURNALPOST_ID, SAKSNUMMER, AKTØR_ID, ENHETID,
                dokumentTypeId.getTermNavn());
        verify(klargjørForVLTjenesteMock).klargjørForVL(any(), eq(SAKSNUMMER), eq(JOURNALPOST_ID), any(), any(),
                eq(foreldrepengerFødsel), any(), any(), any(), any());
    }

    @Test
    public void skalTillateJournalførinAvSøknadMedOmsorgEtterGrense() throws Exception {
        OppdaterOgFerdigstillJournalfoeringRequest request = lagRequest(ENHETID, JOURNALPOST_ID, SAKSNUMMER);
        when(tilJournalføringTjenesteMock.tilJournalføring(any(), any(), any(), any(), any())).thenReturn(true);
        DokumentTypeId dokumentTypeId = DokumentTypeId.SØKNAD_FORELDREPENGER_ADOPSJON;
        when(journalMetadata.getDokumentTypeId()).thenReturn(dokumentTypeId);
        when(fagsakRestKlientMock.finnFagsakInfomasjon(ArgumentMatchers.<SaksnummerDto>any()))
                .thenReturn(Optional.of(new FagsakInfomasjonDto(AKTØR_ID, foreldrepenger.getOffisiellKode(), false)));

        JournalMetadata dokument = lagJournalMetadata(DokumentTypeId.SØKNAD_FORELDREPENGER_ADOPSJON);
        String xml = readFile("testdata/fp-adopsjon-mor.xml");
        JournalDokument jdMock = new JournalDokument(dokument, xml);

        doReturn(Optional.of(jdMock)).when(hentDataFraJoarkTjenesteMock).hentStrukturertJournalDokument(any());

        behandleDokumentService.oppdaterOgFerdigstillJournalfoering(request);

        verify(tilJournalføringTjenesteMock).tilJournalføring(JOURNALPOST_ID, SAKSNUMMER, AKTØR_ID, ENHETID,
                dokumentTypeId.getTermNavn());
        verify(klargjørForVLTjenesteMock).klargjørForVL(any(), eq(SAKSNUMMER), eq(JOURNALPOST_ID), any(), any(),
                eq(foreldrepengerAdopsjon), any(), any(), any(), any());
    }

    @Test
    public void skalTillateJournalførinAvEndringsSøknadMedAnnetSaksnummer() throws Exception {
        OppdaterOgFerdigstillJournalfoeringRequest request = lagRequest(ENHETID, JOURNALPOST_ID, SAKSNUMMER);
        when(tilJournalføringTjenesteMock.tilJournalføring(any(), any(), any(), any(), any())).thenReturn(true);
        DokumentTypeId dokumentTypeId = DokumentTypeId.FORELDREPENGER_ENDRING_SØKNAD;
        when(journalMetadata.getDokumentTypeId()).thenReturn(dokumentTypeId);
        when(fagsakRestKlientMock.finnFagsakInfomasjon(ArgumentMatchers.<SaksnummerDto>any()))
                .thenReturn(Optional.of(new FagsakInfomasjonDto(AKTØR_ID, foreldrepenger.getOffisiellKode(), false)));

        JournalMetadata dokument = lagJournalMetadata(DokumentTypeId.FORELDREPENGER_ENDRING_SØKNAD);
        String xml = readFile("testdata/selvb-soeknad-endring.xml");
        JournalDokument jdMock = new JournalDokument(dokument, xml);

        doReturn(Optional.of(jdMock)).when(hentDataFraJoarkTjenesteMock).hentStrukturertJournalDokument(any());

        behandleDokumentService.oppdaterOgFerdigstillJournalfoering(request);

        verify(tilJournalføringTjenesteMock).tilJournalføring(JOURNALPOST_ID, SAKSNUMMER, AKTØR_ID, ENHETID,
                dokumentTypeId.getTermNavn());
        verify(klargjørForVLTjenesteMock).klargjørForVL(any(), eq(SAKSNUMMER), eq(JOURNALPOST_ID), any(), any(),
                eq(foreldrepenger), any(), any(), any(), any());
    }

    @Test
    public void skalKjøreHeltIgjennomNaarJournaltilstandErEndelig() throws Exception {
        when(journalMetadata.getJournaltilstand()).thenReturn(JournalMetadata.Journaltilstand.ENDELIG);

        OppdaterOgFerdigstillJournalfoeringRequest request = lagRequest(ENHETID, JOURNALPOST_ID, SAKSNUMMER);
        behandleDokumentService.oppdaterOgFerdigstillJournalfoering(request);

        verify(tilJournalføringTjenesteMock, never()).tilJournalføring(any(), any(), any(), any(), any());
        verify(klargjørForVLTjenesteMock).klargjørForVL(any(), eq(SAKSNUMMER), eq(JOURNALPOST_ID), any(), any(),
                eq(engangsstønadFødsel), any(), any(), any(), any());
    }

    private OppdaterOgFerdigstillJournalfoeringRequest lagRequest(String enhetid, String journalpostId,
            String sakId) {
        OppdaterOgFerdigstillJournalfoeringRequest request = new OppdaterOgFerdigstillJournalfoeringRequest();
        request.setEnhetId(enhetid);
        request.setJournalpostId(journalpostId);
        request.setSakId(sakId);
        return request;
    }

    JournalMetadata lagJournalMetadata(DokumentTypeId dokumentTypeId) {
        JournalMetadata.Builder builder = JournalMetadata.builder();
        builder.medJournalpostId(JOURNALPOST_ID);
        builder.medDokumentId(ENHETID);
        builder.medVariantFormat(VariantFormat.FULLVERSJON);
        builder.medDokumentType(dokumentTypeId);
        builder.medDokumentKategori(DokumentKategori.UDEFINERT);
        builder.medArkivFilType(ArkivFilType.XML);
        builder.medErHoveddokument(true);
        builder.medForsendelseMottatt(LocalDate.now());
        builder.medForsendelseMottattTidspunkt(LocalDateTime.now());
        builder.medBrukerIdentListe(Collections.singletonList(BRUKER_FNR));
        return builder.build();
    }

    String readFile(String filename) throws URISyntaxException, IOException {
        Path path = Paths.get(getClass().getClassLoader().getResource(filename).toURI());
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
