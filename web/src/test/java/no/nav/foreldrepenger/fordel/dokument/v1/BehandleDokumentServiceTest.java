package no.nav.foreldrepenger.fordel.dokument.v1;

import static no.nav.foreldrepenger.fordel.dokument.v1.BehandleDokumentService.ENHET_MANGLER;
import static no.nav.foreldrepenger.fordel.dokument.v1.BehandleDokumentService.JOURNALPOST_MANGLER;
import static no.nav.foreldrepenger.fordel.dokument.v1.BehandleDokumentService.SAKSNUMMER_UGYLDIG;
import static no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema.ENGANGSSTØNAD;
import static no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema.ENGANGSSTØNAD_FØDSEL;
import static no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema.FORELDREPENGER;
import static no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema.FORELDREPENGER_ADOPSJON;
import static no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema.FORELDREPENGER_FØDSEL;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.ETTERSENDT_KLAGE;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.FORELDREPENGER_ENDRING_SØKNAD;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.INNTEKTSMELDING;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.KLAGE_DOKUMENT;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.SØKNAD_FORELDREPENGER_ADOPSJON;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.SØKNAD_SVANGERSKAPSPENGER;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.UDEFINERT;
import static no.nav.foreldrepenger.fordel.kodeverdi.Journalposttype.INNGÅENDE;
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
import no.nav.tjeneste.virksomhet.behandledokumentforsendelse.v1.OppdaterOgFerdigstillJournalfoeringUgyldigInput;
import no.nav.tjeneste.virksomhet.behandledokumentforsendelse.v1.meldinger.OppdaterOgFerdigstillJournalfoeringRequest;
import no.nav.vedtak.exception.FunksjonellException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
class BehandleDokumentServiceTest {

    private static final String JOURNALPOST_ID = "123";
    private static final String ENHETID = "4567";
    private static final String SAKSNUMMER = "789";
    private static final String AKTØR_ID = "9000000000009";

    static {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Oslo"));
    }

    private BehandleDokumentService behandleDokument;
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

    private static OppdaterOgFerdigstillJournalfoeringRequest req(String enhetid, String journalpostId, String sakId) {
        var request = new OppdaterOgFerdigstillJournalfoeringRequest();
        request.setEnhetId(enhetid);
        request.setJournalpostId(journalpostId);
        request.setSakId(sakId);
        return request;
    }

    @BeforeEach
    public void setUp() {

        when(fagsak.finnFagsakInfomasjon(ArgumentMatchers.<SaksnummerDto>any())).thenReturn(
            Optional.of(new FagsakInfomasjonDto(AKTØR_ID, ENGANGSSTØNAD.getOffisiellKode())));
        when(journalpost.getJournalposttype()).thenReturn(INNGÅENDE);
        when(arkiv.hentArkivJournalpost(JOURNALPOST_ID)).thenReturn(journalpost);

        behandleDokument = new BehandleDokumentService(klargjør, fagsak, sak, aktør, arkiv, mock(DokumentRepository.class));
    }

    @Test
    void skalValiderePåkrevdInput_enhetId() {
        assertThat(assertThrows(OppdaterOgFerdigstillJournalfoeringUgyldigInput.class,
            () -> behandleDokument.oppdaterOgFerdigstillJournalfoering(req(null, JOURNALPOST_ID, SAKSNUMMER))).getMessage().contains(ENHET_MANGLER));
    }

    @Test
    void skalValiderePåkrevdInput_journalpostId() {
        assertThat(assertThrows(OppdaterOgFerdigstillJournalfoeringUgyldigInput.class,
            () -> behandleDokument.oppdaterOgFerdigstillJournalfoering(req(ENHETID, null, SAKSNUMMER))).getMessage().contains(JOURNALPOST_MANGLER));
    }

    @Test
    void skalValiderePåkrevdInput_saksnummer() {
        assertThat(assertThrows(OppdaterOgFerdigstillJournalfoeringUgyldigInput.class,
            () -> behandleDokument.oppdaterOgFerdigstillJournalfoering(req(ENHETID, JOURNALPOST_ID, null))).getMessage()
            .contains(SAKSNUMMER_UGYLDIG));
    }

