package no.nav.foreldrepenger.fordel.dokument.v1;

import static no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema.ENGANGSSTØNAD;
import static no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema.ENGANGSSTØNAD_FØDSEL;
import static no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema.FORELDREPENGER;
import static no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema.FORELDREPENGER_ADOPSJON;
import static no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema.FORELDREPENGER_FØDSEL;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

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
import no.nav.vedtak.felles.integrasjon.sak.v1.SakClient;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
class BehandleDokumentServiceTest {

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
    private ArkivTjeneste arkiv;
    @Mock
    private KlargjørForVLTjeneste klargjør;
    @Mock
    private FagsakTjeneste fagsak;
    @Mock
    private PersonInformasjon aktør;
    @Mock
    private SakClient sak;
    @Mock
    private ArkivJournalpost journalpost;

    @BeforeEach
    public void setUp() {

        when(fagsak.finnFagsakInfomasjon(ArgumentMatchers.<SaksnummerDto>any()))
                .thenReturn(Optional.of(new FagsakInfomasjonDto(AKTØR_ID, ENGANGSSTØNAD.getOffisiellKode())));

        when(journalpost.getTilstand()).thenReturn(Journalstatus.MOTTATT);
        when(journalpost.getJournalposttype()).thenReturn(Journalposttype.INNGÅENDE);
        when(journalpost.getHovedtype()).thenReturn(SØKNAD_ENGANGSSTØNAD_FØDSEL);

        when(arkiv.hentArkivJournalpost(JOURNALPOST_ID)).thenReturn(journalpost);

        when(aktør.hentAktørIdForPersonIdent(any())).thenReturn(Optional.empty());
        when(aktør.hentAktørIdForPersonIdent(BRUKER_FNR)).thenReturn(Optional.of(AKTØR_ID));

        behandleDokumentService = new BehandleDokumentService(klargjør,
                fagsak, sak, aktør, arkiv, mock(DokumentRepository.class));
    }

    @Test
    void skalValiderePåkrevdInput_enhetId() {
        var e = assertThrows(OppdaterOgFerdigstillJournalfoeringUgyldigInput.class,
                () -> behandleDokumentService.oppdaterOgFerdigstillJournalfoering(lagRequest(null, JOURNALPOST_ID, SAKSNUMMER)));
        assertThat(e.getMessage().contains(BehandleDokumentService.ENHET_MANGLER));
    }

    @Test
    void skalValiderePåkrevdInput_journalpostId() {
        var e = assertThrows(OppdaterOgFerdigstillJournalfoeringUgyldigInput.class,
                () -> behandleDokumentService.oppdaterOgFerdigstillJournalfoering(lagRequest(ENHETID, null, SAKSNUMMER)));
        assertThat(e.getMessage().contains(BehandleDokumentService.JOURNALPOST_MANGLER));
    }

    @Test
    void skalValiderePåkrevdInput_saksnummer() {
        var e = assertThrows(OppdaterOgFerdigstillJournalfoeringUgyldigInput.class,
                () -> behandleDokumentService.oppdaterOgFerdigstillJournalfoering(lagRequest(ENHETID, JOURNALPOST_ID, null)));
        assertThat(e.getMessage().contains(BehandleDokumentService.SAKSNUMMER_UGYLDIG));
    }

    @Test
    void skalValidereAtFagsakFinnes() {
        when(fagsak.finnFagsakInfomasjon(any()))
                .thenReturn(Optional.empty());
        var e = assertThrows(FunksjonellException.class,
                () -> behandleDokumentService.oppdaterOgFerdigstillJournalfoering(lagRequest(ENHETID, JOURNALPOST_ID, SAKSNUMMER)));
        assertThat(e.getMessage().contains("Kan ikke journalføre på saksnummer"));

    }

