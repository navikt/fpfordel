package no.nav.foreldrepenger.mottak.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import no.nav.foreldrepenger.journalføring.oppgave.Journalføringsoppgave;
import no.nav.foreldrepenger.mottak.domene.oppgavebehandling.OpprettGSakOppgaveTask;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.journal.ArkivTjeneste;
import no.nav.foreldrepenger.mottak.journal.OpprettetJournalpost;
import no.nav.foreldrepenger.mottak.person.PersonInformasjon;
import no.nav.vedtak.exception.VLException;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;
import no.nav.vedtak.felles.prosesstask.api.TaskType;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
class TilJournalføringTaskTest {
    private static final String ARKIV_ID = "234567";
    private static final String SAKSNUMMER = "9876543";
    private static final String AKTØR_ID = "9000000000009";
    private static final String BRUKER_FNR = "99999999899";

    @Mock
    private ProsessTaskTjeneste taskTjenesteMock;
    @Mock
    private ArkivTjeneste arkivTjeneste;
    @Mock
    private PersonInformasjon aktørConsumerMock;
    @Mock
    private Journalføringsoppgave journalføringsOppgave;

    private TilJournalføringTask task;
    private ProsessTaskData ptd;
    private UUID forsendelseId;

    @BeforeEach
    void setup() {
        forsendelseId = UUID.randomUUID();
        when(aktørConsumerMock.hentPersonIdentForAktørId(AKTØR_ID)).thenReturn(Optional.of(BRUKER_FNR));

        task = new TilJournalføringTask(taskTjenesteMock, arkivTjeneste, aktørConsumerMock, journalføringsOppgave);

        ptd = ProsessTaskData.forProsessTask(TilJournalføringTask.class);

    }

    @Test
    void skal_teste_precondition() {
        var data = new MottakMeldingDataWrapper(ptd);
        var e = assertThrows(VLException.class, () -> task.precondition(data));
        data.setArkivId(ARKIV_ID);
        e = assertThrows(VLException.class, () -> task.precondition(data));
        data.setAktørId(AKTØR_ID);
        data.setSaksnummer("asdf");
        task.precondition(data);
    }

    @Test
    void test_utfor_uten_mangler() {

        var data = new MottakMeldingDataWrapper(ptd);
        data.setArkivId(ARKIV_ID);
        data.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        data.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_FØDSEL);
        data.setSaksnummer("123");
        data.setAktørId(AKTØR_ID);
        data.setForsendelseId(forsendelseId);

        var wrapper = doTaskWithPrecondition(data);

        assertThat(wrapper).isNotNull();
        assertThat(wrapper.getProsessTaskData().taskType()).as("Forventer at sak uten mangler går videre til neste steg")
            .isNotEqualTo(TaskType.forProsessTask(TilJournalføringTask.class));
    }

    @Test
    void test_mangler_arkivsak() {
        var data = new MottakMeldingDataWrapper(ptd);
        data.setArkivId(ARKIV_ID);
        data.setSaksnummer(SAKSNUMMER);
        data.setAktørId(AKTØR_ID);
        data.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        data.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_FØDSEL);
        data.setForsendelseId(forsendelseId);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        var wrapper = doTaskWithPrecondition(data);

        verify(arkivTjeneste).oppdaterMedSak(any(), captor.capture(), any());

        String sak = captor.getValue();

        assertThat(sak).isEqualTo(SAKSNUMMER);

        assertThat(wrapper).isNotNull();
        assertThat(wrapper.getProsessTaskData().taskType()).as("Forventer at sak uten mangler går videre til neste steg")
            .isNotEqualTo(TaskType.forProsessTask(TilJournalføringTask.class));
    }

    @Test
    void test_mangler_journalforing_gir_mangel_exception() {
        var data = new MottakMeldingDataWrapper(ptd);
        data.setArkivId(ARKIV_ID);
        data.setSaksnummer(SAKSNUMMER);
        data.setAktørId(AKTØR_ID);
        data.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        data.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_FØDSEL);
        data.setForsendelseId(forsendelseId);

        Exception funkExc = new IllegalStateException("bla bla");
        doThrow(funkExc).when(arkivTjeneste).ferdigstillJournalføring(any(), any());

        var wrapper = doTaskWithPrecondition(data);

        assertThat(wrapper).isNotNull();
        assertThat(wrapper.getProsessTaskData().taskType()).as("Forventer at sak med mangler går til Gosys")
            .isEqualTo(TaskType.forProsessTask(OpprettGSakOppgaveTask.class));
    }

    private MottakMeldingDataWrapper doTaskWithPrecondition(MottakMeldingDataWrapper data) {
        task.precondition(data);
        return task.doTask(data);
    }

    @Test
    void test_mangler_ting_som_ikke_kan_rettes() {
        var data = new MottakMeldingDataWrapper(ptd);
        data.setArkivId(ARKIV_ID);
        data.setSaksnummer(SAKSNUMMER);
        data.setAktørId(AKTØR_ID);
        data.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        data.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_FØDSEL);

        doThrow(new IllegalArgumentException("blab bla")).when(arkivTjeneste).ferdigstillJournalføring(any(), any());

        var wrapper = doTaskWithPrecondition(data);

        assertThat(wrapper).isNotNull();
        assertThat(wrapper.getProsessTaskData().taskType()).as("Forventer at sak med dokumentmangler går til Gosys")
            .isEqualTo(TaskType.forProsessTask(OpprettGSakOppgaveTask.class));
    }

    @Test
    void test_validerDatagrunnlag_uten_feil() {
        var data = new MottakMeldingDataWrapper(ptd);
        data.setSaksnummer("saksnummer");
        data.setAktørId(AKTØR_ID);
        data.setArkivId(ARKIV_ID);

        assertDoesNotThrow(() -> task.precondition(data));
    }

    @Test
    void test_skalVedJournalføringAvDokumentForsendelseFåJournalTilstandEndeligJournalført() {
        var data = new MottakMeldingDataWrapper(ptd);

        when(arkivTjeneste.opprettJournalpost(forsendelseId, AKTØR_ID, SAKSNUMMER)).thenReturn(new OpprettetJournalpost(ARKIV_ID, true));

        data.setForsendelseId(forsendelseId);
        data.setAktørId(AKTØR_ID);
        data.setArkivId(ARKIV_ID);
        data.setBehandlingTema(BehandlingTema.FORELDREPENGER_FØDSEL);
        data.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        data.setSaksnummer(SAKSNUMMER);
        data.setRetryingTask("ABC");

        var next = doTaskWithPrecondition(data);

        assertThat(next.getProsessTaskData().taskType()).isEqualTo(TaskType.forProsessTask(VLKlargjørerTask.class));
    }

    @Test
    void test_skalTilManuellNårJournalTilstandIkkeErEndelig() {
        var data = new MottakMeldingDataWrapper(ptd);

        doThrow(IllegalArgumentException.class).when(arkivTjeneste).ferdigstillJournalføring(ARKIV_ID, "9999");

        data.setForsendelseId(forsendelseId);
        data.setAktørId(AKTØR_ID);
        data.setArkivId(ARKIV_ID);
        data.setBehandlingTema(BehandlingTema.FORELDREPENGER_FØDSEL);
        data.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        data.setSaksnummer(SAKSNUMMER);

        data = doTaskWithPrecondition(data);

        assertThat(data.getProsessTaskData().taskType()).isEqualTo(TaskType.forProsessTask(OpprettGSakOppgaveTask.class));
    }
}
