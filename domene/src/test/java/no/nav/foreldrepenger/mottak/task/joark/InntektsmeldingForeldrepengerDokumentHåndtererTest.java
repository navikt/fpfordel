package no.nav.foreldrepenger.mottak.task.joark;

import static no.nav.foreldrepenger.mottak.task.joark.JoarkTestsupport.AKTØR_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import no.nav.foreldrepenger.fordel.kodeverdi.ArkivFilType;
import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentKategori;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverdi.Tema;
import no.nav.foreldrepenger.fordel.kodeverdi.VariantFormat;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentRepository;
import no.nav.foreldrepenger.mottak.domene.oppgavebehandling.OpprettGSakOppgaveTask;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.journal.ArkivTjeneste;
import no.nav.foreldrepenger.mottak.journal.JournalDokument;
import no.nav.foreldrepenger.mottak.journal.JournalMetadata;
import no.nav.foreldrepenger.mottak.task.HentOgVurderVLSakTask;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumerMedCache;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

@RunWith(MockitoJUnitRunner.class)
public class InntektsmeldingForeldrepengerDokumentHåndtererTest {

    private static final String ARKIV_ID = JoarkTestsupport.ARKIV_ID;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private ProsessTaskData taskData;
    private HentDataFraJoarkTask joarkTaskTestobjekt;
    private MottakMeldingDataWrapper dataWrapper;
    private JoarkDokumentHåndterer håndterer;
    private JoarkTestsupport joarkTestsupport = new JoarkTestsupport();
    private JournalMetadata journalMetadata;

    @Before
    public void setUp() throws Exception {
        initMocks(this);

        ProsessTaskRepository ptr = mock(ProsessTaskRepository.class);
        håndterer = mock(JoarkDokumentHåndterer.class);
        AktørConsumerMedCache aktørConsumer = mock(AktørConsumerMedCache.class);
        joarkTaskTestobjekt = spy(new HentDataFraJoarkTask(ptr, aktørConsumer, håndterer, mock(ArkivTjeneste.class), mock(DokumentRepository.class)));
        when(håndterer.hentGyldigAktørFraMetadata(any())).thenReturn(Optional.of(AKTØR_ID));
        when(håndterer.hentGyldigAktørFraPersonident(any())).thenReturn(Optional.of(AKTØR_ID));
        taskData = new ProsessTaskData(HentDataFraJoarkTask.TASKNAME);
        taskData.setSekvens("1");
        dataWrapper = new MottakMeldingDataWrapper(taskData);
        dataWrapper.setArkivId(ARKIV_ID);
        dataWrapper.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        journalMetadata = joarkTestsupport.lagJournalMetadataStrukturert(DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL);
    }

    @Test
    public void skal_håndtere_dokument_som_har_ikke_en_inntektsmelding() throws Exception {
        String aktørId = "9000000000009";
        dataWrapper.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_FØDSEL);
        dataWrapper.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        dataWrapper.setAktørId(aktørId);

        when(håndterer.hentGyldigAktørFraMetadata(any())).thenReturn(Optional.of(aktørId));

        String xml = joarkTestsupport.readFile("testsoknader/engangsstoenad-termin-soeknad.xml");
        JournalDokument journalDokument = new JournalDokument(journalMetadata, xml);
        doReturn(Collections.singletonList(journalMetadata)).when(håndterer).hentJoarkDokumentMetadata(any());
        doReturn(journalDokument).when(håndterer).hentJournalDokument(any());

        MottakMeldingDataWrapper wrapper = joarkTaskTestobjekt.doTask(dataWrapper);