    @Test
    void skalIkkeJournalføreKlagerPåSakUtenBehandling() {

        when(fagsak.finnFagsakInfomasjon(ArgumentMatchers.<SaksnummerDto>any()))
                .thenReturn(Optional
                        .of(new FagsakInfomasjonDto(AKTØR_ID, BehandlingTema.UDEFINERT.getOffisiellKode())));

        when(journalpost.getHovedtype()).thenReturn(DokumentTypeId.KLAGE_DOKUMENT);
        assertThrows(FunksjonellException.class,
                () -> behandleDokumentService.oppdaterOgFerdigstillJournalfoering(lagRequest(ENHETID, JOURNALPOST_ID, SAKSNUMMER)));
    }

    @Test
    void skalKunneJournalføreKlagerPåSakMedBehandling() throws Exception {

        var request = lagRequest(ENHETID, JOURNALPOST_ID, SAKSNUMMER);

        when(journalpost.getHovedtype()).thenReturn(DokumentTypeId.KLAGE_DOKUMENT);
        when(arkiv.oppdaterRettMangler(any(), any(), any(), any())).thenReturn(true);

        behandleDokumentService.oppdaterOgFerdigstillJournalfoering(request);
    }

    @Test
    void skalIkkeJournalførePapirsøknadSakAnnenYtelse() {

        when(fagsak.finnFagsakInfomasjon(ArgumentMatchers.<SaksnummerDto>any()))
                .thenReturn(Optional
                        .of(new FagsakInfomasjonDto(AKTØR_ID, BehandlingTema.FORELDREPENGER_FØDSEL.getOffisiellKode())));

        when(journalpost.getHovedtype()).thenReturn(DokumentTypeId.SØKNAD_SVANGERSKAPSPENGER);
        assertThrows(FunksjonellException.class,
                () -> behandleDokumentService.oppdaterOgFerdigstillJournalfoering(lagRequest(ENHETID, JOURNALPOST_ID, SAKSNUMMER)));
    }

    @Test
    void skalKjøreHeltIgjennomNaarJournaltilstandIkkeErEndelig() throws Exception {
        var request = lagRequest(ENHETID, JOURNALPOST_ID, SAKSNUMMER);
        when(arkiv.oppdaterRettMangler(any(), any(), any(), any())).thenReturn(true);
        when(journalpost.getTilstand()).thenReturn(Journalstatus.MOTTATT);
        when(journalpost.getJournalpostId()).thenReturn(JOURNALPOST_ID);

        behandleDokumentService.oppdaterOgFerdigstillJournalfoering(request);

        verify(arkiv).oppdaterMedSak(JOURNALPOST_ID, SAKSNUMMER, AKTØR_ID);
        verify(arkiv).ferdigstillJournalføring(JOURNALPOST_ID, ENHETID);
        verify(klargjør).klargjørForVL(any(), eq(SAKSNUMMER), eq(JOURNALPOST_ID), any(), any(),
                eq(ENGANGSSTØNAD_FØDSEL), any(), any(), any(), any());
    }

    public void skalGiUnntakNårDetFinnesManglerSomIkkeKanRettes() {
        assertThrows(OppdaterOgFerdigstillJournalfoeringUgyldigInput.class,
                () -> behandleDokumentService.oppdaterOgFerdigstillJournalfoering(lagRequest(ENHETID, JOURNALPOST_ID, SAKSNUMMER)));
    }

