package no.nav.foreldrepenger.mottak.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverdi.Tema;
import no.nav.foreldrepenger.mottak.domene.dokument.Dokument;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentRepository;
import no.nav.foreldrepenger.mottak.domene.oppgavebehandling.OpprettGSakOppgaveTask;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingFeil;
import no.nav.foreldrepenger.mottak.felles.kafka.LoggingHendelseProdusent;
import no.nav.foreldrepenger.mottak.journal.ArkivTjeneste;
import no.nav.foreldrepenger.mottak.journal.JournalTjeneste;
import no.nav.foreldrepenger.mottak.journal.dokumentforsendelse.DokumentforsendelseRequest;
import no.nav.foreldrepenger.mottak.journal.dokumentforsendelse.DokumentforsendelseTestUtil;
import no.nav.foreldrepenger.mottak.journal.dokumentforsendelse.JournalTilstand;
import no.nav.foreldrepenger.mottak.tjeneste.TilJournalføringTjeneste;
import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseStatus;
import no.nav.vedtak.exception.VLException;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumerMedCache;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

public class TilJournalføringTaskTest {
    private static final String ARKIV_ID = "234567";
    private static final String SAKSNUMMER = "9876543";
    private static final String AKTØR_ID = "9000000000009";
    private static final String BRUKER_FNR = "99999999899";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private ProsessTaskRepository prosessTaskRepositoryMock;
    @Mock
    private ArkivTjeneste arkivTjeneste;
    @Mock
    private JournalTjeneste journalTjenesteMock;
    @Mock
    private DokumentRepository dokumentRepositoryMock;
    @Mock
    private AktørConsumerMedCache aktørConsumerMock;

    private TilJournalføringTask task;
    private ProsessTaskData ptd;
    private UUID forsendelseId;

    @Before
    public void setup() {
        forsendelseId = UUID.randomUUID();
        journalTjenesteMock = mock(JournalTjeneste.class);
        prosessTaskRepositoryMock = mock(ProsessTaskRepository.class);
        dokumentRepositoryMock = mock(DokumentRepository.class);
        aktørConsumerMock = mock(AktørConsumerMedCache.class);
        arkivTjeneste = mock(ArkivTjeneste.class);
        when(aktørConsumerMock.hentPersonIdentForAktørId(AKTØR_ID)).thenReturn(Optional.of(BRUKER_FNR));

        TilJournalføringTjeneste tilJournalføringTjeneste = new TilJournalføringTjeneste(journalTjenesteMock, dokumentRepositoryMock);

        task = new TilJournalføringTask(prosessTaskRepositoryMock, tilJournalføringTjeneste, arkivTjeneste,
                new LoggingHendelseProdusent(), dokumentRepositoryMock, aktørConsumerMock);

        ptd = new ProsessTaskData(TilJournalføringTask.TASKNAME);
        ptd.setSekvens("1");

    }

    @Test
    public void skal_teste_precondition() throws Exception {
        MottakMeldingDataWrapper data = new MottakMeldingDataWrapper(ptd);

        try {
            task.precondition(data);
            fail();
        } catch (VLException e) {
            assertThat(e.getFeil().getKode())
                    .isEqualTo(MottakMeldingFeil.FACTORY.prosesstaskPreconditionManglerProperty("", "", 0L).getKode());
        }
        data.setArkivId(ARKIV_ID);
        try {
            task.precondition(data);
            fail();
        } catch (VLException e) {
            assertThat(e.getFeil().getKode())
                    .isEqualTo(MottakMeldingFeil.FACTORY.prosesstaskPreconditionManglerProperty("", "", 0L).getKode());
        }
        data.setAktørId(AKTØR_ID);

        try {
            task.precondition(data);
            fail();
        } catch (VLException e) {
            assertThat(e.getFeil().getKode())
                    .isEqualTo(MottakMeldingFeil.FACTORY.prosesstaskPreconditionManglerProperty("", "", 0L).getKode());
        }
        data.setSaksnummer("asdf");

        task.precondition(data);
    }

