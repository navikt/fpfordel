package no.nav.foreldrepenger.fordel.web.app.rest.journalføring;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverdi.Journalstatus;
import no.nav.foreldrepenger.kontrakter.fordel.FagsakInfomasjonDto;
import no.nav.foreldrepenger.kontrakter.fordel.SaksnummerDto;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentRepository;
import no.nav.foreldrepenger.mottak.journal.ArkivJournalpost;
import no.nav.foreldrepenger.mottak.journal.ArkivTjeneste;
import no.nav.foreldrepenger.mottak.klient.Fagsak;
import no.nav.foreldrepenger.mottak.person.PersonInformasjon;
import no.nav.foreldrepenger.mottak.sak.SakClient;
import no.nav.foreldrepenger.mottak.tjeneste.VLKlargjører;
import no.nav.vedtak.exception.FunksjonellException;
import no.nav.vedtak.exception.TekniskException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;
import java.util.TimeZone;

import static no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema.*;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.*;
import static no.nav.foreldrepenger.fordel.kodeverdi.Journalposttype.INNGÅENDE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BehandleDokumentRestTjenesteTest {

    static {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Oslo"));
    }

    private BehandleDokumentRestTjeneste behandleDokument;

    private static final String JOURNALPOST_ID = "123";
    private static final String ENHETID = "4567";
    private static final String SAKSNUMMER = "789";
    private static final String AKTØR_ID = "9000000000009";

    @Mock
    private ArkivTjeneste arkiv;
    @Mock
    private VLKlargjører klargjør;
    @Mock
    private Fagsak fagsak;
    @Mock
    private PersonInformasjon aktør;
    @Mock
    private SakClient sak;
    @Mock
    private ArkivJournalpost journalpost;

    @BeforeEach
    public void setUp() {

        lenient().when(fagsak.finnFagsakInfomasjon(ArgumentMatchers.<SaksnummerDto>any()))
                .thenReturn(Optional.of(new FagsakInfomasjonDto(AKTØR_ID, ENGANGSSTØNAD.getOffisiellKode())));
        lenient().when(journalpost.getJournalposttype()).thenReturn(INNGÅENDE);
        lenient().when(arkiv.hentArkivJournalpost(JOURNALPOST_ID)).thenReturn(journalpost);

        behandleDokument = new BehandleDokumentRestTjeneste(klargjør,
                fagsak, sak, aktør, arkiv, mock(DokumentRepository.class));
    }

    @Test
    void skalValiderePåkrevdInput_enhetId() {
        var req = req(null, JOURNALPOST_ID, SAKSNUMMER);
        Exception ex = assertThrows(TekniskException.class,
                () -> behandleDokument.oppdaterOgFerdigstillJournalfoering(req));

        assertThat(ex.getMessage()).contains("Ugyldig input: EnhetId");
    }

    @Test
    void skalValiderePåkrevdInput_journalpostId() {
        var req = req(ENHETID, null, SAKSNUMMER);
        Exception ex = assertThrows(TekniskException.class,
                () -> behandleDokument.oppdaterOgFerdigstillJournalfoering(req));
        assertThat(ex.getMessage())
                .contains("Ugyldig input: ArkivId");
    }

    @Test
    void skalValiderePåkrevdInput_saksnummer() {
        var req = req(ENHETID, JOURNALPOST_ID, null);
        Exception ex = assertThrows(TekniskException.class,
                () -> behandleDokument.oppdaterOgFerdigstillJournalfoering(req));
        assertThat(ex.getMessage())
                .contains("Ugyldig input: Saksnummer");
    }

    @Test
    void skalValidereAtFagsakFinnes() {
        when(fagsak.finnFagsakInfomasjon(any()))
                .thenReturn(Optional.empty());

        var req = req(ENHETID, JOURNALPOST_ID, SAKSNUMMER);
        Exception ex = assertThrows(FunksjonellException.class,
                () -> behandleDokument.oppdaterOgFerdigstillJournalfoering(req));

        assertThat(ex.getMessage())
                .contains("Kan ikke journalføre på saksnummer");

    }

    @Test
    void skalIkkeJournalføreKlagerPåSakUtenBehandling() {

        when(fagsak.finnFagsakInfomasjon(ArgumentMatchers.<SaksnummerDto>any()))
                .thenReturn(Optional.of(new FagsakInfomasjonDto(AKTØR_ID, BehandlingTema.UDEFINERT.getOffisiellKode())));

        when(journalpost.getHovedtype()).thenReturn(KLAGE_DOKUMENT);
        var req = req(ENHETID, JOURNALPOST_ID, SAKSNUMMER);
        assertThrows(FunksjonellException.class,
                () -> behandleDokument.oppdaterOgFerdigstillJournalfoering(req));
    }

    @Test
    void skalKunneJournalføreKlagerPåSakMedBehandling() {

        lenient().when(journalpost.getHovedtype()).thenReturn(KLAGE_DOKUMENT);
        lenient().when(arkiv.oppdaterRettMangler(any(), any(), any(), any())).thenReturn(true);

        behandleDokument.oppdaterOgFerdigstillJournalfoering(req(ENHETID, JOURNALPOST_ID, SAKSNUMMER));
    }

    @Test
    void skalIkkeJournalførePapirsøknadSakAnnenYtelse() {

        when(fagsak.finnFagsakInfomasjon(ArgumentMatchers.<SaksnummerDto>any()))
                .thenReturn(Optional.of(new FagsakInfomasjonDto(AKTØR_ID, FORELDREPENGER_FØDSEL.getOffisiellKode())));

        when(journalpost.getHovedtype()).thenReturn(SØKNAD_SVANGERSKAPSPENGER);
        var req = req(ENHETID, JOURNALPOST_ID, SAKSNUMMER);
        assertThrows(FunksjonellException.class,
                () -> behandleDokument.oppdaterOgFerdigstillJournalfoering(req));
    }

    @Test
    void skalKjøreHeltIgjennomNaarJournaltilstandIkkeErEndelig() throws Exception {
        when(arkiv.oppdaterRettMangler(any(), any(), any(), any())).thenReturn(true);
        when(journalpost.getTilstand()).thenReturn(Journalstatus.MOTTATT);
        when(journalpost.getJournalpostId()).thenReturn(JOURNALPOST_ID);
        when(journalpost.getHovedtype()).thenReturn(SØKNAD_ENGANGSSTØNAD_FØDSEL);

        behandleDokument.oppdaterOgFerdigstillJournalfoering(req(ENHETID, JOURNALPOST_ID, SAKSNUMMER));

        verify(arkiv).oppdaterMedSak(JOURNALPOST_ID, SAKSNUMMER, AKTØR_ID);
        verify(arkiv).ferdigstillJournalføring(JOURNALPOST_ID, ENHETID);
        verify(klargjør).klargjør(any(), eq(SAKSNUMMER), eq(JOURNALPOST_ID), any(), any(),
                eq(ENGANGSSTØNAD_FØDSEL), any(), any(), any(), any());
    }

    @Test
    void skalTillateJournalførinAvInntektsmeldingForeldrepender() throws Exception {
        when(journalpost.getHovedtype()).thenReturn(DokumentTypeId.INNTEKTSMELDING);
        when(fagsak.finnFagsakInfomasjon(ArgumentMatchers.<SaksnummerDto>any()))
                .thenReturn(Optional.of(new FagsakInfomasjonDto(AKTØR_ID, FORELDREPENGER_FØDSEL.getOffisiellKode())));

        when(journalpost.getInnholderStrukturertInformasjon()).thenReturn(true);
        when(journalpost.getStrukturertPayload()).thenReturn(readFile("testdata/inntektsmelding-foreldrepenger.xml"));
        when(journalpost.getTilstand()).thenReturn(Journalstatus.MOTTATT);
        when(journalpost.getJournalpostId()).thenReturn(JOURNALPOST_ID);
        when(arkiv.oppdaterRettMangler(any(), any(), any(), any())).thenReturn(true);

        behandleDokument.oppdaterOgFerdigstillJournalfoering(req(ENHETID, JOURNALPOST_ID, SAKSNUMMER));

        verify(arkiv).oppdaterMedSak(JOURNALPOST_ID, SAKSNUMMER, AKTØR_ID);
        verify(arkiv).ferdigstillJournalføring(JOURNALPOST_ID, ENHETID);
        verify(klargjør).klargjør(eq(readFile("testdata/inntektsmelding-foreldrepenger.xml")), eq(SAKSNUMMER), eq(JOURNALPOST_ID), any(), any(),
                eq(FORELDREPENGER_FØDSEL), any(), any(), any(), any());
    }

    @Test
    void skalIkkeTillateJournalførinAvInntektsmeldingSvangerskapspenger() throws Exception {
        when(journalpost.getHovedtype()).thenReturn(INNTEKTSMELDING);
        when(fagsak.finnFagsakInfomasjon(ArgumentMatchers.<SaksnummerDto>any()))
                .thenReturn(Optional.of(new FagsakInfomasjonDto(AKTØR_ID, FORELDREPENGER_FØDSEL.getOffisiellKode())));

        when(journalpost.getInnholderStrukturertInformasjon()).thenReturn(true);
        when(journalpost.getStrukturertPayload()).thenReturn(readFile("testdata/inntektsmelding-svangerskapspenger.xml"));

        var req = req(ENHETID, JOURNALPOST_ID, SAKSNUMMER);
        assertThrows(FunksjonellException.class,
                () -> behandleDokument.oppdaterOgFerdigstillJournalfoering(req));
    }

    @Test
    void skalIkkeTillateJournalførinAvSøknadMedUttakFørGrense() throws Exception {
        when(journalpost.getHovedtype()).thenReturn(SØKNAD_FORELDREPENGER_FØDSEL);
        when(fagsak.finnFagsakInfomasjon(ArgumentMatchers.<SaksnummerDto>any()))
                .thenReturn(Optional.of(new FagsakInfomasjonDto(AKTØR_ID, FORELDREPENGER.getOffisiellKode())));

        when(journalpost.getStrukturertPayload()).thenReturn(readFile("testdata/selvb-soeknad-forp-uttak-før-konfigverdi.xml"));
        when(journalpost.getInnholderStrukturertInformasjon()).thenReturn(true);
        var req = req(ENHETID, JOURNALPOST_ID, SAKSNUMMER);
        FunksjonellException ex = assertThrows(FunksjonellException.class,
                () -> behandleDokument.oppdaterOgFerdigstillJournalfoering(req));
        assertThat(ex.getMessage())
                .contains("For tidlig");
    }

    @Test
    void skalIkkeTillateJournalførinAvSøknadMedOmsorgFørGrense() throws Exception {
        when(journalpost.getHovedtype()).thenReturn(SØKNAD_FORELDREPENGER_ADOPSJON);
        when(fagsak.finnFagsakInfomasjon(ArgumentMatchers.<SaksnummerDto>any()))
                .thenReturn(Optional.of(new FagsakInfomasjonDto(AKTØR_ID, FORELDREPENGER.getOffisiellKode())));

        when(journalpost.getStrukturertPayload()).thenReturn(readFile("testdata/fp-adopsjon-far.xml"));
        when(journalpost.getInnholderStrukturertInformasjon()).thenReturn(true);
        var req = req(ENHETID, JOURNALPOST_ID, SAKSNUMMER);
        var e = assertThrows(FunksjonellException.class,
                () -> behandleDokument.oppdaterOgFerdigstillJournalfoering(req));
        assertThat(e.getMessage()).contains("For tidlig");
    }

    @Test
    void skalTillateJournalførinAvSøknadMedUttakEtterGrense() throws Exception {
        when(journalpost.getHovedtype()).thenReturn(SØKNAD_FORELDREPENGER_FØDSEL);
        when(fagsak.finnFagsakInfomasjon(ArgumentMatchers.<SaksnummerDto>any()))
                .thenReturn(Optional.of(new FagsakInfomasjonDto(AKTØR_ID, FORELDREPENGER.getOffisiellKode())));

        when(journalpost.getStrukturertPayload()).thenReturn(readFile("testdata/selvb-soeknad-forp.xml"));
        when(journalpost.getInnholderStrukturertInformasjon()).thenReturn(true);
        when(journalpost.getTilstand()).thenReturn(Journalstatus.MOTTATT);
        when(journalpost.getJournalpostId()).thenReturn(JOURNALPOST_ID);
        when(arkiv.oppdaterRettMangler(any(), any(), any(), any())).thenReturn(true);

        behandleDokument.oppdaterOgFerdigstillJournalfoering(req(ENHETID, JOURNALPOST_ID, SAKSNUMMER));

        verify(arkiv).oppdaterMedSak(JOURNALPOST_ID, SAKSNUMMER, AKTØR_ID);
        verify(arkiv).ferdigstillJournalføring(JOURNALPOST_ID, ENHETID);
        verify(klargjør).klargjør(eq(readFile("testdata/selvb-soeknad-forp.xml")), eq(SAKSNUMMER), eq(JOURNALPOST_ID), any(), any(),
                eq(FORELDREPENGER_FØDSEL), any(), any(), any(), any());
    }

    @Test
    void skalIgnorereUkjentStrukturertData() throws Exception {
        when(journalpost.getHovedtype()).thenReturn(ETTERSENDT_KLAGE);
        when(fagsak.finnFagsakInfomasjon(ArgumentMatchers.<SaksnummerDto>any()))
                .thenReturn(Optional.of(new FagsakInfomasjonDto(AKTØR_ID, FORELDREPENGER.getOffisiellKode())));

        when(journalpost.getStrukturertPayload()).thenReturn(readFile("testdata/metadata.json"));
        when(journalpost.getInnholderStrukturertInformasjon()).thenReturn(true);
        when(journalpost.getTilstand()).thenReturn(Journalstatus.MOTTATT);
        when(journalpost.getJournalpostId()).thenReturn(JOURNALPOST_ID);
        when(arkiv.oppdaterRettMangler(any(), any(), any(), any())).thenReturn(true);

        behandleDokument.oppdaterOgFerdigstillJournalfoering(req(ENHETID, JOURNALPOST_ID, SAKSNUMMER));

        verify(arkiv).oppdaterMedSak(JOURNALPOST_ID, SAKSNUMMER, AKTØR_ID);
        verify(arkiv).ferdigstillJournalføring(JOURNALPOST_ID, ENHETID);
        verify(klargjør).klargjør(eq(null), eq(SAKSNUMMER), eq(JOURNALPOST_ID), any(), any(),
                eq(FORELDREPENGER), any(), any(), any(), any());
    }

    @Test
    void skalTillateJournalførinAvSøknadMedOmsorgEtterGrense() throws Exception {
        when(journalpost.getHovedtype()).thenReturn(SØKNAD_FORELDREPENGER_ADOPSJON);
        when(fagsak.finnFagsakInfomasjon(ArgumentMatchers.<SaksnummerDto>any()))
                .thenReturn(Optional.of(new FagsakInfomasjonDto(AKTØR_ID, FORELDREPENGER.getOffisiellKode())));

        when(journalpost.getStrukturertPayload()).thenReturn(readFile("testdata/fp-adopsjon-mor.xml"));
        when(journalpost.getInnholderStrukturertInformasjon()).thenReturn(true);
        when(journalpost.getTilstand()).thenReturn(Journalstatus.MOTTATT);
        when(journalpost.getJournalpostId()).thenReturn(JOURNALPOST_ID);
        when(arkiv.oppdaterRettMangler(any(), any(), any(), any())).thenReturn(true);

        behandleDokument.oppdaterOgFerdigstillJournalfoering(req(ENHETID, JOURNALPOST_ID, SAKSNUMMER));

        verify(arkiv).oppdaterMedSak(JOURNALPOST_ID, SAKSNUMMER, AKTØR_ID);
        verify(arkiv).ferdigstillJournalføring(JOURNALPOST_ID, ENHETID);
        verify(klargjør).klargjør(eq(readFile("testdata/fp-adopsjon-mor.xml")), eq(SAKSNUMMER), eq(JOURNALPOST_ID), any(), any(),
                eq(FORELDREPENGER_ADOPSJON), any(), any(), any(), any());
    }

    @Test
    void skalTillateJournalførinAvEndringsSøknadMedAnnetSaksnummer() throws Exception {
        when(journalpost.getHovedtype()).thenReturn(FORELDREPENGER_ENDRING_SØKNAD);
        when(fagsak.finnFagsakInfomasjon(ArgumentMatchers.<SaksnummerDto>any()))
                .thenReturn(Optional.of(new FagsakInfomasjonDto(AKTØR_ID, FORELDREPENGER.getOffisiellKode())));

        when(journalpost.getStrukturertPayload()).thenReturn(readFile("testdata/selvb-soeknad-endring.xml"));
        when(journalpost.getInnholderStrukturertInformasjon()).thenReturn(true);
        when(journalpost.getTilstand()).thenReturn(Journalstatus.MOTTATT);
        when(journalpost.getJournalpostId()).thenReturn(JOURNALPOST_ID);
        when(arkiv.oppdaterRettMangler(any(), any(), any(), any())).thenReturn(true);

        behandleDokument.oppdaterOgFerdigstillJournalfoering(req(ENHETID, JOURNALPOST_ID, SAKSNUMMER));

        verify(arkiv).oppdaterMedSak(JOURNALPOST_ID, SAKSNUMMER, AKTØR_ID);
        verify(arkiv).ferdigstillJournalføring(JOURNALPOST_ID, ENHETID);
        verify(klargjør).klargjør(eq(readFile("testdata/selvb-soeknad-endring.xml")), eq(SAKSNUMMER), eq(JOURNALPOST_ID), any(), any(),
                eq(FORELDREPENGER), any(), any(), any(), any());
    }

    @Test
    void skalKjøreHeltIgjennomNaarJournaltilstandErEndelig() throws Exception {
        when(journalpost.getTilstand()).thenReturn(Journalstatus.JOURNALFOERT);
        when(journalpost.getHovedtype()).thenReturn(SØKNAD_ENGANGSSTØNAD_FØDSEL);
        behandleDokument.oppdaterOgFerdigstillJournalfoering(req(ENHETID, JOURNALPOST_ID, SAKSNUMMER));

        verify(klargjør).klargjør(any(), eq(SAKSNUMMER), eq(JOURNALPOST_ID), any(), any(),
                eq(ENGANGSSTØNAD_FØDSEL), any(), any(), any(), any());
    }

    private static BehandleDokumentRestTjeneste.BehandleDokumentRequest req(String enhetid, String journalpostId, String sakId) {
        return new BehandleDokumentRestTjeneste.BehandleDokumentRequest(
                journalpostId,
                sakId,
                enhetid);
    }

    String readFile(String filename) throws URISyntaxException, IOException {
        Path path = Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource(filename)).toURI());
        return Files.readString(path);
    }
}