    @Test
    void skalTillateJournalførinAvInntektsmeldingForeldrepender() throws Exception {
        var dokumentTypeId = DokumentTypeId.INNTEKTSMELDING;
        when(journalpost.getHovedtype()).thenReturn(dokumentTypeId);
        when(fagsak.finnFagsakInfomasjon(ArgumentMatchers.<SaksnummerDto>any()))
                .thenReturn(
                        Optional.of(new FagsakInfomasjonDto(AKTØR_ID, FORELDREPENGER_FØDSEL.getOffisiellKode())));

        String xml = readFile("testdata/inntektsmelding-foreldrepenger.xml");

        when(journalpost.getInnholderStrukturertInformasjon()).thenReturn(true);
        when(journalpost.getStrukturertPayload()).thenReturn(xml);
        when(journalpost.getTilstand()).thenReturn(Journalstatus.MOTTATT);
        when(journalpost.getJournalpostId()).thenReturn(JOURNALPOST_ID);
        when(arkiv.oppdaterRettMangler(any(), any(), any(), any())).thenReturn(true);

        behandleDokumentService.oppdaterOgFerdigstillJournalfoering(lagRequest(ENHETID, JOURNALPOST_ID, SAKSNUMMER));

        verify(arkiv).oppdaterMedSak(JOURNALPOST_ID, SAKSNUMMER, AKTØR_ID);
        verify(arkiv).ferdigstillJournalføring(JOURNALPOST_ID, ENHETID);
        verify(klargjør).klargjørForVL(eq(xml), eq(SAKSNUMMER), eq(JOURNALPOST_ID), any(), any(),
                eq(FORELDREPENGER_FØDSEL), any(), any(), any(), any());
    }

    @Test
    void skalIkkeTillateJournalførinAvInntektsmeldingSvangerskapspenger() throws Exception {
        when(journalpost.getHovedtype()).thenReturn(DokumentTypeId.INNTEKTSMELDING);
        when(fagsak.finnFagsakInfomasjon(ArgumentMatchers.<SaksnummerDto>any()))
                .thenReturn(Optional.of(new FagsakInfomasjonDto(AKTØR_ID, FORELDREPENGER_FØDSEL.getOffisiellKode())));

        String xml = readFile("testdata/inntektsmelding-svangerskapspenger.xml");
        when(journalpost.getInnholderStrukturertInformasjon()).thenReturn(true);
        when(journalpost.getStrukturertPayload()).thenReturn(xml);

        assertThrows(FunksjonellException.class,
                () -> behandleDokumentService.oppdaterOgFerdigstillJournalfoering(lagRequest(ENHETID, JOURNALPOST_ID, SAKSNUMMER)));
    }

    @Test
    void skalIkkeTillateJournalførinAvSøknadMedUttakFørGrense() throws Exception {
        when(journalpost.getHovedtype()).thenReturn(DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL);
        when(fagsak.finnFagsakInfomasjon(ArgumentMatchers.<SaksnummerDto>any()))
                .thenReturn(Optional.of(new FagsakInfomasjonDto(AKTØR_ID, FORELDREPENGER.getOffisiellKode())));

        String xml = readFile("testdata/selvb-soeknad-forp-uttak-før-konfigverdi.xml");
        when(journalpost.getStrukturertPayload()).thenReturn(xml);
        when(journalpost.getInnholderStrukturertInformasjon()).thenReturn(true);
        var e = assertThrows(FunksjonellException.class,
                () -> behandleDokumentService.oppdaterOgFerdigstillJournalfoering(lagRequest(ENHETID, JOURNALPOST_ID, SAKSNUMMER)));
        assertThat(e.getMessage()).contains("For tidlig");
    }

    @Test
    void skalIkkeTillateJournalførinAvSøknadMedOmsorgFørGrense() throws Exception {
        when(journalpost.getHovedtype()).thenReturn(DokumentTypeId.SØKNAD_FORELDREPENGER_ADOPSJON);
        when(fagsak.finnFagsakInfomasjon(ArgumentMatchers.<SaksnummerDto>any()))
                .thenReturn(Optional.of(new FagsakInfomasjonDto(AKTØR_ID, FORELDREPENGER.getOffisiellKode())));

        String xml = readFile("testdata/fp-adopsjon-far.xml");
        when(journalpost.getStrukturertPayload()).thenReturn(xml);
        when(journalpost.getInnholderStrukturertInformasjon()).thenReturn(true);
        var e = assertThrows(FunksjonellException.class,
                () -> behandleDokumentService.oppdaterOgFerdigstillJournalfoering(lagRequest(ENHETID, JOURNALPOST_ID, SAKSNUMMER)));
        assertThat(e.getMessage()).contains("For tidlig");
    }