    @Test
    public void test_utfor_uten_mangler() throws Exception {

        MottakMeldingDataWrapper data = new MottakMeldingDataWrapper(ptd);
        data.setArkivId(ARKIV_ID);
        data.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        data.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_FØDSEL);
        data.setSaksnummer("123");
        data.setAktørId(AKTØR_ID);
        data.setForsendelseId(forsendelseId);

        MottakMeldingDataWrapper wrapper = doTaskWithPrecondition(data);

        assertThat(wrapper).isNotNull();
        assertThat(wrapper.getProsessTaskData().getTaskType())
                .as("Forventer at sak uten mangler går videre til neste steg")
                .isNotEqualTo(TilJournalføringTask.TASKNAME);
    }

    @Test
    public void test_mangler_arkivsak() {
        MottakMeldingDataWrapper data = new MottakMeldingDataWrapper(ptd);
        data.setArkivId(ARKIV_ID);
        data.setSaksnummer(SAKSNUMMER);
        data.setAktørId(AKTØR_ID);
        data.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        data.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_FØDSEL);
        data.setForsendelseId(forsendelseId);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        MottakMeldingDataWrapper wrapper = doTaskWithPrecondition(data);

        verify(arkivTjeneste).ferdigstillJournalføring(any(), captor.capture(), any());

        String sak = captor.getValue();

        assertThat(sak).isEqualTo(SAKSNUMMER);

        assertThat(wrapper).isNotNull();
        assertThat(wrapper.getProsessTaskData().getTaskType())
                .as("Forventer at sak uten mangler går videre til neste steg")
                .isNotEqualTo(TilJournalføringTask.TASKNAME);
    }

    @Test
    public void test_mangler_journalforing_gir_mangel_exception() {
        MottakMeldingDataWrapper data = new MottakMeldingDataWrapper(ptd);
        data.setArkivId(ARKIV_ID);
        data.setSaksnummer(SAKSNUMMER);
        data.setAktørId(AKTØR_ID);
        data.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        data.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_FØDSEL);
        data.setForsendelseId(forsendelseId);

        Exception funkExc = new IllegalStateException("bla bla");
        doThrow(funkExc).when(arkivTjeneste).ferdigstillJournalføring(any(), any(), any());

        MottakMeldingDataWrapper wrapper = doTaskWithPrecondition(data);

        assertThat(wrapper).isNotNull();
        assertThat(wrapper.getProsessTaskData().getTaskType()).as("Forventer at sak med mangler går til Gosys")
                .isEqualTo(OpprettGSakOppgaveTask.TASKNAME);
    }

    private MottakMeldingDataWrapper doTaskWithPrecondition(MottakMeldingDataWrapper data) {
        task.precondition(data);
        return task.doTask(data);
    }

    @Test
    public void test_mangler_ting_som_ikke_kan_rettes() {
        MottakMeldingDataWrapper data = new MottakMeldingDataWrapper(ptd);
        data.setArkivId(ARKIV_ID);
        data.setSaksnummer(SAKSNUMMER);
        data.setAktørId(AKTØR_ID);
        data.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        data.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_FØDSEL);

        doThrow(new IllegalArgumentException("blab bla")).when(arkivTjeneste).ferdigstillJournalføring(any(),any(),any());

        MottakMeldingDataWrapper wrapper = doTaskWithPrecondition(data);

        assertThat(wrapper).isNotNull();
        assertThat(wrapper.getProsessTaskData().getTaskType()).as("Forventer at sak med dokumentmangler går til Gosys")
                .isEqualTo(OpprettGSakOppgaveTask.TASKNAME);
    }

    @Test
    public void test_validerDatagrunnlag_uten_feil() throws Exception {
        ProsessTaskData prosessTaskData = ptd;
        MottakMeldingDataWrapper data = new MottakMeldingDataWrapper(prosessTaskData);

        data.setSaksnummer("saksnummer");
        data.setAktørId(AKTØR_ID);
        task.precondition(data);
    }

    @Test
    public void test_skalVedJournalføringAvDokumentForsendelseFåJournalTilstandEndeligJournalført() {
        MottakMeldingDataWrapper data = new MottakMeldingDataWrapper(ptd);

        List<Dokument> dokumenter = DokumentforsendelseTestUtil.lagHoveddokumentMedXmlOgPdf(forsendelseId,
                DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL);

        when(journalTjenesteMock.journalførDokumentforsendelse(any(DokumentforsendelseRequest.class))).thenReturn(
                DokumentforsendelseTestUtil.lagDokumentforsendelseRespons(JournalTilstand.ENDELIG_JOURNALFØRT, 3));
        when(dokumentRepositoryMock.hentEksaktDokumentMetadata(any(UUID.class)))
                .thenReturn(DokumentforsendelseTestUtil.lagMetadata(forsendelseId, SAKSNUMMER));
        when(dokumentRepositoryMock.hentDokumenter(any(UUID.class))).thenReturn(dokumenter);

        data.setForsendelseId(forsendelseId);
        data.setAktørId(AKTØR_ID);
        data.setBehandlingTema(BehandlingTema.FORELDREPENGER_FØDSEL);
        data.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        data.setSaksnummer(SAKSNUMMER);
        data.setRetryingTask("ABC");

        ArgumentCaptor<DokumentforsendelseRequest> dokCapture = ArgumentCaptor
                .forClass(DokumentforsendelseRequest.class);

        task.doTask(data);

        verify(journalTjenesteMock).journalførDokumentforsendelse(dokCapture.capture());
        verify(dokumentRepositoryMock).oppdaterForsendelseMetadata(any(UUID.class), any(), any(),
                any(ForsendelseStatus.class));

        DokumentforsendelseRequest request = dokCapture.getValue();
        assertThat(request.isRetrying()).isTrue();
        assertThat(request.getForsøkEndeligJF()).isTrue();
        assertThat(request.getSaksnummer()).isEqualTo(SAKSNUMMER);
    }

    @Test
    public void test_skalKasteTekniskExceptionNårJournalTilstandIkkeErEndelig() {
        MottakMeldingDataWrapper data = new MottakMeldingDataWrapper(ptd);

        List<Dokument> dokumenter = DokumentforsendelseTestUtil.lagHoveddokumentMedXmlOgPdf(forsendelseId,
                DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL);

        when(journalTjenesteMock.journalførDokumentforsendelse(any(DokumentforsendelseRequest.class))).thenReturn(
                DokumentforsendelseTestUtil.lagDokumentforsendelseRespons(JournalTilstand.MIDLERTIDIG_JOURNALFØRT, 3));
        when(dokumentRepositoryMock.hentEksaktDokumentMetadata(any(UUID.class)))
                .thenReturn(DokumentforsendelseTestUtil.lagMetadata(forsendelseId, SAKSNUMMER));
        when(dokumentRepositoryMock.hentDokumenter(any(UUID.class))).thenReturn(dokumenter);

        data.setForsendelseId(forsendelseId);
        data.setAktørId(AKTØR_ID);
        data.setBehandlingTema(BehandlingTema.FORELDREPENGER_FØDSEL);
        data.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        data.setSaksnummer(SAKSNUMMER);

        data = task.doTask(data);

        verify(journalTjenesteMock).journalførDokumentforsendelse(any(DokumentforsendelseRequest.class));
        verify(dokumentRepositoryMock, times(1)).oppdaterForsendelseMedArkivId(any(UUID.class), any(),
                any(ForsendelseStatus.class));
        assertThat(data).isNotNull();
        assertThat(data.getProsessTaskData().getTaskType()).isEqualTo(OpprettGSakOppgaveTask.TASKNAME);
    }
}
