package no.nav.foreldrepenger.mottak.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
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
import no.nav.foreldrepenger.mottak.behandlendeenhet.EnhetsTjeneste;
import no.nav.foreldrepenger.mottak.domene.dokument.Dokument;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentRepository;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.journal.JournalTjeneste;
import no.nav.foreldrepenger.mottak.journal.dokumentforsendelse.DokumentforsendelseRequest;
import no.nav.foreldrepenger.mottak.journal.dokumentforsendelse.DokumentforsendelseTestUtil;
import no.nav.foreldrepenger.mottak.journal.dokumentforsendelse.JournalTilstand;
import no.nav.foreldrepenger.mottak.tjeneste.TilJournalføringTjeneste;
import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseStatus;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

public class MidlJournalføringTaskTest {
    private static final String SAKSNUMMER = "9876543";
    private static final String AKTØR_ID = "9000000000009";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    
    @Mock
    private ProsessTaskRepository prosessTaskRepositoryMock;
    @Mock
    private JournalTjeneste journalTjenesteMock;
    @Mock
    private EnhetsTjeneste enhetsTjenesteMock;
    @Mock
    private DokumentRepository dokumentRepositoryMock;

    private MidlJournalføringTask task;
    private ProsessTaskData ptd;
    private String enhetId = "1234";
    private UUID forsendelseId;

    @Before
    public void setup() {
        forsendelseId = UUID.randomUUID();
        journalTjenesteMock = mock(JournalTjeneste.class);
        prosessTaskRepositoryMock = mock(ProsessTaskRepository.class);
        enhetsTjenesteMock = mock(EnhetsTjeneste.class);
        dokumentRepositoryMock = mock(DokumentRepository.class);

        TilJournalføringTjeneste tilJournalføringTjeneste = new TilJournalføringTjeneste(journalTjenesteMock, dokumentRepositoryMock);

        task = new MidlJournalføringTask(prosessTaskRepositoryMock, tilJournalføringTjeneste, dokumentRepositoryMock);

        ptd = new ProsessTaskData(TilJournalføringTask.TASKNAME);
        ptd.setSekvens("1");

        when(enhetsTjenesteMock.hentFordelingEnhetId(any(), any(), any(), any())).thenReturn(enhetId);

    }

    @Test
    public void test_skalVedJournalføringAvDokumentForsendelseFåJournalTilstandEndeligJournalført() {
        MottakMeldingDataWrapper data = new MottakMeldingDataWrapper(ptd);

        List<Dokument> dokumenter = DokumentforsendelseTestUtil.lagHoveddokumentMedXmlOgPdf(forsendelseId, DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL);

        when(journalTjenesteMock.journalførDokumentforsendelse(any(DokumentforsendelseRequest.class))).thenReturn(DokumentforsendelseTestUtil.lagDokumentforsendelseRespons(JournalTilstand.ENDELIG_JOURNALFØRT, 3));
        when(dokumentRepositoryMock.hentEksaktDokumentMetadata(any(UUID.class))).thenReturn(DokumentforsendelseTestUtil.lagMetadata(forsendelseId, SAKSNUMMER));
        when(dokumentRepositoryMock.hentDokumenter(any(UUID.class))).thenReturn(dokumenter);

        data.setForsendelseId(forsendelseId);
        data.setAktørId(AKTØR_ID);
        data.setBehandlingTema(BehandlingTema.FORELDREPENGER_FØDSEL);
        data.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);

        ArgumentCaptor<DokumentforsendelseRequest> dokCapture = ArgumentCaptor.forClass(DokumentforsendelseRequest.class);

        MottakMeldingDataWrapper target = task.doTask(data);

        verify(journalTjenesteMock).journalførDokumentforsendelse(dokCapture.capture());
        verify(dokumentRepositoryMock).oppdaterForseldelseMedArkivId(any(UUID.class), any(), any(ForsendelseStatus.class));

        DokumentforsendelseRequest request = dokCapture.getValue();
        assertThat(request.isRetrying()).isFalse();
        assertThat(request.getForsøkEndeligJF()).isFalse();
        assertThat(target.getArkivId()).isEqualTo(DokumentforsendelseTestUtil.lagDokumentforsendelseRespons(JournalTilstand.MIDLERTIDIG_JOURNALFØRT, 3).getJournalpostId());
    }
}
