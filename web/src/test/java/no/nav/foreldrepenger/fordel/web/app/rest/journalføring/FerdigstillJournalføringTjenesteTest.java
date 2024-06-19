package no.nav.foreldrepenger.fordel.web.app.rest.journalføring;


import static no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema.ENGANGSSTØNAD;
import static no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema.ENGANGSSTØNAD_FØDSEL;
import static no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema.FORELDREPENGER;
import static no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema.FORELDREPENGER_ADOPSJON;
import static no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema.FORELDREPENGER_FØDSEL;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.DOK_INNLEGGELSE;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.ETTERSENDT_KLAGE;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.FORELDREPENGER_ENDRING_SØKNAD;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.INNTEKTSMELDING;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.KLAGE_DOKUMENT;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.SØKNAD_FORELDREPENGER_ADOPSJON;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.SØKNAD_SVANGERSKAPSPENGER;
import static no.nav.foreldrepenger.fordel.kodeverdi.Journalposttype.INNGÅENDE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverdi.Journalstatus;
import no.nav.foreldrepenger.fordel.kodeverdi.MottakKanal;
import no.nav.foreldrepenger.journalføring.domene.JournalpostId;
import no.nav.foreldrepenger.journalføring.oppgave.Journalføringsoppgave;
import no.nav.foreldrepenger.kontrakter.fordel.FagsakInfomasjonDto;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentRepository;
import no.nav.foreldrepenger.mottak.domene.oppgavebehandling.FerdigstillOppgaveTask;
import no.nav.foreldrepenger.mottak.journal.ArkivJournalpost;
import no.nav.foreldrepenger.mottak.journal.ArkivTjeneste;
import no.nav.foreldrepenger.mottak.journal.saf.DokumentInfo;
import no.nav.foreldrepenger.mottak.journal.saf.Journalpost;
import no.nav.foreldrepenger.mottak.klient.Fagsak;
import no.nav.foreldrepenger.mottak.person.PersonInformasjon;
import no.nav.foreldrepenger.mottak.tjeneste.VLKlargjører;
import no.nav.vedtak.exception.FunksjonellException;
import no.nav.vedtak.felles.integrasjon.dokarkiv.dto.OppdaterJournalpostRequest;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;
import no.nav.vedtak.felles.prosesstask.api.TaskType;

@ExtendWith(MockitoExtension.class)
class FerdigstillJournalføringTjenesteTest {
    private static final JournalpostId journalpostId = JournalpostId.fra("123");
    private static final String JOURNALPOST_ID = "123";
    private static final String ENHETID = "4567";
    private static final String SAKSNUMMER = "789";
    private static final String OPPGAVE_ID = "123456L";
    private static final String AKTØR_ID = "9000000000009";
    @Mock
    private VLKlargjører klargjører;
    @Mock
    private Fagsak fagsak;
    @Mock
    private PersonInformasjon pdl;
    @Mock
    private Journalføringsoppgave oppgaver;
    @Mock
    private ProsessTaskTjeneste taskTjeneste;
    @Mock
    private ArkivTjeneste arkiv;
    @Mock
    private ArkivJournalpost arkivJournalpost;

    private FerdigstillJournalføringTjeneste journalføringTjeneste;
    private final List<FerdigstillJournalføringTjeneste.DokumenterMedNyTittel> tomDokumentListe = new ArrayList<>();


    @BeforeEach
    public void setUp() {

        lenient().when(fagsak.finnFagsakInfomasjon(ArgumentMatchers.any()))
            .thenReturn(Optional.of(new FagsakInfomasjonDto(AKTØR_ID, ENGANGSSTØNAD.getOffisiellKode())));
        lenient().when(arkivJournalpost.getJournalposttype()).thenReturn(INNGÅENDE);
        lenient().when(arkiv.hentArkivJournalpost(JOURNALPOST_ID)).thenReturn(arkivJournalpost);
        lenient().when(arkivJournalpost.getJournalpostId()).thenReturn(JOURNALPOST_ID);

        journalføringTjeneste = new FerdigstillJournalføringTjeneste(klargjører,
            fagsak,
            pdl,
            oppgaver,
            taskTjeneste,
            arkiv,
            mock(DokumentRepository.class));
    }

