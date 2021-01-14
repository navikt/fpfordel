package no.nav.foreldrepenger.fordel.dokument.v1;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverdi.Journalposttype;
import no.nav.foreldrepenger.fordel.kodeverdi.Journalstatus;
import no.nav.foreldrepenger.kontrakter.fordel.FagsakInfomasjonDto;
import no.nav.foreldrepenger.kontrakter.fordel.SaksnummerDto;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentRepository;
import no.nav.foreldrepenger.mottak.journal.ArkivJournalpost;
import no.nav.foreldrepenger.mottak.journal.ArkivTjeneste;
import no.nav.foreldrepenger.mottak.klient.FagsakTjeneste;
import no.nav.foreldrepenger.mottak.person.PersonInformasjon;
import no.nav.foreldrepenger.mottak.tjeneste.KlargjørForVLTjeneste;
import no.nav.tjeneste.virksomhet.behandledokumentforsendelse.v1.OppdaterOgFerdigstillJournalfoeringUgyldigInput;
import no.nav.tjeneste.virksomhet.behandledokumentforsendelse.v1.meldinger.OppdaterOgFerdigstillJournalfoeringRequest;
import no.nav.vedtak.exception.FunksjonellException;

@ExtendWith(MockitoExtension.class)
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

    @Mock
    private ArkivTjeneste arkivTjeneste;
    @Mock
    private KlargjørForVLTjeneste klargjørForVLTjenesteMock;
    @Mock
    private FagsakTjeneste fagsakRestKlientMock;
    @Mock
    private PersonInformasjon aktørConsumer;

    private BehandlingTema engangsstønadFødsel;
    private BehandlingTema foreldrepengerFødsel;
    private BehandlingTema foreldrepengerAdopsjon;
    private BehandlingTema foreldrepenger;
    private BehandlingTema engangsstønad;
    @Mock
    private ArkivJournalpost journalpost;

    @BeforeEach
    public void setUp() {
        engangsstønadFødsel = BehandlingTema.ENGANGSSTØNAD_FØDSEL;
        foreldrepengerFødsel = BehandlingTema.FORELDREPENGER_FØDSEL;
        foreldrepengerAdopsjon = BehandlingTema.FORELDREPENGER_ADOPSJON;
        engangsstønad = BehandlingTema.ENGANGSSTØNAD;
        foreldrepenger = BehandlingTema.FORELDREPENGER;
        DokumentTypeId dokumentTypeId = DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL;

        lenient().when(fagsakRestKlientMock.finnFagsakInfomasjon(ArgumentMatchers.<SaksnummerDto>any()))
                .thenReturn(Optional.of(new FagsakInfomasjonDto(AKTØR_ID, engangsstønad.getOffisiellKode())));

        lenient().when(journalpost.getTilstand()).thenReturn(Journalstatus.MOTTATT);
        lenient().when(journalpost.getJournalposttype()).thenReturn(Journalposttype.INNGÅENDE);
        lenient().when(journalpost.getHovedtype()).thenReturn(dokumentTypeId);

        lenient().when(arkivTjeneste.hentArkivJournalpost(JOURNALPOST_ID)).thenReturn(journalpost);

        when(aktørConsumer.hentAktørIdForPersonIdent(any())).thenReturn(Optional.empty());
        lenient().when(aktørConsumer.hentAktørIdForPersonIdent(BRUKER_FNR)).thenReturn(Optional.of(AKTØR_ID));

        behandleDokumentService = new BehandleDokumentService(klargjørForVLTjenesteMock,
                fagsakRestKlientMock, aktørConsumer, arkivTjeneste, mock(DokumentRepository.class));
    }

    @Test
    public void skalValiderePåkrevdInput_enhetId() {
        var e = assertThrows(OppdaterOgFerdigstillJournalfoeringUgyldigInput.class,
                () -> behandleDokumentService.oppdaterOgFerdigstillJournalfoering(lagRequest(null, JOURNALPOST_ID, SAKSNUMMER)));
        assertThat(e.getMessage().contains(BehandleDokumentService.ENHET_MANGLER));
    }

    @Test
    public void skalValiderePåkrevdInput_journalpostId() {
        var e = assertThrows(OppdaterOgFerdigstillJournalfoeringUgyldigInput.class,
                () -> behandleDokumentService.oppdaterOgFerdigstillJournalfoering(lagRequest(ENHETID, null, SAKSNUMMER)));
        assertThat(e.getMessage().contains(BehandleDokumentService.JOURNALPOST_MANGLER));
    }

    @Test
    public void skalValiderePåkrevdInput_saksnummer() {
        var e = assertThrows(OppdaterOgFerdigstillJournalfoeringUgyldigInput.class,
                () -> behandleDokumentService.oppdaterOgFerdigstillJournalfoering(lagRequest(ENHETID, JOURNALPOST_ID, null)));
        assertThat(e.getMessage().contains(BehandleDokumentService.SAKSNUMMER_UGYLDIG));
    }

    @Test
    public void skalValidereAtFagsakFinnes() {
        when(fagsakRestKlientMock.finnFagsakInfomasjon(any()))
                .thenReturn(Optional.empty());
        var e = assertThrows(FunksjonellException.class,
                () -> behandleDokumentService.oppdaterOgFerdigstillJournalfoering(lagRequest(ENHETID, JOURNALPOST_ID, SAKSNUMMER)));
        assertThat(e.getMessage().contains("Kan ikke journalføre på saksnummer"));

    }

    @Test
    public void skalIkkeJournalføreKlagerPåSakUtenBehandling() {

        when(fagsakRestKlientMock.finnFagsakInfomasjon(ArgumentMatchers.<SaksnummerDto>any()))
                .thenReturn(Optional
                        .of(new FagsakInfomasjonDto(AKTØR_ID, BehandlingTema.UDEFINERT.getOffisiellKode())));

        when(journalpost.getHovedtype()).thenReturn(DokumentTypeId.KLAGE_DOKUMENT);
        assertThrows(FunksjonellException.class,
                () -> behandleDokumentService.oppdaterOgFerdigstillJournalfoering(lagRequest(ENHETID, JOURNALPOST_ID, SAKSNUMMER)));
    }

    @Test
    public void skalKunneJournalføreKlagerPåSakMedBehandling() throws Exception {

        OppdaterOgFerdigstillJournalfoeringRequest request = lagRequest(ENHETID, JOURNALPOST_ID, SAKSNUMMER);

        when(journalpost.getHovedtype()).thenReturn(DokumentTypeId.KLAGE_DOKUMENT);
        when(arkivTjeneste.oppdaterRettMangler(any(), any(), any(), any())).thenReturn(true);

        behandleDokumentService.oppdaterOgFerdigstillJournalfoering(request);
    }

    @Test
    public void skalIkkeJournalførePapirsøknadSakAnnenYtelse() {

        when(fagsakRestKlientMock.finnFagsakInfomasjon(ArgumentMatchers.<SaksnummerDto>any()))
                .thenReturn(Optional
                        .of(new FagsakInfomasjonDto(AKTØR_ID, BehandlingTema.FORELDREPENGER_FØDSEL.getOffisiellKode())));

        when(journalpost.getHovedtype()).thenReturn(DokumentTypeId.SØKNAD_SVANGERSKAPSPENGER);
        assertThrows(FunksjonellException.class,
                () -> behandleDokumentService.oppdaterOgFerdigstillJournalfoering(lagRequest(ENHETID, JOURNALPOST_ID, SAKSNUMMER)));
    }

    @Test
    public void skalKjøreHeltIgjennomNaarJournaltilstandIkkeErEndelig() throws Exception {
        OppdaterOgFerdigstillJournalfoeringRequest request = lagRequest(ENHETID, JOURNALPOST_ID, SAKSNUMMER);
        when(arkivTjeneste.oppdaterRettMangler(any(), any(), any(), any())).thenReturn(true);
        when(journalpost.getTilstand()).thenReturn(Journalstatus.MOTTATT);
        when(journalpost.getJournalpostId()).thenReturn(JOURNALPOST_ID);

        behandleDokumentService.oppdaterOgFerdigstillJournalfoering(request);

        verify(arkivTjeneste).oppdaterMedSak(JOURNALPOST_ID, SAKSNUMMER);
        verify(arkivTjeneste).ferdigstillJournalføring(JOURNALPOST_ID, ENHETID);
        verify(klargjørForVLTjenesteMock).klargjørForVL(any(), eq(SAKSNUMMER), eq(JOURNALPOST_ID), any(), any(),
                eq(engangsstønadFødsel), any(), any(), any(), any());
    }

    @Test
    public void skalGiUnntakNårDetFinnesManglerSomIkkeKanRettes() {
        assertThrows(OppdaterOgFerdigstillJournalfoeringUgyldigInput.class,
                () -> behandleDokumentService.oppdaterOgFerdigstillJournalfoering(lagRequest(ENHETID, JOURNALPOST_ID, SAKSNUMMER)));
    }

    @Test
    public void skalTillateJournalførinAvInntektsmeldingForeldrepender() throws Exception {
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
        when(arkivTjeneste.oppdaterRettMangler(any(), any(), any(), any())).thenReturn(true);

        behandleDokumentService.oppdaterOgFerdigstillJournalfoering(lagRequest(ENHETID, JOURNALPOST_ID, SAKSNUMMER));

        verify(arkivTjeneste).oppdaterMedSak(JOURNALPOST_ID, SAKSNUMMER);
        verify(arkivTjeneste).ferdigstillJournalføring(JOURNALPOST_ID, ENHETID);
        verify(klargjørForVLTjenesteMock).klargjørForVL(eq(xml), eq(SAKSNUMMER), eq(JOURNALPOST_ID), any(), any(),
                eq(foreldrepengerFødsel), any(), any(), any(), any());
    }

    @Test
    public void skalIkkeTillateJournalførinAvInntektsmeldingSvangerskapspenger() throws Exception {
        when(journalpost.getHovedtype()).thenReturn(DokumentTypeId.INNTEKTSMELDING);
        when(fagsakRestKlientMock.finnFagsakInfomasjon(ArgumentMatchers.<SaksnummerDto>any()))
                .thenReturn(Optional.of(new FagsakInfomasjonDto(AKTØR_ID, foreldrepengerFødsel.getOffisiellKode())));

        String xml = readFile("testdata/inntektsmelding-svangerskapspenger.xml");
        when(journalpost.getInnholderStrukturertInformasjon()).thenReturn(true);
        when(journalpost.getStrukturertPayload()).thenReturn(xml);

        assertThrows(FunksjonellException.class,
                () -> behandleDokumentService.oppdaterOgFerdigstillJournalfoering(lagRequest(ENHETID, JOURNALPOST_ID, SAKSNUMMER)));
    }

    @Test
    public void skalIkkeTillateJournalførinAvSøknadMedUttakFørGrense() throws Exception {
        when(journalpost.getHovedtype()).thenReturn(DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL);
        when(fagsakRestKlientMock.finnFagsakInfomasjon(ArgumentMatchers.<SaksnummerDto>any()))
                .thenReturn(Optional.of(new FagsakInfomasjonDto(AKTØR_ID, foreldrepenger.getOffisiellKode())));

        String xml = readFile("testdata/selvb-soeknad-forp-uttak-før-konfigverdi.xml");
        when(journalpost.getStrukturertPayload()).thenReturn(xml);
        when(journalpost.getInnholderStrukturertInformasjon()).thenReturn(true);
        var e = assertThrows(FunksjonellException.class,
                () -> behandleDokumentService.oppdaterOgFerdigstillJournalfoering(lagRequest(ENHETID, JOURNALPOST_ID, SAKSNUMMER)));
        assertThat(e.getFeil().getLøsningsforslag()).contains("2018");
    }

    @Test
    public void skalIkkeTillateJournalførinAvSøknadMedOmsorgFørGrense() throws Exception {
        when(journalpost.getHovedtype()).thenReturn(DokumentTypeId.SØKNAD_FORELDREPENGER_ADOPSJON);
        when(fagsakRestKlientMock.finnFagsakInfomasjon(ArgumentMatchers.<SaksnummerDto>any()))
                .thenReturn(Optional.of(new FagsakInfomasjonDto(AKTØR_ID, foreldrepenger.getOffisiellKode())));

        String xml = readFile("testdata/fp-adopsjon-far.xml");
        when(journalpost.getStrukturertPayload()).thenReturn(xml);
        when(journalpost.getInnholderStrukturertInformasjon()).thenReturn(true);
        var e = assertThrows(FunksjonellException.class,
                () -> behandleDokumentService.oppdaterOgFerdigstillJournalfoering(lagRequest(ENHETID, JOURNALPOST_ID, SAKSNUMMER)));
        assertThat(e.getFeil().getLøsningsforslag()).contains("2018");
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
        when(arkivTjeneste.oppdaterRettMangler(any(), any(), any(), any())).thenReturn(true);

        behandleDokumentService.oppdaterOgFerdigstillJournalfoering(request);

        verify(arkivTjeneste).oppdaterMedSak(JOURNALPOST_ID, SAKSNUMMER);
        verify(arkivTjeneste).ferdigstillJournalføring(JOURNALPOST_ID, ENHETID);
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
        when(arkivTjeneste.oppdaterRettMangler(any(), any(), any(), any())).thenReturn(true);

        behandleDokumentService.oppdaterOgFerdigstillJournalfoering(request);

        verify(arkivTjeneste).oppdaterMedSak(JOURNALPOST_ID, SAKSNUMMER);
        verify(arkivTjeneste).ferdigstillJournalføring(JOURNALPOST_ID, ENHETID);
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
        when(arkivTjeneste.oppdaterRettMangler(any(), any(), any(), any())).thenReturn(true);

        behandleDokumentService.oppdaterOgFerdigstillJournalfoering(request);

        verify(arkivTjeneste).oppdaterMedSak(JOURNALPOST_ID, SAKSNUMMER);
        verify(arkivTjeneste).ferdigstillJournalføring(JOURNALPOST_ID, ENHETID);
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
        when(arkivTjeneste.oppdaterRettMangler(any(), any(), any(), any())).thenReturn(true);

        behandleDokumentService.oppdaterOgFerdigstillJournalfoering(request);

        verify(arkivTjeneste).oppdaterMedSak(JOURNALPOST_ID, SAKSNUMMER);
        verify(arkivTjeneste).ferdigstillJournalføring(JOURNALPOST_ID, ENHETID);
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

    private static OppdaterOgFerdigstillJournalfoeringRequest lagRequest(String enhetid, String journalpostId,
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
