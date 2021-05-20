package no.nav.foreldrepenger.mottak.task.joark;

import static no.nav.foreldrepenger.mottak.task.joark.JoarkTestsupport.AKTØR_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverdi.Tema;
import no.nav.foreldrepenger.mottak.domene.oppgavebehandling.OpprettGSakOppgaveTask;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.journal.ArkivJournalpost;
import no.nav.foreldrepenger.mottak.journal.ArkivTjeneste;
import no.nav.foreldrepenger.mottak.person.PersonInformasjon;
import no.nav.foreldrepenger.mottak.task.TilJournalføringTask;
import no.nav.foreldrepenger.mottak.tjeneste.Destinasjon;
import no.nav.foreldrepenger.mottak.tjeneste.DestinasjonsRuter;
import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseStatus;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
class ManuellJournalføringDokumentHåndtererTest {

    private static final String DEFAULT_TASK_FOR_MANUELL_JOURNALFØRING = OpprettGSakOppgaveTask.TASKNAME;

    private static final String ARKIV_ID = JoarkTestsupport.ARKIV_ID;

    @Mock
    private ProsessTaskData taskData;
    @Mock
    private ArkivTjeneste arkivTjeneste;
    @Mock
    private PersonInformasjon aktørConsumer;
    @Mock
    private DestinasjonsRuter vurderVLSaker;
    private HentDataFraJoarkTask joarkTaskTestobjekt;
    private MottakMeldingDataWrapper dataWrapper;
    private JoarkTestsupport joarkTestsupport = new JoarkTestsupport();

    @BeforeEach
    void setUp() {
        ProsessTaskRepository ptr = mock(ProsessTaskRepository.class);
        when(aktørConsumer.hentAktørIdForPersonIdent(any())).thenReturn(Optional.of(AKTØR_ID));
        joarkTaskTestobjekt = spy(new HentDataFraJoarkTask(ptr, vurderVLSaker, aktørConsumer, arkivTjeneste));
        taskData = new ProsessTaskData(HentDataFraJoarkTask.TASKNAME);
        taskData.setSekvens("1");
        dataWrapper = new MottakMeldingDataWrapper(taskData);
        dataWrapper.setArkivId(ARKIV_ID);
    }

    @Test
    void skalHåndtereManuellJournalføringAvInntektsmelding() {

        ArkivJournalpost journalMetadata = joarkTestsupport.lagArkivJournalpostStrukturert(DokumentTypeId.INNTEKTSMELDING,
                "testsoknader/inntektsmelding-manual-sample.xml");

        doReturn(journalMetadata).when(arkivTjeneste).hentArkivJournalpost(ARKIV_ID);

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
    void skalHåndtereManuellJournalføringMedGyldigFnr() {
        var metadata = joarkTestsupport.lagJArkivJournalpostUstrukturert(DokumentTypeId.ANNET);
        doReturn(metadata).when(arkivTjeneste).hentArkivJournalpost(ARKIV_ID);
        when(arkivTjeneste.oppdaterRettMangler(any(), any(), any(), any())).thenReturn(true);
        when(vurderVLSaker.bestemDestinasjon(any())).thenReturn(new Destinasjon(ForsendelseStatus.FPSAK, "123"));

        MottakMeldingDataWrapper result = joarkTaskTestobjekt.doTask(dataWrapper);

        assertThat(result.getArkivId()).isEqualTo(ARKIV_ID);
        assertThat(result.getProsessTaskData().getTaskType()).isEqualTo(TilJournalføringTask.TASKNAME);
        assertThat(result.getAktørId().get()).isEqualTo(AKTØR_ID);
    }

    @Test
    void skalHåndtereManuellJournalføringMedUgyldigFnr() {
        var metadata = joarkTestsupport.lagArkivJournalpostUstrukturert(Collections.emptyList());
        doReturn(metadata).when(arkivTjeneste).hentArkivJournalpost(ARKIV_ID);
        when(aktørConsumer.hentAktørIdForPersonIdent(any())).thenReturn(Optional.empty());

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