    @Test
    void skalValidereAtFagsakFinnes() {
        when(fagsak.finnFagsakInfomasjon(any())).thenReturn(Optional.empty());
        assertThat(assertThrows(FunksjonellException.class,
            () -> behandleDokument.oppdaterOgFerdigstillJournalfoering(req(ENHETID, JOURNALPOST_ID, SAKSNUMMER))).getMessage()
            .contains("Kan ikke journalføre på saksnummer"));

    }

    @Test
    void skalIkkeJournalføreKlagerPåSakUtenBehandling() {

        when(fagsak.finnFagsakInfomasjon(ArgumentMatchers.<SaksnummerDto>any())).thenReturn(
            Optional.of(new FagsakInfomasjonDto(AKTØR_ID, UDEFINERT.getOffisiellKode())));

        when(journalpost.getHovedtype()).thenReturn(KLAGE_DOKUMENT);
        assertThrows(FunksjonellException.class,
            () -> behandleDokument.oppdaterOgFerdigstillJournalfoering(req(ENHETID, JOURNALPOST_ID, SAKSNUMMER)));
    }

    @Test
    void skalKunneJournalføreKlagerPåSakMedBehandling() throws Exception {

        when(journalpost.getHovedtype()).thenReturn(KLAGE_DOKUMENT);
        when(arkiv.oppdaterRettMangler(any(), any(), any(), any())).thenReturn(true);

        behandleDokument.oppdaterOgFerdigstillJournalfoering(req(ENHETID, JOURNALPOST_ID, SAKSNUMMER));
    }

    @Test
    void skalIkkeJournalførePapirsøknadSakAnnenYtelse() {

        when(fagsak.finnFagsakInfomasjon(ArgumentMatchers.<SaksnummerDto>any())).thenReturn(
            Optional.of(new FagsakInfomasjonDto(AKTØR_ID, FORELDREPENGER_FØDSEL.getOffisiellKode())));

        when(journalpost.getHovedtype()).thenReturn(SØKNAD_SVANGERSKAPSPENGER);
        assertThrows(FunksjonellException.class,
            () -> behandleDokument.oppdaterOgFerdigstillJournalfoering(req(ENHETID, JOURNALPOST_ID, SAKSNUMMER)));
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
        verify(klargjør).klargjør(any(), eq(SAKSNUMMER), eq(JOURNALPOST_ID), any(), any(), eq(ENGANGSSTØNAD_FØDSEL), any(), any(), any(), any());
    }

    @Test
    void skalTillateJournalførinAvInntektsmeldingForeldrepender() throws Exception {
        when(journalpost.getHovedtype()).thenReturn(DokumentTypeId.INNTEKTSMELDING);
        when(fagsak.finnFagsakInfomasjon(ArgumentMatchers.<SaksnummerDto>any())).thenReturn(
            Optional.of(new FagsakInfomasjonDto(AKTØR_ID, FORELDREPENGER_FØDSEL.getOffisiellKode())));

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
        when(fagsak.finnFagsakInfomasjon(ArgumentMatchers.<SaksnummerDto>any())).thenReturn(
            Optional.of(new FagsakInfomasjonDto(AKTØR_ID, FORELDREPENGER_FØDSEL.getOffisiellKode())));

        when(journalpost.getInnholderStrukturertInformasjon()).thenReturn(true);
        when(journalpost.getStrukturertPayload()).thenReturn(readFile("testdata/inntektsmelding-svangerskapspenger.xml"));

        assertThrows(FunksjonellException.class,
            () -> behandleDokument.oppdaterOgFerdigstillJournalfoering(req(ENHETID, JOURNALPOST_ID, SAKSNUMMER)));
    }

    @Test
    void skalIkkeTillateJournalførinAvSøknadMedUttakFørGrense() throws Exception {
        when(journalpost.getHovedtype()).thenReturn(SØKNAD_FORELDREPENGER_FØDSEL);
        when(fagsak.finnFagsakInfomasjon(ArgumentMatchers.<SaksnummerDto>any())).thenReturn(
            Optional.of(new FagsakInfomasjonDto(AKTØR_ID, FORELDREPENGER.getOffisiellKode())));

        when(journalpost.getStrukturertPayload()).thenReturn(readFile("testdata/selvb-soeknad-forp-uttak-før-konfigverdi.xml"));
        when(journalpost.getInnholderStrukturertInformasjon()).thenReturn(true);
        assertThat(assertThrows(FunksjonellException.class,
            () -> behandleDokument.oppdaterOgFerdigstillJournalfoering(req(ENHETID, JOURNALPOST_ID, SAKSNUMMER))).getMessage()).contains(
            "For tidlig");
    }

