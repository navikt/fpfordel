package no.nav.foreldrepenger.fordel.dokument.v1;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentMatchers;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverdi.Journalposttype;
import no.nav.foreldrepenger.fordel.kodeverdi.Journalstatus;
import no.nav.foreldrepenger.kontrakter.fordel.FagsakInfomasjonDto;
import no.nav.foreldrepenger.kontrakter.fordel.SaksnummerDto;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentRepository;
import no.nav.foreldrepenger.mottak.journal.ArkivJournalpost;
import no.nav.foreldrepenger.mottak.journal.ArkivTjeneste;
import no.nav.foreldrepenger.mottak.klient.FagsakRestKlient;
import no.nav.foreldrepenger.mottak.tjeneste.KlargjørForVLTjeneste;
import no.nav.tjeneste.virksomhet.behandledokumentforsendelse.v1.OppdaterOgFerdigstillJournalfoeringUgyldigInput;
import no.nav.tjeneste.virksomhet.behandledokumentforsendelse.v1.meldinger.OppdaterOgFerdigstillJournalfoeringRequest;
import no.nav.vedtak.exception.FunksjonellException;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumerMedCache;

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

    private ArkivTjeneste arkivTjeneste;
    private KlargjørForVLTjeneste klargjørForVLTjenesteMock;
    private FagsakRestKlient fagsakRestKlientMock;
    private AktørConsumerMedCache aktørConsumer;

    private BehandlingTema engangsstønadFødsel;
    private BehandlingTema foreldrepengerFødsel;
    private BehandlingTema foreldrepengerAdopsjon;
    private BehandlingTema foreldrepenger;
    private BehandlingTema engangsstønad;
    private ArkivJournalpost journalpost;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        engangsstønadFødsel = BehandlingTema.ENGANGSSTØNAD_FØDSEL;
        foreldrepengerFødsel = BehandlingTema.FORELDREPENGER_FØDSEL;
        foreldrepengerAdopsjon = BehandlingTema.FORELDREPENGER_ADOPSJON;
        engangsstønad = BehandlingTema.ENGANGSSTØNAD;
        foreldrepenger = BehandlingTema.FORELDREPENGER;
        DokumentTypeId dokumentTypeId = DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL;

        fagsakRestKlientMock = mock(FagsakRestKlient.class);

        when(fagsakRestKlientMock.finnFagsakInfomasjon(ArgumentMatchers.<SaksnummerDto>any()))
                .thenReturn(Optional.of(new FagsakInfomasjonDto(AKTØR_ID, engangsstønad.getOffisiellKode())));

        journalpost = mock(ArkivJournalpost.class);
        when(journalpost.getTilstand()).thenReturn(Journalstatus.MOTTATT);
        when(journalpost.getJournalposttype()).thenReturn(Journalposttype.INNGÅENDE);
        when(journalpost.getHovedtype()).thenReturn(dokumentTypeId);

        arkivTjeneste = mock(ArkivTjeneste.class);
        when(arkivTjeneste.hentArkivJournalpost(JOURNALPOST_ID)).thenReturn(journalpost);

        klargjørForVLTjenesteMock = mock(KlargjørForVLTjeneste.class);

        aktørConsumer = mock(AktørConsumerMedCache.class);
        when(aktørConsumer.hentAktørIdForPersonIdent(any())).thenReturn(Optional.empty());
        when(aktørConsumer.hentAktørIdForPersonIdent(BRUKER_FNR)).thenReturn(Optional.of(AKTØR_ID));
        var dokumentRepository = mock(DokumentRepository.class);

        behandleDokumentService = new BehandleDokumentService(klargjørForVLTjenesteMock,
                fagsakRestKlientMock, aktørConsumer, arkivTjeneste, dokumentRepository);
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
                        .of(new FagsakInfomasjonDto(AKTØR_ID, BehandlingTema.UDEFINERT.getOffisiellKode())));

        when(journalpost.getHovedtype()).thenReturn(DokumentTypeId.KLAGE_DOKUMENT);
        behandleDokumentService.oppdaterOgFerdigstillJournalfoering(request);
    }

    @Test
    public void skalKunneJournalføreKlagerPåSakMedBehandling() throws Exception {

        OppdaterOgFerdigstillJournalfoeringRequest request = lagRequest(ENHETID, JOURNALPOST_ID, SAKSNUMMER);

        when(journalpost.getHovedtype()).thenReturn(DokumentTypeId.KLAGE_DOKUMENT);
        when(arkivTjeneste.oppdaterRettMangler(any(),any(),any(), any())).thenReturn(true);

        behandleDokumentService.oppdaterOgFerdigstillJournalfoering(request);
    }

    @Test(expected = FunksjonellException.class)
    public void skalIkkeJournalførePapirsøknadSakAnnenYtelse() throws Exception {

        OppdaterOgFerdigstillJournalfoeringRequest request = lagRequest(ENHETID, JOURNALPOST_ID, SAKSNUMMER);
        when(fagsakRestKlientMock.finnFagsakInfomasjon(ArgumentMatchers.<SaksnummerDto>any()))
                .thenReturn(Optional
                        .of(new FagsakInfomasjonDto(AKTØR_ID, BehandlingTema.FORELDREPENGER_FØDSEL.getOffisiellKode())));

        when(journalpost.getHovedtype()).thenReturn(DokumentTypeId.SØKNAD_SVANGERSKAPSPENGER);
        behandleDokumentService.oppdaterOgFerdigstillJournalfoering(request);
    }

    @Test
    public void skalKjøreHeltIgjennomNaarJournaltilstandIkkeErEndelig() throws Exception {
        OppdaterOgFerdigstillJournalfoeringRequest request = lagRequest(ENHETID, JOURNALPOST_ID, SAKSNUMMER);
        when(arkivTjeneste.oppdaterRettMangler(any(),any(),any(), any())).thenReturn(true);
        when(journalpost.getTilstand()).thenReturn(Journalstatus.MOTTATT);
        when(journalpost.getJournalpostId()).thenReturn(JOURNALPOST_ID);

        behandleDokumentService.oppdaterOgFerdigstillJournalfoering(request);

        verify(arkivTjeneste).ferdigstillJournalføring(JOURNALPOST_ID, SAKSNUMMER, ENHETID);
        verify(klargjørForVLTjenesteMock).klargjørForVL(any(), eq(SAKSNUMMER), eq(JOURNALPOST_ID), any(), any(),
                eq(engangsstønadFødsel), any(), any(), any(), any());
    }

    @Test(expected = OppdaterOgFerdigstillJournalfoeringUgyldigInput.class)
    public void skalGiUnntakNårDetFinnesManglerSomIkkeKanRettes() throws Exception {
        OppdaterOgFerdigstillJournalfoeringRequest request = lagRequest(ENHETID, JOURNALPOST_ID, SAKSNUMMER);
        behandleDokumentService.oppdaterOgFerdigstillJournalfoering(request);
    }

    @Test
    public void skalTillateJournalførinAvInntektsmeldingForeldrepender() throws Exception {
        OppdaterOgFerdigstillJournalfoeringRequest request = lagRequest(ENHETID, JOURNALPOST_ID, SAKSNUMMER);
        DokumentTypeId dokumentTypeId = DokumentTypeId.INNTEKTSMELDING;
        when(journalpost.getHovedtype()).thenReturn(dokumentTypeId);
        when(fagsakRestKlientMock.finnFagsakInfomasjon(ArgumentMatchers.<SaksnummerDto>any()))
                .thenReturn(
                        Optional.of(new FagsakInfomasjonDto(AKTØR_ID, foreldrepengerFødsel.getOffisiellKode())));

        String xml = readFile("testdata/inntektsmelding-foreldrepenger.xml");

        when(journalpost.getInnholderStrukturertInformasjon()).thenReturn(true);
        when(journalpost.getStrukturertPayload()).thenReturn(xml);
        when(journalpost.getTilstand()).thenReturn(Journalstatus.MOTTATT);
        when(journalpost.getJournalpostId()).thenReturn(JOURNALPOST_ID);
        when(arkivTjeneste.oppdaterRettMangler(any(),any(),any(), any())).thenReturn(true);

        behandleDokumentService.oppdaterOgFerdigstillJournalfoering(request);

        verify(arkivTjeneste).ferdigstillJournalføring(JOURNALPOST_ID, SAKSNUMMER, ENHETID);
        verify(klargjørForVLTjenesteMock).klargjørForVL(eq(xml), eq(SAKSNUMMER), eq(JOURNALPOST_ID), any(), any(),
                eq(foreldrepengerFødsel), any(), any(), any(), any());
    }

    @Test(expected = FunksjonellException.class)
    public void skalIkkeTillateJournalførinAvInntektsmeldingSvangerskapspenger() throws Exception {
        OppdaterOgFerdigstillJournalfoeringRequest request = lagRequest(ENHETID, JOURNALPOST_ID, SAKSNUMMER);

        when(journalpost.getHovedtype()).thenReturn(DokumentTypeId.INNTEKTSMELDING);
        when(fagsakRestKlientMock.finnFagsakInfomasjon(ArgumentMatchers.<SaksnummerDto>any()))
                .thenReturn(Optional.of(new FagsakInfomasjonDto(AKTØR_ID, foreldrepengerFødsel.getOffisiellKode())));

        String xml = readFile("testdata/inntektsmelding-svangerskapspenger.xml");
        when(journalpost.getInnholderStrukturertInformasjon()).thenReturn(true);
        when(journalpost.getStrukturertPayload()).thenReturn(xml);

        behandleDokumentService.oppdaterOgFerdigstillJournalfoering(request);
    }

    @Test(expected = FunksjonellException.class)
    public void skalIkkeTillateJournalførinAvSøknadMedUttakFørGrense() throws Exception {
        OppdaterOgFerdigstillJournalfoeringRequest request = lagRequest(ENHETID, JOURNALPOST_ID, SAKSNUMMER);

        when(journalpost.getHovedtype()).thenReturn(DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL);
        when(fagsakRestKlientMock.finnFagsakInfomasjon(ArgumentMatchers.<SaksnummerDto>any()))
                .thenReturn(Optional.of(new FagsakInfomasjonDto(AKTØR_ID, foreldrepenger.getOffisiellKode())));


        String xml = readFile("testdata/selvb-soeknad-forp-uttak-før-konfigverdi.xml");
        when(journalpost.getStrukturertPayload()).thenReturn(xml);
        when(journalpost.getInnholderStrukturertInformasjon()).thenReturn(true);

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

        when(journalpost.getHovedtype()).thenReturn(DokumentTypeId.SØKNAD_FORELDREPENGER_ADOPSJON);
        when(fagsakRestKlientMock.finnFagsakInfomasjon(ArgumentMatchers.<SaksnummerDto>any()))
                .thenReturn(Optional.of(new FagsakInfomasjonDto(AKTØR_ID, foreldrepenger.getOffisiellKode())));

        String xml = readFile("testdata/fp-adopsjon-far.xml");
        when(journalpost.getStrukturertPayload()).thenReturn(xml);
        when(journalpost.getInnholderStrukturertInformasjon()).thenReturn(true);

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
        DokumentTypeId dokumentTypeId = DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL;
        when(journalpost.getHovedtype()).thenReturn(dokumentTypeId);
        when(fagsakRestKlientMock.finnFagsakInfomasjon(ArgumentMatchers.<SaksnummerDto>any()))
                .thenReturn(Optional.of(new FagsakInfomasjonDto(AKTØR_ID, foreldrepenger.getOffisiellKode())));

        String xml = readFile("testdata/selvb-soeknad-forp.xml");
        when(journalpost.getStrukturertPayload()).thenReturn(xml);
        when(journalpost.getInnholderStrukturertInformasjon()).thenReturn(true);
        when(journalpost.getTilstand()).thenReturn(Journalstatus.MOTTATT);
        when(journalpost.getJournalpostId()).thenReturn(JOURNALPOST_ID);
        when(arkivTjeneste.oppdaterRettMangler(any(),any(),any(), any())).thenReturn(true);

        behandleDokumentService.oppdaterOgFerdigstillJournalfoering(request);

        verify(arkivTjeneste).ferdigstillJournalføring(JOURNALPOST_ID, SAKSNUMMER, ENHETID);
        verify(klargjørForVLTjenesteMock).klargjørForVL(eq(xml), eq(SAKSNUMMER), eq(JOURNALPOST_ID), any(), any(),
                eq(foreldrepengerFødsel), any(), any(), any(), any());
    }

    @Test
    public void skalIgnorereUkjentStrukturertData() throws Exception {
        OppdaterOgFerdigstillJournalfoeringRequest request = lagRequest(ENHETID, JOURNALPOST_ID, SAKSNUMMER);
        DokumentTypeId dokumentTypeId = DokumentTypeId.SØKNAD_KONTANTSTØTTE;
        when(journalpost.getHovedtype()).thenReturn(dokumentTypeId);
        when(fagsakRestKlientMock.finnFagsakInfomasjon(ArgumentMatchers.<SaksnummerDto>any()))
                .thenReturn(Optional.of(new FagsakInfomasjonDto(AKTØR_ID, foreldrepenger.getOffisiellKode())));

        String xml = readFile("testdata/metadata.json");
        when(journalpost.getStrukturertPayload()).thenReturn(xml);
        when(journalpost.getInnholderStrukturertInformasjon()).thenReturn(true);
        when(journalpost.getTilstand()).thenReturn(Journalstatus.MOTTATT);
        when(journalpost.getJournalpostId()).thenReturn(JOURNALPOST_ID);
        when(arkivTjeneste.oppdaterRettMangler(any(),any(),any(), any())).thenReturn(true);

        behandleDokumentService.oppdaterOgFerdigstillJournalfoering(request);

        verify(arkivTjeneste).ferdigstillJournalføring(JOURNALPOST_ID, SAKSNUMMER, ENHETID);
        verify(klargjørForVLTjenesteMock).klargjørForVL(eq(null), eq(SAKSNUMMER), eq(JOURNALPOST_ID), any(), any(),
                eq(foreldrepenger), any(), any(), any(), any());
    }

    @Test
    public void skalTillateJournalførinAvSøknadMedOmsorgEtterGrense() throws Exception {
        OppdaterOgFerdigstillJournalfoeringRequest request = lagRequest(ENHETID, JOURNALPOST_ID, SAKSNUMMER);
        DokumentTypeId dokumentTypeId = DokumentTypeId.SØKNAD_FORELDREPENGER_ADOPSJON;
        when(journalpost.getHovedtype()).thenReturn(dokumentTypeId);
        when(fagsakRestKlientMock.finnFagsakInfomasjon(ArgumentMatchers.<SaksnummerDto>any()))
                .thenReturn(Optional.of(new FagsakInfomasjonDto(AKTØR_ID, foreldrepenger.getOffisiellKode())));

        String xml = readFile("testdata/fp-adopsjon-mor.xml");
        when(journalpost.getStrukturertPayload()).thenReturn(xml);
        when(journalpost.getInnholderStrukturertInformasjon()).thenReturn(true);
        when(journalpost.getTilstand()).thenReturn(Journalstatus.MOTTATT);
        when(journalpost.getJournalpostId()).thenReturn(JOURNALPOST_ID);
        when(arkivTjeneste.oppdaterRettMangler(any(),any(),any(), any())).thenReturn(true);

        behandleDokumentService.oppdaterOgFerdigstillJournalfoering(request);

        verify(arkivTjeneste).ferdigstillJournalføring(JOURNALPOST_ID, SAKSNUMMER, ENHETID);
        verify(klargjørForVLTjenesteMock).klargjørForVL(eq(xml), eq(SAKSNUMMER), eq(JOURNALPOST_ID), any(), any(),
                eq(foreldrepengerAdopsjon), any(), any(), any(), any());
    }

    @Test
    public void skalTillateJournalførinAvEndringsSøknadMedAnnetSaksnummer() throws Exception {
        OppdaterOgFerdigstillJournalfoeringRequest request = lagRequest(ENHETID, JOURNALPOST_ID, SAKSNUMMER);
        DokumentTypeId dokumentTypeId = DokumentTypeId.FORELDREPENGER_ENDRING_SØKNAD;
        when(journalpost.getHovedtype()).thenReturn(dokumentTypeId);
        when(fagsakRestKlientMock.finnFagsakInfomasjon(ArgumentMatchers.<SaksnummerDto>any()))
                .thenReturn(Optional.of(new FagsakInfomasjonDto(AKTØR_ID, foreldrepenger.getOffisiellKode())));

        String xml = readFile("testdata/selvb-soeknad-endring.xml");
        when(journalpost.getStrukturertPayload()).thenReturn(xml);
        when(journalpost.getInnholderStrukturertInformasjon()).thenReturn(true);
        when(journalpost.getTilstand()).thenReturn(Journalstatus.MOTTATT);
        when(journalpost.getJournalpostId()).thenReturn(JOURNALPOST_ID);
        when(arkivTjeneste.oppdaterRettMangler(any(),any(),any(), any())).thenReturn(true);

        behandleDokumentService.oppdaterOgFerdigstillJournalfoering(request);

        verify(arkivTjeneste).ferdigstillJournalføring(JOURNALPOST_ID, SAKSNUMMER, ENHETID);
        verify(klargjørForVLTjenesteMock).klargjørForVL(eq(xml), eq(SAKSNUMMER), eq(JOURNALPOST_ID), any(), any(),
                eq(foreldrepenger), any(), any(), any(), any());
    }

    @Test
    public void skalKjøreHeltIgjennomNaarJournaltilstandErEndelig() throws Exception {
        when(journalpost.getTilstand()).thenReturn(Journalstatus.JOURNALFOERT);

        OppdaterOgFerdigstillJournalfoeringRequest request = lagRequest(ENHETID, JOURNALPOST_ID, SAKSNUMMER);
        behandleDokumentService.oppdaterOgFerdigstillJournalfoering(request);

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

    String readFile(String filename) throws URISyntaxException, IOException {
        Path path = Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource(filename)).toURI());
        return Files.readString(path);
    }
}