    @Test
    void skalKunneJournalføreKlagerPåSakMedBehandling() {

        lenient().when(arkivJournalpost.getHovedtype()).thenReturn(KLAGE_DOKUMENT);
        lenient().when(arkiv.oppdaterRettMangler(any(), any(), any(), any())).thenReturn(true);
        lenient().doThrow(new IllegalArgumentException("FEIL")).when(oppgaver).ferdigstillAlleÅpneJournalføringsoppgaverFor(any(JournalpostId.class));

        journalføringTjeneste.oppdaterJournalpostOgFerdigstill(ENHETID, SAKSNUMMER, arkivJournalpost, null, Collections.emptyList(), null);
        verify(oppgaver).ferdigstillAlleÅpneJournalføringsoppgaverFor(journalpostId);
        var taskCaptor = ArgumentCaptor.forClass(ProsessTaskData.class);
        verify(taskTjeneste).lagre(taskCaptor.capture());
        var taskdata = taskCaptor.getValue();
        assertThat(taskdata.getTaskType()).isEqualTo(TaskType.forProsessTask(FerdigstillOppgaveTask.class).value());
        assertThat(taskdata.getPropertyValue(FerdigstillOppgaveTask.JOURNALPOSTID_KEY)).isEqualTo(journalpostId.getVerdi());
    }

    @Test
    void skalIkkeJournalføreKlagerPåSakUtenBehandling() {

        when(fagsak.finnFagsakInfomasjon(ArgumentMatchers.any())).thenReturn(
            Optional.of(new FagsakInfomasjonDto(AKTØR_ID, BehandlingTema.UDEFINERT.getOffisiellKode())));
        when(arkivJournalpost.getHovedtype()).thenReturn(KLAGE_DOKUMENT);

        assertThrows(FunksjonellException.class,
            () -> journalføringTjeneste.oppdaterJournalpostOgFerdigstill(ENHETID, SAKSNUMMER, arkivJournalpost, null, tomDokumentListe, null));
    }

    @Test
    void skalIkkeJournalførePapirsøknadSakAnnenYtelse() {

        when(fagsak.finnFagsakInfomasjon(ArgumentMatchers.any())).thenReturn(
            Optional.of(new FagsakInfomasjonDto(AKTØR_ID, FORELDREPENGER_FØDSEL.getOffisiellKode())));

        when(arkivJournalpost.getHovedtype()).thenReturn(SØKNAD_SVANGERSKAPSPENGER);
        assertThrows(FunksjonellException.class,
            () -> journalføringTjeneste.oppdaterJournalpostOgFerdigstill(ENHETID, SAKSNUMMER, arkivJournalpost, null,
                tomDokumentListe, null));
    }

    @Test
    void skalValidereAtFagsakFinnes() {
        when(fagsak.finnFagsakInfomasjon(any())).thenReturn(Optional.empty());

        Exception ex = assertThrows(FunksjonellException.class,
            () -> journalføringTjeneste.oppdaterJournalpostOgFerdigstill(ENHETID, SAKSNUMMER, arkivJournalpost, null,
                tomDokumentListe, null));

        assertThat(ex.getMessage()).contains("Kan ikke journalføre på saksnummer");
    }

    @Test
    void skalKjøreHeltIgjennomNaarJournaltilstandIkkeErEndelig() {
        when(arkivJournalpost.getTilstand()).thenReturn(Journalstatus.MOTTATT);
        when(arkivJournalpost.getHovedtype()).thenReturn(SØKNAD_ENGANGSSTØNAD_FØDSEL);

        journalføringTjeneste.oppdaterJournalpostOgFerdigstill(ENHETID, SAKSNUMMER, arkivJournalpost, null, Collections.emptyList(), null);

        verify(arkiv).oppdaterMedSak(JOURNALPOST_ID, SAKSNUMMER, AKTØR_ID);
        verify(arkiv).ferdigstillJournalføring(JOURNALPOST_ID, ENHETID);
        verify(klargjører).klargjør(any(), eq(SAKSNUMMER), eq(JOURNALPOST_ID), any(), any(), eq(ENGANGSSTØNAD_FØDSEL), any(), any(), any(), any());
    }