    @Test
    void skalIkkeTillateJournalførinAvSøknadMedOmsorgFørGrense() throws Exception {
        when(journalpost.getHovedtype()).thenReturn(SØKNAD_FORELDREPENGER_ADOPSJON);
        when(fagsak.finnFagsakInfomasjon(ArgumentMatchers.<SaksnummerDto>any())).thenReturn(
            Optional.of(new FagsakInfomasjonDto(AKTØR_ID, FORELDREPENGER.getOffisiellKode())));

        when(journalpost.getStrukturertPayload()).thenReturn(readFile("testdata/fp-adopsjon-far.xml"));
        when(journalpost.getInnholderStrukturertInformasjon()).thenReturn(true);
        var e = assertThrows(FunksjonellException.class,
            () -> behandleDokument.oppdaterOgFerdigstillJournalfoering(req(ENHETID, JOURNALPOST_ID, SAKSNUMMER)));
        assertThat(e.getMessage()).contains("For tidlig");
    }

    @Test
    void skalTillateJournalførinAvSøknadMedUttakEtterGrense() throws Exception {
        when(journalpost.getHovedtype()).thenReturn(SØKNAD_FORELDREPENGER_FØDSEL);
        when(fagsak.finnFagsakInfomasjon(ArgumentMatchers.<SaksnummerDto>any())).thenReturn(
            Optional.of(new FagsakInfomasjonDto(AKTØR_ID, FORELDREPENGER.getOffisiellKode())));

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
        when(fagsak.finnFagsakInfomasjon(ArgumentMatchers.<SaksnummerDto>any())).thenReturn(
            Optional.of(new FagsakInfomasjonDto(AKTØR_ID, FORELDREPENGER.getOffisiellKode())));

        when(journalpost.getStrukturertPayload()).thenReturn(readFile("testdata/metadata.json"));
        when(journalpost.getInnholderStrukturertInformasjon()).thenReturn(true);
        when(journalpost.getTilstand()).thenReturn(Journalstatus.MOTTATT);
        when(journalpost.getJournalpostId()).thenReturn(JOURNALPOST_ID);
        when(arkiv.oppdaterRettMangler(any(), any(), any(), any())).thenReturn(true);

        behandleDokument.oppdaterOgFerdigstillJournalfoering(req(ENHETID, JOURNALPOST_ID, SAKSNUMMER));

        verify(arkiv).oppdaterMedSak(JOURNALPOST_ID, SAKSNUMMER, AKTØR_ID);
        verify(arkiv).ferdigstillJournalføring(JOURNALPOST_ID, ENHETID);
        verify(klargjør).klargjør(eq(null), eq(SAKSNUMMER), eq(JOURNALPOST_ID), any(), any(), eq(FORELDREPENGER), any(), any(), any(), any());
    }

    @Test
    void skalTillateJournalførinAvSøknadMedOmsorgEtterGrense() throws Exception {
        when(journalpost.getHovedtype()).thenReturn(SØKNAD_FORELDREPENGER_ADOPSJON);
        when(fagsak.finnFagsakInfomasjon(ArgumentMatchers.<SaksnummerDto>any())).thenReturn(
            Optional.of(new FagsakInfomasjonDto(AKTØR_ID, FORELDREPENGER.getOffisiellKode())));

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
        when(fagsak.finnFagsakInfomasjon(ArgumentMatchers.<SaksnummerDto>any())).thenReturn(
            Optional.of(new FagsakInfomasjonDto(AKTØR_ID, FORELDREPENGER.getOffisiellKode())));

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

        verify(klargjør).klargjør(any(), eq(SAKSNUMMER), eq(JOURNALPOST_ID), any(), any(), eq(ENGANGSSTØNAD_FØDSEL), any(), any(), any(), any());
    }

    String readFile(String filename) throws URISyntaxException, IOException {
        Path path = Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource(filename)).toURI());
        return Files.readString(path);
    }
}
