package no.nav.foreldrepenger.mottak.task.joark;

import static no.nav.foreldrepenger.mottak.task.joark.JoarkTestsupport.AKTØR_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverdi.Tema;
import no.nav.foreldrepenger.mottak.domene.oppgavebehandling.OpprettGSakOppgaveTask;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.journal.ArkivTjeneste;
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
    private JoarkTestsupport joarkTestsupport = new JoarkTestsupport();
    private AktørConsumerMedCache aktørConsumer;
    private ArkivTjeneste arkivTjeneste;

    @Before
    public void setUp() throws Exception {
        initMocks(this);

        ProsessTaskRepository ptr = mock(ProsessTaskRepository.class);
        aktørConsumer = mock(AktørConsumerMedCache.class);
        arkivTjeneste = mock(ArkivTjeneste.class);
        joarkTaskTestobjekt = spy(new HentDataFraJoarkTask(ptr, aktørConsumer, arkivTjeneste));
        when(aktørConsumer.hentAktørIdForPersonIdent(any())).thenReturn(Optional.of(AKTØR_ID));
        taskData = new ProsessTaskData(HentDataFraJoarkTask.TASKNAME);
        taskData.setSekvens("1");
        dataWrapper = new MottakMeldingDataWrapper(taskData);
        dataWrapper.setArkivId(ARKIV_ID);
        dataWrapper.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
    }

    @Test
    public void skalHåndtereIntekksmeldingForeldrepengerManuellJournalføringDokumentHåndterer() throws Exception {
        var dokument = joarkTestsupport
                .lagArkivJournalpostStrukturert(DokumentTypeId.INNTEKTSMELDING, "testsoknader/inntektsmelding-manual-sample.xml");

        when(arkivTjeneste.hentArkivJournalpost(ARKIV_ID)).thenReturn(dokument);

        BehandlingTema actualBehandlingTema = BehandlingTema.FORELDREPENGER;
        dataWrapper.setBehandlingTema(actualBehandlingTema);
        dataWrapper.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);

        MottakMeldingDataWrapper wrapper = joarkTaskTestobjekt.doTask(dataWrapper);
        assertThat(wrapper.getAktørId()).hasValue(JoarkTestsupport.AKTØR_ID);
        assertThat(wrapper.getProsessTaskData().getTaskType()).isEqualTo(OpprettGSakOppgaveTask.TASKNAME);
    }

    @Test
    public void skalHåndtereIntekksmeldingForeldrepengerElektronikJournalføringDokumentHåndterer() throws Exception {
        var dokument = joarkTestsupport
                .lagArkivJournalpostStrukturert(DokumentTypeId.INNTEKTSMELDING, "testsoknader/inntektsmelding-elektronisk-sample.xml");
        when(arkivTjeneste.hentArkivJournalpost(ARKIV_ID)).thenReturn(dokument);

        BehandlingTema actualBehandlingTema = BehandlingTema.FORELDREPENGER;
        dataWrapper.setBehandlingTema(actualBehandlingTema);
        dataWrapper.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);

        MottakMeldingDataWrapper wrapper = joarkTaskTestobjekt.doTask(dataWrapper);

        assertThat(wrapper.getProsessTaskData().getTaskType()).isEqualTo(HentOgVurderVLSakTask.TASKNAME);
    }

    @Test
    public void skalHåndtereInntektsmeldingUtenStartdatoMedManuellJournalføring() throws Exception {
        var dokument = joarkTestsupport
                .lagArkivJournalpostStrukturert(DokumentTypeId.INNTEKTSMELDING,
                        "testsoknader/inntektsmelding-manual-uten-startdato-foreldrepenger-periode-sample.xml");

        when(arkivTjeneste.hentArkivJournalpost(ARKIV_ID)).thenReturn(dokument);

        BehandlingTema actualBehandlingTema = BehandlingTema.FORELDREPENGER;
        dataWrapper.setBehandlingTema(actualBehandlingTema);
        dataWrapper.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);

        MottakMeldingDataWrapper wrapper = joarkTaskTestobjekt.doTask(dataWrapper);
        assertThat(wrapper.getAktørId()).hasValue(JoarkTestsupport.AKTØR_ID);
        assertThat(wrapper.getProsessTaskData().getTaskType()).isEqualTo(OpprettGSakOppgaveTask.TASKNAME);
    }

    @Test
    public void skalHåndtereInntektsmeldingUtenGyldigFNR() throws Exception {
        var dokument = joarkTestsupport
                .lagArkivJournalpostStrukturert(DokumentTypeId.INNTEKTSMELDING,
                        "testsoknader/inntektsmelding-manual-uten-startdato-foreldrepenger-periode-sample.xml");
        when(arkivTjeneste.hentArkivJournalpost(ARKIV_ID)).thenReturn(dokument);

        when(aktørConsumer.hentAktørIdForPersonIdent(any())).thenReturn(Optional.empty());

        BehandlingTema actualBehandlingTema = BehandlingTema.FORELDREPENGER;
        dataWrapper.setBehandlingTema(actualBehandlingTema);
        dataWrapper.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);

        MottakMeldingDataWrapper wrapper = joarkTaskTestobjekt.doTask(dataWrapper);
        assertThat(wrapper.getAktørId()).isEmpty();
        assertThat(wrapper.getProsessTaskData().getTaskType()).isEqualTo(OpprettGSakOppgaveTask.TASKNAME);
    }
}