    @Test
    void skalTillateJournalførinAvInntektsmeldingForeldrepender() throws Exception {
        when(arkivJournalpost.getHovedtype()).thenReturn(DokumentTypeId.INNTEKTSMELDING);
        when(fagsak.finnFagsakInfomasjon(ArgumentMatchers.any())).thenReturn(
            Optional.of(new FagsakInfomasjonDto(AKTØR_ID, FORELDREPENGER_FØDSEL.getOffisiellKode())));

        when(arkivJournalpost.getInnholderStrukturertInformasjon()).thenReturn(true);
        when(arkivJournalpost.getStrukturertPayload()).thenReturn(readFile("testdata/inntektsmelding-foreldrepenger.xml"));
        when(arkivJournalpost.getTilstand()).thenReturn(Journalstatus.MOTTATT);

        journalføringTjeneste.oppdaterJournalpostOgFerdigstill(ENHETID, SAKSNUMMER, arkivJournalpost, null, Collections.emptyList(), null);

        verify(arkiv).oppdaterMedSak(JOURNALPOST_ID, SAKSNUMMER, AKTØR_ID);
        verify(arkiv).ferdigstillJournalføring(JOURNALPOST_ID, ENHETID);
        verify(klargjører).klargjør(eq(readFile("testdata/inntektsmelding-foreldrepenger.xml")), eq(SAKSNUMMER), eq(JOURNALPOST_ID), any(), any(),
            eq(FORELDREPENGER_FØDSEL), any(), any(), any(), any());
    }

    @Test
    void skalIkkeTillateJournalførinAvInntektsmeldingSvangerskapspenger() throws Exception {
        when(arkivJournalpost.getHovedtype()).thenReturn(INNTEKTSMELDING);
        when(fagsak.finnFagsakInfomasjon(ArgumentMatchers.any())).thenReturn(
            Optional.of(new FagsakInfomasjonDto(AKTØR_ID, FORELDREPENGER_FØDSEL.getOffisiellKode())));

        when(arkivJournalpost.getInnholderStrukturertInformasjon()).thenReturn(true);
        when(arkivJournalpost.getStrukturertPayload()).thenReturn(readFile("testdata/inntektsmelding-svangerskapspenger.xml"));

        assertThrows(FunksjonellException.class,
            () -> journalføringTjeneste.oppdaterJournalpostOgFerdigstill(ENHETID, SAKSNUMMER, arkivJournalpost, null,
                tomDokumentListe, null));
    }

    @Test
    void skalIkkeTillateJournalførinAvSøknadMedUttakFørGrense() throws Exception {
        when(arkivJournalpost.getHovedtype()).thenReturn(SØKNAD_FORELDREPENGER_FØDSEL);
        when(fagsak.finnFagsakInfomasjon(ArgumentMatchers.any())).thenReturn(
            Optional.of(new FagsakInfomasjonDto(AKTØR_ID, FORELDREPENGER.getOffisiellKode())));

        when(arkivJournalpost.getStrukturertPayload()).thenReturn(readFile("testdata/selvb-soeknad-forp-uttak-før-konfigverdi.xml"));
        when(arkivJournalpost.getInnholderStrukturertInformasjon()).thenReturn(true);

        FunksjonellException ex = assertThrows(FunksjonellException.class,
            () -> journalføringTjeneste.oppdaterJournalpostOgFerdigstill(ENHETID, SAKSNUMMER, arkivJournalpost, null,
                tomDokumentListe, null));
        assertThat(ex.getMessage()).contains("For tidlig");
    }

    @Test
    void skalIkkeTillateJournalførinAvSøknadMedOmsorgFørGrense() throws Exception {
        when(arkivJournalpost.getHovedtype()).thenReturn(SØKNAD_FORELDREPENGER_ADOPSJON);
        when(fagsak.finnFagsakInfomasjon(ArgumentMatchers.any())).thenReturn(
            Optional.of(new FagsakInfomasjonDto(AKTØR_ID, FORELDREPENGER.getOffisiellKode())));

        when(arkivJournalpost.getStrukturertPayload()).thenReturn(readFile("testdata/fp-adopsjon-far.xml"));
        when(arkivJournalpost.getInnholderStrukturertInformasjon()).thenReturn(true);

        var e = assertThrows(FunksjonellException.class,
            () -> journalføringTjeneste.oppdaterJournalpostOgFerdigstill(ENHETID, SAKSNUMMER, arkivJournalpost, null,
                tomDokumentListe, null));
        assertThat(e.getMessage()).contains("For tidlig");
    }

