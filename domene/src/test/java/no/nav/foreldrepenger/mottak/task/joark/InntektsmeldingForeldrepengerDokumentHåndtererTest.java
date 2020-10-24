package no.nav.foreldrepenger.mottak.task.joark;

import static no.nav.foreldrepenger.mottak.task.joark.JoarkTestsupport.AKTØR_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverdi.Tema;
import no.nav.foreldrepenger.mottak.domene.oppgavebehandling.OpprettGSakOppgaveTask;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.journal.ArkivTjeneste;
import no.nav.foreldrepenger.mottak.person.PersonTjeneste;
import no.nav.foreldrepenger.mottak.task.HentOgVurderVLSakTask;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

@ExtendWith(MockitoExtension.class)
public class InntektsmeldingForeldrepengerDokumentHåndtererTest {

    private static final String ARKIV_ID = JoarkTestsupport.ARKIV_ID;

    private ProsessTaskData taskData;
    private HentDataFraJoarkTask joarkTaskTestobjekt;
    private MottakMeldingDataWrapper dataWrapper;
    private JoarkTestsupport joarkTestsupport = new JoarkTestsupport();
    @Mock
    private PersonTjeneste aktørConsumer;
    @Mock
    private ArkivTjeneste arkivTjeneste;

    @BeforeEach
    public void setUp() {
        // initMocks(this);
        ProsessTaskRepository ptr = mock(ProsessTaskRepository.class);
        joarkTaskTestobjekt = spy(new HentDataFraJoarkTask(ptr, aktørConsumer, arkivTjeneste));
        when(aktørConsumer.hentAktørIdForPersonIdent(any())).thenReturn(Optional.of(AKTØR_ID));
        taskData = new ProsessTaskData(HentDataFraJoarkTask.TASKNAME);
        taskData.setSekvens("1");
        dataWrapper = new MottakMeldingDataWrapper(taskData);
        dataWrapper.setArkivId(ARKIV_ID);
        dataWrapper.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
    }

    @Test
    public void skalHåndtereIntekksmeldingForeldrepengerManuellJournalføringDokumentHåndterer() {
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
    public void skalHåndtereIntekksmeldingForeldrepengerElektronikJournalføringDokumentHåndterer() {
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
    public void skalHåndtereInntektsmeldingUtenStartdatoMedManuellJournalføring() {
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
    public void skalHåndtereInntektsmeldingUtenGyldigFNR() {
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