    @Test
    void skalTillateJournalførinAvSøknadMedUttakEtterGrense() throws Exception {
        var request = lagRequest(ENHETID, JOURNALPOST_ID, SAKSNUMMER);
        var dokumentTypeId = DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL;
        when(journalpost.getHovedtype()).thenReturn(dokumentTypeId);
        when(fagsak.finnFagsakInfomasjon(ArgumentMatchers.<SaksnummerDto>any()))
                .thenReturn(Optional.of(new FagsakInfomasjonDto(AKTØR_ID, FORELDREPENGER.getOffisiellKode())));

        String xml = readFile("testdata/selvb-soeknad-forp.xml");
        when(journalpost.getStrukturertPayload()).thenReturn(xml);
        when(journalpost.getInnholderStrukturertInformasjon()).thenReturn(true);
        when(journalpost.getTilstand()).thenReturn(Journalstatus.MOTTATT);
        when(journalpost.getJournalpostId()).thenReturn(JOURNALPOST_ID);
        when(arkiv.oppdaterRettMangler(any(), any(), any(), any())).thenReturn(true);

        behandleDokumentService.oppdaterOgFerdigstillJournalfoering(request);

        verify(arkiv).oppdaterMedSak(JOURNALPOST_ID, SAKSNUMMER, AKTØR_ID);
        verify(arkiv).ferdigstillJournalføring(JOURNALPOST_ID, ENHETID);
        verify(klargjør).klargjørForVL(eq(xml), eq(SAKSNUMMER), eq(JOURNALPOST_ID), any(), any(),
                eq(FORELDREPENGER_FØDSEL), any(), any(), any(), any());
    }

    @Test
    void skalIgnorereUkjentStrukturertData() throws Exception {
        var request = lagRequest(ENHETID, JOURNALPOST_ID, SAKSNUMMER);
        var dokumentTypeId = DokumentTypeId.SØKNAD_KONTANTSTØTTE;
        when(journalpost.getHovedtype()).thenReturn(dokumentTypeId);
        when(fagsak.finnFagsakInfomasjon(ArgumentMatchers.<SaksnummerDto>any()))
                .thenReturn(Optional.of(new FagsakInfomasjonDto(AKTØR_ID, FORELDREPENGER.getOffisiellKode())));

        String xml = readFile("testdata/metadata.json");
        when(journalpost.getStrukturertPayload()).thenReturn(xml);
        when(journalpost.getInnholderStrukturertInformasjon()).thenReturn(true);
        when(journalpost.getTilstand()).thenReturn(Journalstatus.MOTTATT);
        when(journalpost.getJournalpostId()).thenReturn(JOURNALPOST_ID);
        when(arkiv.oppdaterRettMangler(any(), any(), any(), any())).thenReturn(true);

        behandleDokumentService.oppdaterOgFerdigstillJournalfoering(request);

        verify(arkiv).oppdaterMedSak(JOURNALPOST_ID, SAKSNUMMER, AKTØR_ID);
        verify(arkiv).ferdigstillJournalføring(JOURNALPOST_ID, ENHETID);
        verify(klargjør).klargjørForVL(eq(null), eq(SAKSNUMMER), eq(JOURNALPOST_ID), any(), any(),
                eq(FORELDREPENGER), any(), any(), any(), any());
    }