    @Test
    void skalTillateJournalførinAvSøknadMedUttakEtterGrense() throws Exception {
        when(arkivJournalpost.getHovedtype()).thenReturn(SØKNAD_FORELDREPENGER_FØDSEL);
        when(fagsak.finnFagsakInfomasjon(ArgumentMatchers.any())).thenReturn(
            Optional.of(new FagsakInfomasjonDto(AKTØR_ID, FORELDREPENGER.getOffisiellKode())));

        when(arkivJournalpost.getStrukturertPayload()).thenReturn(readFile("testdata/selvb-soeknad-forp.xml"));
        when(arkivJournalpost.getInnholderStrukturertInformasjon()).thenReturn(true);
        when(arkivJournalpost.getTilstand()).thenReturn(Journalstatus.MOTTATT);

        journalføringTjeneste.oppdaterJournalpostOgFerdigstill(ENHETID, SAKSNUMMER, arkivJournalpost, null, Collections.emptyList(), null);

        verify(arkiv).oppdaterMedSak(JOURNALPOST_ID, SAKSNUMMER, AKTØR_ID);
        verify(arkiv).ferdigstillJournalføring(JOURNALPOST_ID, ENHETID);
        verify(klargjører).klargjør(eq(readFile("testdata/selvb-soeknad-forp.xml")), eq(SAKSNUMMER), eq(JOURNALPOST_ID), any(), any(),
            eq(FORELDREPENGER_FØDSEL), any(), any(), any(), any());
    }

    @Test
    void skalIgnorereUkjentStrukturertData() throws Exception {
        when(arkivJournalpost.getHovedtype()).thenReturn(ETTERSENDT_KLAGE);
        when(fagsak.finnFagsakInfomasjon(ArgumentMatchers.any())).thenReturn(
            Optional.of(new FagsakInfomasjonDto(AKTØR_ID, FORELDREPENGER.getOffisiellKode())));

        when(arkivJournalpost.getStrukturertPayload()).thenReturn(readFile("testdata/metadata.json"));
        when(arkivJournalpost.getInnholderStrukturertInformasjon()).thenReturn(true);
        when(arkivJournalpost.getTilstand()).thenReturn(Journalstatus.MOTTATT);

        journalføringTjeneste.oppdaterJournalpostOgFerdigstill(ENHETID, SAKSNUMMER, arkivJournalpost, null, Collections.emptyList(), null);

        verify(arkiv).oppdaterMedSak(JOURNALPOST_ID, SAKSNUMMER, AKTØR_ID);
        verify(arkiv).ferdigstillJournalføring(JOURNALPOST_ID, ENHETID);
        verify(klargjører).klargjør(eq(null), eq(SAKSNUMMER), eq(JOURNALPOST_ID), any(), any(), eq(FORELDREPENGER), any(), any(), any(), any());
    }

    @Test
    void skalTillateJournalførinAvSøknadMedOmsorgEtterGrense() throws Exception {
        when(arkivJournalpost.getHovedtype()).thenReturn(SØKNAD_FORELDREPENGER_ADOPSJON);
        when(fagsak.finnFagsakInfomasjon(ArgumentMatchers.any())).thenReturn(
            Optional.of(new FagsakInfomasjonDto(AKTØR_ID, SØKNAD_FORELDREPENGER_ADOPSJON.getOffisiellKode())));

        when(arkivJournalpost.getStrukturertPayload()).thenReturn(readFile("testdata/fp-adopsjon-mor.xml"));
        when(arkivJournalpost.getInnholderStrukturertInformasjon()).thenReturn(true);
        when(arkivJournalpost.getTilstand()).thenReturn(Journalstatus.MOTTATT);

        journalføringTjeneste.oppdaterJournalpostOgFerdigstill(ENHETID, SAKSNUMMER, arkivJournalpost, null, Collections.emptyList(), null);

        verify(arkiv).oppdaterMedSak(JOURNALPOST_ID, SAKSNUMMER, AKTØR_ID);
        verify(arkiv).ferdigstillJournalføring(JOURNALPOST_ID, ENHETID);
        verify(klargjører).klargjør(eq(readFile("testdata/fp-adopsjon-mor.xml")), eq(SAKSNUMMER), eq(JOURNALPOST_ID), any(), any(),
            eq(FORELDREPENGER_ADOPSJON), any(), any(), any(), any());
    }

