package no.nav.foreldrepenger.mottak.task.joark;

import static no.nav.foreldrepenger.mottak.task.joark.JoarkTestsupport.AKTØR_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverdi.Tema;
import no.nav.foreldrepenger.mottak.domene.oppgavebehandling.OpprettGSakOppgaveTask;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.journal.ArkivJournalpost;
import no.nav.foreldrepenger.mottak.journal.ArkivTjeneste;
import no.nav.foreldrepenger.mottak.task.HentOgVurderVLSakTask;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumerMedCache;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

public class ManuellJournalføringDokumentHåndtererTest {

    private static final String DEFAULT_TASK_FOR_MANUELL_JOURNALFØRING = OpprettGSakOppgaveTask.TASKNAME;

    private static final String ARKIV_ID = JoarkTestsupport.ARKIV_ID;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private ProsessTaskData taskData;
    private ArkivTjeneste arkivTjeneste;
    private AktørConsumerMedCache aktørConsumer;
    private HentDataFraJoarkTask joarkTaskTestobjekt;
    private MottakMeldingDataWrapper dataWrapper;
    private JoarkTestsupport joarkTestsupport = new JoarkTestsupport();

    @Before
    public void setUp() throws Exception {
        initMocks(this);

        ProsessTaskRepository ptr = mock(ProsessTaskRepository.class);
        arkivTjeneste = mock(ArkivTjeneste.class);
        aktørConsumer = mock(AktørConsumerMedCache.class);
        joarkTaskTestobjekt = spy(new HentDataFraJoarkTask(ptr, aktørConsumer, arkivTjeneste));
        when(aktørConsumer.hentAktørIdForPersonIdent(any())).thenReturn(Optional.of(AKTØR_ID));
        when(aktørConsumer.hentAktørIdForPersonIdent(any())).thenReturn(Optional.of(AKTØR_ID));
        taskData = new ProsessTaskData(HentDataFraJoarkTask.TASKNAME);
        taskData.setSekvens("1");
        dataWrapper = new MottakMeldingDataWrapper(taskData);
        dataWrapper.setArkivId(ARKIV_ID);
    }

    @Test
    public void skalHåndtereManuellJournalføringAvInntektsmelding() throws Exception {

        ArkivJournalpost journalMetadata = joarkTestsupport.lagArkivJournalpostStrukturert(DokumentTypeId.INNTEKTSMELDING, "testsoknader/inntektsmelding-manual-sample.xml");

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
    public void skalHåndtereManuellJournalføringMedGyldigFnr() throws Exception {
        var metadata = joarkTestsupport.lagJArkivJournalpostUstrukturert(DokumentTypeId.ANNET);
        doReturn(metadata).when(arkivTjeneste).hentArkivJournalpost(ARKIV_ID);

        MottakMeldingDataWrapper result = joarkTaskTestobjekt.doTask(dataWrapper);

        assertThat(result.getArkivId()).isEqualTo(ARKIV_ID);
        assertThat(result.getProsessTaskData().getTaskType()).isEqualTo(HentOgVurderVLSakTask.TASKNAME);
        assertThat(result.getAktørId().get()).isEqualTo(AKTØR_ID);
    }

    @Test
    public void skalHåndtereManuellJournalføringMedUgyldigFnr() throws Exception {
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