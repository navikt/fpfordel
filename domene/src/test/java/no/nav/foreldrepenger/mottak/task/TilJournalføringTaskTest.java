package no.nav.foreldrepenger.mottak.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.Tema;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentRepository;
import no.nav.foreldrepenger.mottak.domene.oppgavebehandling.OpprettGSakOppgaveTask;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingFeil;
import no.nav.foreldrepenger.mottak.felles.kafka.LoggingHendelseProdusent;
import no.nav.foreldrepenger.mottak.journal.ArkivTjeneste;
import no.nav.foreldrepenger.mottak.journal.OpprettetJournalpost;
import no.nav.foreldrepenger.mottak.person.PersonInformasjon;
import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseStatus;
import no.nav.vedtak.exception.VLException;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TilJournalføringTaskTest {
    private static final String ARKIV_ID = "234567";
    private static final String SAKSNUMMER = "9876543";
    private static final String AKTØR_ID = "9000000000009";
    private static final String BRUKER_FNR = "99999999899";

    @Mock
    private ProsessTaskRepository prosessTaskRepositoryMock;
    @Mock
    private ArkivTjeneste arkivTjeneste;
    @Mock
    private DokumentRepository dokumentRepositoryMock;
    @Mock
    private PersonInformasjon aktørConsumerMock;

    private TilJournalføringTask task;
    private ProsessTaskData ptd;
    private UUID forsendelseId;

    @BeforeEach
    public void setup() {
        forsendelseId = UUID.randomUUID();
        when(aktørConsumerMock.hentPersonIdentForAktørId(AKTØR_ID)).thenReturn(Optional.of(BRUKER_FNR));

        task = new TilJournalføringTask(prosessTaskRepositoryMock, arkivTjeneste,
                new LoggingHendelseProdusent(), dokumentRepositoryMock, aktørConsumerMock);

        ptd = new ProsessTaskData(TilJournalføringTask.TASKNAME);
        ptd.setSekvens("1");

    }

    @Test
    public void skal_teste_precondition() {
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
    public void test_utfor_uten_mangler() {

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

        verify(arkivTjeneste).oppdaterMedSak(any(), captor.capture(), any());

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
        doThrow(funkExc).when(arkivTjeneste).ferdigstillJournalføring(any(), any());

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

        doThrow(new IllegalArgumentException("blab bla")).when(arkivTjeneste).ferdigstillJournalføring(any(), any());

        MottakMeldingDataWrapper wrapper = doTaskWithPrecondition(data);

        assertThat(wrapper).isNotNull();
        assertThat(wrapper.getProsessTaskData().getTaskType()).as("Forventer at sak med dokumentmangler går til Gosys")
                .isEqualTo(OpprettGSakOppgaveTask.TASKNAME);
    }

    @Test
    public void test_validerDatagrunnlag_uten_feil() {
        ProsessTaskData prosessTaskData = ptd;
        MottakMeldingDataWrapper data = new MottakMeldingDataWrapper(prosessTaskData);

        data.setSaksnummer("saksnummer");
        data.setAktørId(AKTØR_ID);
        task.precondition(data);
    }

    @Test
    public void test_skalVedJournalføringAvDokumentForsendelseFåJournalTilstandEndeligJournalført() {
        MottakMeldingDataWrapper data = new MottakMeldingDataWrapper(ptd);

        when(arkivTjeneste.opprettJournalpost(forsendelseId, AKTØR_ID, SAKSNUMMER)).thenReturn(new OpprettetJournalpost(ARKIV_ID, true));

        data.setForsendelseId(forsendelseId);
        data.setAktørId(AKTØR_ID);
        data.setBehandlingTema(BehandlingTema.FORELDREPENGER_FØDSEL);
        data.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        data.setSaksnummer(SAKSNUMMER);
        data.setRetryingTask("ABC");

        var next = task.doTask(data);

        verify(dokumentRepositoryMock).oppdaterForsendelseMetadata(any(UUID.class), any(), any(),
                any(ForsendelseStatus.class));

        assertThat(next.getProsessTaskData().getTaskType()).isEqualTo(KlargjorForVLTask.TASKNAME);
    }

    @Test
    public void test_skalTilManuellNårJournalTilstandIkkeErEndelig() {
        MottakMeldingDataWrapper data = new MottakMeldingDataWrapper(ptd);

        when(arkivTjeneste.opprettJournalpost(forsendelseId, AKTØR_ID, SAKSNUMMER)).thenReturn(new OpprettetJournalpost(ARKIV_ID, false));

        data.setForsendelseId(forsendelseId);
        data.setAktørId(AKTØR_ID);
        data.setBehandlingTema(BehandlingTema.FORELDREPENGER_FØDSEL);
        data.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        data.setSaksnummer(SAKSNUMMER);

        data = task.doTask(data);

        verify(dokumentRepositoryMock).oppdaterForsendelseMedArkivId(any(UUID.class), any(), any(ForsendelseStatus.class));
        assertThat(data.getProsessTaskData().getTaskType()).isEqualTo(OpprettGSakOppgaveTask.TASKNAME);
    }
}