    @Test
    void skalTillateJournalførinAvEndringsSøknadMedAnnetSaksnummer() throws Exception {
        when(arkivJournalpost.getHovedtype()).thenReturn(FORELDREPENGER_ENDRING_SØKNAD);
        when(fagsak.finnFagsakInfomasjon(ArgumentMatchers.any())).thenReturn(
            Optional.of(new FagsakInfomasjonDto(AKTØR_ID, FORELDREPENGER.getOffisiellKode())));

        when(arkivJournalpost.getStrukturertPayload()).thenReturn(readFile("testdata/selvb-soeknad-endring.xml"));
        when(arkivJournalpost.getInnholderStrukturertInformasjon()).thenReturn(true);
        when(arkivJournalpost.getTilstand()).thenReturn(Journalstatus.MOTTATT);

        journalføringTjeneste.oppdaterJournalpostOgFerdigstill(ENHETID, SAKSNUMMER, arkivJournalpost, null, Collections.emptyList(), null);

        verify(arkiv).oppdaterMedSak(JOURNALPOST_ID, SAKSNUMMER, AKTØR_ID);
        verify(arkiv).ferdigstillJournalføring(JOURNALPOST_ID, ENHETID);
        verify(klargjører).klargjør(eq(readFile("testdata/selvb-soeknad-endring.xml")), eq(SAKSNUMMER), eq(JOURNALPOST_ID), any(), any(),
            eq(FORELDREPENGER), any(), any(), any(), any());
    }

    @Test
    void skalKjøreHeltIgjennomNaarJournaltilstandErEndelig() {
        when(arkivJournalpost.getTilstand()).thenReturn(Journalstatus.JOURNALFOERT);
        when(arkivJournalpost.getHovedtype()).thenReturn(SØKNAD_ENGANGSSTØNAD_FØDSEL);

        journalføringTjeneste.oppdaterJournalpostOgFerdigstill(ENHETID, SAKSNUMMER, arkivJournalpost, null, Collections.emptyList(), null);

        verify(klargjører).klargjør(any(), eq(SAKSNUMMER), eq(JOURNALPOST_ID), any(), any(), eq(ENGANGSSTØNAD_FØDSEL), any(), any(), any(), any());
    }

    @Test
    void verifisereAtRiktigOppdateringAvJournalpostKallesOgMedriktigeParametere() {
        var nyTittel = "Søknad om foreldrepenger ved fødsel";
        var forrigeTittel = "Mor er innlagt i helseinstitusjon";
        var dokumenterMedNyeTitler = (List.of(opprettDokument(nyTittel)));
        var nyDokumentTypeId = DokumentTypeId.fraTermNavn(nyTittel);

        when(fagsak.finnFagsakInfomasjon(ArgumentMatchers.any())).thenReturn(
            Optional.of(new FagsakInfomasjonDto(AKTØR_ID, FORELDREPENGER_FØDSEL.getOffisiellKode())));

        when(arkivJournalpost.getHovedtype()).thenReturn(DOK_INNLEGGELSE);
        when(arkivJournalpost.getTilstand()).thenReturn(Journalstatus.MOTTATT);
        when(arkivJournalpost.getKanal()).thenReturn(MottakKanal.SKAN_IM.getKode());
        when(arkivJournalpost.getOriginalJournalpost()).thenReturn(opprettJournalpost(forrigeTittel,
            List.of(new DokumentInfo("1", forrigeTittel, "kode", null, null))));

        journalføringTjeneste.oppdaterJournalpostOgFerdigstill(ENHETID,
            SAKSNUMMER,
            arkivJournalpost,
            nyTittel,
            dokumenterMedNyeTitler,
            nyDokumentTypeId);

        verify(arkiv, times(1)).oppdaterJournalpostVedManuellJournalføring(JOURNALPOST_ID,
            nyTittel,
            List.of(new OppdaterJournalpostRequest.DokumentInfoOppdater("1", nyTittel, null)),
            arkivJournalpost,
            AKTØR_ID,
            FORELDREPENGER_FØDSEL);
        verify(arkiv).oppdaterMedSak(JOURNALPOST_ID, SAKSNUMMER, AKTØR_ID);
        verify(arkiv).ferdigstillJournalføring(JOURNALPOST_ID, ENHETID);
        verify(klargjører).klargjør(any(), eq(SAKSNUMMER), eq(JOURNALPOST_ID), any(), any(), eq(FORELDREPENGER_FØDSEL), any(), any(), any(), any());
    }