    @Test
    void skalTillateJournalførinAvSøknadMedOmsorgEtterGrense() throws Exception {
        var request = lagRequest(ENHETID, JOURNALPOST_ID, SAKSNUMMER);
        var dokumentTypeId = DokumentTypeId.SØKNAD_FORELDREPENGER_ADOPSJON;
        when(journalpost.getHovedtype()).thenReturn(dokumentTypeId);
        when(fagsak.finnFagsakInfomasjon(ArgumentMatchers.<SaksnummerDto>any()))
                .thenReturn(Optional.of(new FagsakInfomasjonDto(AKTØR_ID, FORELDREPENGER.getOffisiellKode())));

        String xml = readFile("testdata/fp-adopsjon-mor.xml");
        when(journalpost.getStrukturertPayload()).thenReturn(xml);
        when(journalpost.getInnholderStrukturertInformasjon()).thenReturn(true);
        when(journalpost.getTilstand()).thenReturn(Journalstatus.MOTTATT);
        when(journalpost.getJournalpostId()).thenReturn(JOURNALPOST_ID);
        when(arkiv.oppdaterRettMangler(any(), any(), any(), any())).thenReturn(true);

        behandleDokumentService.oppdaterOgFerdigstillJournalfoering(request);

        verify(arkiv).oppdaterMedSak(JOURNALPOST_ID, SAKSNUMMER, AKTØR_ID);
        verify(arkiv).ferdigstillJournalføring(JOURNALPOST_ID, ENHETID);
        verify(klargjør).klargjørForVL(eq(xml), eq(SAKSNUMMER), eq(JOURNALPOST_ID), any(), any(),
                eq(FORELDREPENGER_ADOPSJON), any(), any(), any(), any());
    }

    @Test
    void skalTillateJournalførinAvEndringsSøknadMedAnnetSaksnummer() throws Exception {
        var request = lagRequest(ENHETID, JOURNALPOST_ID, SAKSNUMMER);
        var dokumentTypeId = DokumentTypeId.FORELDREPENGER_ENDRING_SØKNAD;
        when(journalpost.getHovedtype()).thenReturn(dokumentTypeId);
        when(fagsak.finnFagsakInfomasjon(ArgumentMatchers.<SaksnummerDto>any()))
                .thenReturn(Optional.of(new FagsakInfomasjonDto(AKTØR_ID, FORELDREPENGER.getOffisiellKode())));

        String xml = readFile("testdata/selvb-soeknad-endring.xml");
        when(journalpost.getStrukturertPayload()).thenReturn(xml);
        when(journalpost.getInnholderStrukturertInformasjon()).thenReturn(true);
        when(journalpost.getTilstand()).thenReturn(Journalstatus.MOTTATT);
        when(journalpost.getJournalpostId()).thenReturn(JOURNALPOST_ID);
        when(arkiv.oppdaterRettMangler(any(), any(), any(), any())).thenReturn(true);

        behandleDokumentService.oppdaterOgFerdigstillJournalfoering(request);

        verify(arkiv).oppdaterMedSak(JOURNALPOST_ID, SAKSNUMMER, AKTØR_ID);
        verify(arkiv).ferdigstillJournalføring(JOURNALPOST_ID, ENHETID);
        verify(klargjør).klargjørForVL(eq(xml), eq(SAKSNUMMER), eq(JOURNALPOST_ID), any(), any(),
                eq(FORELDREPENGER), any(), any(), any(), any());
    }

    @Test
    void skalKjøreHeltIgjennomNaarJournaltilstandErEndelig() throws Exception {
        when(journalpost.getTilstand()).thenReturn(Journalstatus.JOURNALFOERT);

        var request = lagRequest(ENHETID, JOURNALPOST_ID, SAKSNUMMER);
        behandleDokumentService.oppdaterOgFerdigstillJournalfoering(request);

        verify(klargjør).klargjørForVL(any(), eq(SAKSNUMMER), eq(JOURNALPOST_ID), any(), any(),
                eq(ENGANGSSTØNAD_FØDSEL), any(), any(), any(), any());
    }

    private static OppdaterOgFerdigstillJournalfoeringRequest lagRequest(String enhetid, String journalpostId,
            String sakId) {
        var request = new OppdaterOgFerdigstillJournalfoeringRequest();
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
