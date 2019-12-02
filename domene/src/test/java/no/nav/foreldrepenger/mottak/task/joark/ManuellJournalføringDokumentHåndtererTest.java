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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import no.nav.foreldrepenger.fordel.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.fordel.kodeverk.ArkivFilType;
import no.nav.foreldrepenger.fordel.kodeverk.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverk.DokumentKategori;
import no.nav.foreldrepenger.fordel.kodeverk.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.fordel.kodeverk.KodeverkRepositoryImpl;
import no.nav.foreldrepenger.fordel.kodeverk.Tema;
import no.nav.foreldrepenger.fordel.kodeverk.VariantFormat;
import no.nav.foreldrepenger.mottak.domene.oppgavebehandling.OpprettGSakOppgaveTask;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.journal.JournalDokument;
import no.nav.foreldrepenger.mottak.journal.JournalMetadata;
import no.nav.foreldrepenger.mottak.task.HentOgVurderVLSakTask;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumer;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

public class ManuellJournalføringDokumentHåndtererTest {

    private static final String DEFAULT_TASK_FOR_MANUELL_JOURNALFØRING = OpprettGSakOppgaveTask.TASKNAME;

    private static final String ARKIV_ID = JoarkTestsupport.ARKIV_ID;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private KodeverkRepository kodeverkRepository = new KodeverkRepositoryImpl(repoRule.getEntityManager());

    private ProsessTaskData taskData;
    private HentDataFraJoarkTask joarkTaskTestobjekt;
    private MottakMeldingDataWrapper dataWrapper;
    private JoarkDokumentHåndterer håndterer;
    private JoarkTestsupport joarkTestsupport = new JoarkTestsupport();
    private String fastsattInntektsmeldingStartdatoFristForManuellBehandling = "2019-01-01";

    @Before
    public void setUp() throws Exception {
        initMocks(this);

        ProsessTaskRepository ptr = mock(ProsessTaskRepository.class);
        håndterer = mock(JoarkDokumentHåndterer.class);
        AktørConsumer aktørConsumer = mock(AktørConsumer.class);
        joarkTaskTestobjekt = spy(new HentDataFraJoarkTask(ptr, kodeverkRepository, aktørConsumer, håndterer));
        when(håndterer.hentGyldigAktørFraMetadata(any())).thenReturn(Optional.of(AKTØR_ID));
        when(håndterer.hentGyldigAktørFraPersonident(any())).thenReturn(Optional.of(AKTØR_ID));
        taskData = new ProsessTaskData(HentDataFraJoarkTask.TASKNAME);
        taskData.setSekvens("1");
        dataWrapper = new MottakMeldingDataWrapper(kodeverkRepository, taskData);
        dataWrapper.setArkivId(ARKIV_ID);
    }


    @Test
    public void skalHåndtereManuellJournalføringAvInntektsmelding() throws Exception {

        JournalMetadata<DokumentTypeId> journalMetadata = JournalMetadata.builder()
                .medErHoveddokument(true)
                .medArkivFilType(ArkivFilType.XML)
                .medVariantFormat(VariantFormat.ORIGINAL)
                .medDokumentType(DokumentTypeId.INNTEKTSMELDING)
                .medForsendelseMottatt(LocalDate.now())
                .medDokumentKategori(DokumentKategori.ELEKTRONISK_SKJEMA)
                .medBrukerIdentListe(JoarkTestsupport.brukerListe)
                .build();

        List<JournalMetadata<DokumentTypeId>> metadata = Collections.singletonList(journalMetadata);
        doReturn(metadata).when(håndterer).hentJoarkDokumentMetadata(ARKIV_ID);

        String xml = joarkTestsupport.readFile("testsoknader/inntektsmelding-manual-sample.xml");
        JournalDokument<DokumentTypeId> journalDokument = new JournalDokument<DokumentTypeId>(journalMetadata, xml);

        doReturn(journalDokument).when(håndterer).hentJournalDokument(any());

        BehandlingTema actualBehandlingTema = BehandlingTema.UDEFINERT;
        dataWrapper.setBehandlingTema(actualBehandlingTema);
        dataWrapper.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);

        MottakMeldingDataWrapper result = joarkTaskTestobjekt.doTask(dataWrapper);

        assertThat(result.getBehandlingTema()).isEqualTo(BehandlingTema.FORELDREPENGER);
        assertThat(result.getArkivId()).isEqualTo(ARKIV_ID);
        assertThat(result.getProsessTaskData().getTaskType()).isEqualTo(DEFAULT_TASK_FOR_MANUELL_JOURNALFØRING);
        assertThat(result.getAktørId().get()).isEqualTo(AKTØR_ID);
    }

    @Test
    public void skalHåndtereManuellJournalføringMedGyldigFnr() throws Exception {
        List<JournalMetadata<DokumentTypeId>> metadata = Collections.singletonList(joarkTestsupport.lagJournalMetadataUstrukturert(DokumentTypeId.ANNET));
        doReturn(metadata).when(håndterer).hentJoarkDokumentMetadata(ARKIV_ID);

        BehandlingTema actualBehandlingTema = BehandlingTema.ENGANGSSTØNAD_FØDSEL;
        dataWrapper.setBehandlingTema(actualBehandlingTema);
        dataWrapper.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);

        MottakMeldingDataWrapper result = joarkTaskTestobjekt.doTask(dataWrapper);

        assertThat(result.getBehandlingTema()).isEqualTo(actualBehandlingTema);
        assertThat(result.getArkivId()).isEqualTo(ARKIV_ID);
        assertThat(result.getProsessTaskData().getTaskType()).isEqualTo(HentOgVurderVLSakTask.TASKNAME);
        assertThat(result.getAktørId().get()).isEqualTo(AKTØR_ID);
    }

    @Test
    public void skalHåndtereManuellJournalføringMedUgyldigFnr() throws Exception {
        List<JournalMetadata<DokumentTypeId>> metadata = Collections.singletonList(joarkTestsupport.lagJournalMetadataUstrukturert(Collections.singletonList("12789")));
        doReturn(metadata).when(håndterer).hentJoarkDokumentMetadata(ARKIV_ID);
        when(håndterer.hentGyldigAktørFraMetadata(any())).thenReturn(Optional.empty());

        BehandlingTema actualBehandlingTema = BehandlingTema.ENGANGSSTØNAD_FØDSEL;
        dataWrapper.setBehandlingTema(actualBehandlingTema);
        dataWrapper.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);

        MottakMeldingDataWrapper result = joarkTaskTestobjekt.doTask(dataWrapper);

        assertThat(result.getBehandlingTema()).isEqualTo(actualBehandlingTema);
        assertThat(result.getArkivId()).isEqualTo(ARKIV_ID);
        assertThat(result.getProsessTaskData().getTaskType()).isEqualTo(DEFAULT_TASK_FOR_MANUELL_JOURNALFØRING);
        assertThat(result.getAktørId()).isEmpty();
    }

}