    @Test
    void verifisereAtRiktigOppdateringAvGenerellJournalpostKallesOgMedriktigeParametere() {
        var nyTittel = "Søknad om foreldrepenger ved fødsel";
        var forrigeTittel = "Mor er innlagt i helseinstitusjon";
        var dokumenterMedNyeTitler = (List.of(opprettDokument(nyTittel)));
        var nyDokumentTypeId = DokumentTypeId.fraTermNavn(nyTittel);

        when(arkivJournalpost.getHovedtype()).thenReturn(DOK_INNLEGGELSE);
        when(arkivJournalpost.getTilstand()).thenReturn(Journalstatus.MOTTATT);
        when(arkivJournalpost.getKanal()).thenReturn(MottakKanal.SKAN_IM.getKode());
        when(arkivJournalpost.getOriginalJournalpost()).thenReturn(opprettJournalpost(forrigeTittel,
            List.of(new DokumentInfo("1", forrigeTittel, "kode", null, null))));

        journalføringTjeneste.oppdaterJournalpostOgFerdigstillGenerellSak(ENHETID,
            arkivJournalpost,
            AKTØR_ID,
            nyTittel,
            dokumenterMedNyeTitler,
            nyDokumentTypeId);

        verify(arkiv, times(1)).oppdaterJournalpostVedManuellJournalføring(JOURNALPOST_ID,
            nyTittel,
            List.of(new OppdaterJournalpostRequest.DokumentInfoOppdater("1", nyTittel, null)),
            arkivJournalpost,
            AKTØR_ID,
            FORELDREPENGER_FØDSEL);
        verify(arkiv).oppdaterMedGenerellSak(JOURNALPOST_ID, AKTØR_ID);
        verify(arkiv).ferdigstillJournalføring(JOURNALPOST_ID, ENHETID);
    }

    @Test
    void verifisereAtFeilKastesNårJournalpostErFraSelvbetjening() {
        var nyTittel = "Mor er innlagt i helseinstitusjon";
        var dokumenterMedNyeTitler = (List.of(opprettDokument(nyTittel)));
        var nyDokumentTypeId = DokumentTypeId.fraTermNavn(nyTittel);

        when(fagsak.finnFagsakInfomasjon(ArgumentMatchers.any())).thenReturn(
            Optional.of(new FagsakInfomasjonDto(AKTØR_ID, FORELDREPENGER_FØDSEL.getOffisiellKode())));

        when(arkivJournalpost.getHovedtype()).thenReturn(SØKNAD_FORELDREPENGER_FØDSEL);
        when(arkivJournalpost.getTilstand()).thenReturn(Journalstatus.MOTTATT);
        when(arkivJournalpost.getKanal()).thenReturn(MottakKanal.SELVBETJENING.getKode());


        var e = assertThrows(FunksjonellException.class,
            () -> journalføringTjeneste.oppdaterJournalpostOgFerdigstill(ENHETID,
                SAKSNUMMER,
                arkivJournalpost,
                nyTittel,
                dokumenterMedNyeTitler,
                nyDokumentTypeId));
        assertThat(e.getMessage()).contains("Kan ikke endre tittel på journalpost med id 123 som kommer fra Selvbetjening eller Altinn");
    }

    @Test
    void skalTillateKnyttTilAnnenSakInntektsmeldingForeldrepenger() throws Exception {
        when(arkivJournalpost.getHovedtype()).thenReturn(DokumentTypeId.INNTEKTSMELDING);
        when(fagsak.finnFagsakInfomasjon(ArgumentMatchers.any())).thenReturn(
            Optional.of(new FagsakInfomasjonDto(AKTØR_ID, FORELDREPENGER_FØDSEL.getOffisiellKode())));

        when(arkivJournalpost.getStrukturertPayload()).thenReturn(readFile("testdata/inntektsmelding-foreldrepenger.xml"));

        journalføringTjeneste.knyttTilAnnenSak(arkivJournalpost, ENHETID, SAKSNUMMER);

        verify(arkiv).knyttTilAnnenSak(arkivJournalpost, ENHETID, SAKSNUMMER, AKTØR_ID);
    }


    private Journalpost opprettJournalpost(String tittel, List<DokumentInfo> dokumenter) {
        return new Journalpost(JOURNALPOST_ID,
            "I",
            "MOTTATT",
            LocalDateTime.now(),
            tittel,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            dokumenter);
    }

    private FerdigstillJournalføringTjeneste.DokumenterMedNyTittel opprettDokument(String tittel) {
        return new FerdigstillJournalføringTjeneste.DokumenterMedNyTittel("1", tittel);
    }

    String readFile(String filename) throws URISyntaxException, IOException {
        Path path = Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource(filename)).toURI());
        return Files.readString(path);
    }

}