        assertThat(wrapper.getAktørId()).hasValue(aktørId);
        assertThat(wrapper.getProsessTaskData().getTaskType()).isEqualTo(HentOgVurderVLSakTask.TASKNAME);
    }

    @Test
    public void skalHåndtereIntekksmeldingForeldrepengerManuellJournalføringDokumentHåndterer() throws Exception {
        journalMetadata = JournalMetadata.builder()
                .medErHoveddokument(true)
                .medArkivFilType(ArkivFilType.XML)
                .medVariantFormat(VariantFormat.ORIGINAL)
                .medDokumentType(DokumentTypeId.INNTEKTSMELDING)
                .medForsendelseMottatt(LocalDate.now())
                .medDokumentKategori(DokumentKategori.ELEKTRONISK_SKJEMA)
                .medBrukerIdentListe(JoarkTestsupport.brukerListe)
                .build();

        List<JournalMetadata> strukturertJournalMetadataSkanningMetaList = Arrays.asList(journalMetadata);
        doReturn(strukturertJournalMetadataSkanningMetaList).when(håndterer).hentJoarkDokumentMetadata(ARKIV_ID);

        String xml = joarkTestsupport.readFile("testsoknader/inntektsmelding-manual-sample.xml");
        JournalDokument journalDokument = new JournalDokument(journalMetadata, xml);

        doReturn(journalDokument).when(håndterer).hentJournalDokument(Collections.singletonList(journalMetadata));
        BehandlingTema actualBehandlingTema = BehandlingTema.FORELDREPENGER;
        dataWrapper.setBehandlingTema(actualBehandlingTema);
        dataWrapper.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);

        MottakMeldingDataWrapper wrapper = joarkTaskTestobjekt.doTask(dataWrapper);
        assertThat(wrapper.getAktørId()).hasValue(JoarkTestsupport.AKTØR_ID);
        assertThat(wrapper.getProsessTaskData().getTaskType()).isEqualTo(OpprettGSakOppgaveTask.TASKNAME);
    }

    @Test
    public void skalHåndtereIntekksmeldingForeldrepengerElektronikJournalføringDokumentHåndterer() throws Exception {
        journalMetadata = JournalMetadata.builder()
                .medErHoveddokument(true)
                .medArkivFilType(ArkivFilType.XML)
                .medVariantFormat(VariantFormat.ORIGINAL)
                .medDokumentType(DokumentTypeId.INNTEKTSMELDING)
                .medForsendelseMottatt(LocalDate.now())
                .medForsendelseMottattTidspunkt(LocalDateTime.now())
                .medDokumentKategori(DokumentKategori.ELEKTRONISK_SKJEMA)
                .medBrukerIdentListe(JoarkTestsupport.brukerListe)
                .build();

        List<JournalMetadata> strukturertJournalMetadataSkanningMetaList = Arrays.asList(journalMetadata);

        String xml = joarkTestsupport.readFile("testsoknader/inntektsmelding-elektronisk-sample.xml");
        JournalDokument journalDokument = new JournalDokument(journalMetadata, xml);

        doReturn(journalDokument).when(håndterer).hentJournalDokument(Collections.singletonList(journalMetadata));
        doReturn(strukturertJournalMetadataSkanningMetaList).when(håndterer).hentJoarkDokumentMetadata(ARKIV_ID);

        BehandlingTema actualBehandlingTema = BehandlingTema.FORELDREPENGER;
        dataWrapper.setBehandlingTema(actualBehandlingTema);
        dataWrapper.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);

        MottakMeldingDataWrapper wrapper = joarkTaskTestobjekt.doTask(dataWrapper);

        assertThat(wrapper.getProsessTaskData().getTaskType()).isEqualTo(HentOgVurderVLSakTask.TASKNAME);
    }

    @Test
    public void skalHåndtereInntektsmeldingUtenStartdatoMedManuellJournalføring() throws Exception {
        journalMetadata = JournalMetadata.builder()
                .medErHoveddokument(true)
                .medArkivFilType(ArkivFilType.XML)
                .medVariantFormat(VariantFormat.ORIGINAL)
                .medDokumentType(DokumentTypeId.INNTEKTSMELDING)
                .medForsendelseMottatt(LocalDate.now())
                .medDokumentKategori(DokumentKategori.ELEKTRONISK_SKJEMA)
                .medBrukerIdentListe(JoarkTestsupport.brukerListe)
                .build();

        List<JournalMetadata> strukturertJournalMetadataSkanningMetaList = Arrays.asList(journalMetadata);
        doReturn(strukturertJournalMetadataSkanningMetaList).when(håndterer).hentJoarkDokumentMetadata(ARKIV_ID);

        String xml = joarkTestsupport
                .readFile("testsoknader/inntektsmelding-manual-uten-startdato-foreldrepenger-periode-sample.xml");
        JournalDokument journalDokument = new JournalDokument(journalMetadata, xml);

        doReturn(journalDokument).when(håndterer).hentJournalDokument(Collections.singletonList(journalMetadata));

        BehandlingTema actualBehandlingTema = BehandlingTema.FORELDREPENGER;
        dataWrapper.setBehandlingTema(actualBehandlingTema);
        dataWrapper.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);

        MottakMeldingDataWrapper wrapper = joarkTaskTestobjekt.doTask(dataWrapper);
        assertThat(wrapper.getAktørId()).hasValue(JoarkTestsupport.AKTØR_ID);
        assertThat(wrapper.getProsessTaskData().getTaskType()).isEqualTo(OpprettGSakOppgaveTask.TASKNAME);
    }

    @Test
    public void skalHåndtereInntektsmeldingUtenGyldigFNR() throws Exception {
        journalMetadata = JournalMetadata.builder()
                .medErHoveddokument(true)
                .medArkivFilType(ArkivFilType.XML)
                .medVariantFormat(VariantFormat.ORIGINAL)
                .medDokumentType(DokumentTypeId.INNTEKTSMELDING)
                .medForsendelseMottatt(LocalDate.now())
                .medDokumentKategori(DokumentKategori.ELEKTRONISK_SKJEMA)
                .build();

        List<JournalMetadata> strukturertJournalMetadataSkanningMetaList = Arrays.asList(journalMetadata);
        doReturn(strukturertJournalMetadataSkanningMetaList).when(håndterer).hentJoarkDokumentMetadata(ARKIV_ID);

        when(håndterer.hentGyldigAktørFraMetadata(any())).thenReturn(Optional.empty());
        when(håndterer.hentGyldigAktørFraPersonident(any())).thenReturn(Optional.empty());

        String xml = joarkTestsupport
                .readFile("testsoknader/inntektsmelding-manual-uten-startdato-foreldrepenger-periode-sample.xml");
        JournalDokument journalDokument = new JournalDokument(journalMetadata, xml);

        doReturn(journalDokument).when(håndterer).hentJournalDokument(Collections.singletonList(journalMetadata));

        BehandlingTema actualBehandlingTema = BehandlingTema.FORELDREPENGER;
        dataWrapper.setBehandlingTema(actualBehandlingTema);
        dataWrapper.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);

        MottakMeldingDataWrapper wrapper = joarkTaskTestobjekt.doTask(dataWrapper);
        assertThat(wrapper.getAktørId()).isEmpty();
        assertThat(wrapper.getProsessTaskData().getTaskType()).isEqualTo(OpprettGSakOppgaveTask.TASKNAME);
    }
}
