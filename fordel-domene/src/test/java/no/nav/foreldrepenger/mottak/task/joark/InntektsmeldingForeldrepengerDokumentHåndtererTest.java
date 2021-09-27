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
import no.nav.foreldrepenger.mottak.person.PersonInformasjon;
import no.nav.foreldrepenger.mottak.task.TilJournalføringTask;
import no.nav.foreldrepenger.mottak.tjeneste.Destinasjon;
import no.nav.foreldrepenger.mottak.tjeneste.DestinasjonsRuter;
import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseStatus;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;
import no.nav.vedtak.felles.prosesstask.api.TaskType;

@ExtendWith(MockitoExtension.class)
class InntektsmeldingForeldrepengerDokumentHåndtererTest {

    private static final String ARKIV_ID = JoarkTestsupport.ARKIV_ID;

    private ProsessTaskData taskData;
    private HentDataFraJoarkTask joarkTaskTestobjekt;
    private MottakMeldingDataWrapper dataWrapper;
    private JoarkTestsupport joarkTestsupport = new JoarkTestsupport();
    @Mock
    private PersonInformasjon aktørConsumer;
    @Mock
    private ArkivTjeneste arkivTjeneste;
    @Mock
    private DestinasjonsRuter vurderVLSaker;

    @BeforeEach
    void setUp() {
        var ptr = mock(ProsessTaskTjeneste.class);
        joarkTaskTestobjekt = spy(new HentDataFraJoarkTask(ptr, vurderVLSaker, aktørConsumer, arkivTjeneste));
        when(aktørConsumer.hentAktørIdForPersonIdent(any())).thenReturn(Optional.of(AKTØR_ID));
        taskData = ProsessTaskData.forProsessTask(HentDataFraJoarkTask.class);
        dataWrapper = new MottakMeldingDataWrapper(taskData);
        dataWrapper.setArkivId(ARKIV_ID);
        dataWrapper.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
    }

    @Test
    void skalHåndtereIntekksmeldingForeldrepengerManuellJournalføringDokumentHåndterer() {
        var dokument = joarkTestsupport
                .lagArkivJournalpostStrukturert(DokumentTypeId.INNTEKTSMELDING, "testsoknader/inntektsmelding-manual-sample.xml");

        when(arkivTjeneste.hentArkivJournalpost(ARKIV_ID)).thenReturn(dokument);

        BehandlingTema actualBehandlingTema = BehandlingTema.FORELDREPENGER;
        dataWrapper.setBehandlingTema(actualBehandlingTema);
        dataWrapper.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);

        MottakMeldingDataWrapper wrapper = joarkTaskTestobjekt.doTask(dataWrapper);
        assertThat(wrapper.getAktørId()).hasValue(JoarkTestsupport.AKTØR_ID);
        assertThat(wrapper.getProsessTaskData().taskType()).isEqualTo(TaskType.forProsessTask(OpprettGSakOppgaveTask.class));
    }

    @Test
    void skalHåndtereIntekksmeldingForeldrepengerElektronikJournalføringDokumentHåndterer() {
        var dokument = joarkTestsupport
                .lagArkivJournalpostStrukturert(DokumentTypeId.INNTEKTSMELDING, "testsoknader/inntektsmelding-elektronisk-sample.xml");
        when(arkivTjeneste.hentArkivJournalpost(ARKIV_ID)).thenReturn(dokument);
        when(vurderVLSaker.bestemDestinasjon(any())).thenReturn(new Destinasjon(ForsendelseStatus.FPSAK, "123"));

        BehandlingTema actualBehandlingTema = BehandlingTema.FORELDREPENGER;
        dataWrapper.setBehandlingTema(actualBehandlingTema);
        dataWrapper.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);

        MottakMeldingDataWrapper wrapper = joarkTaskTestobjekt.doTask(dataWrapper);

        assertThat(wrapper.getProsessTaskData().taskType()).isEqualTo(TaskType.forProsessTask(TilJournalføringTask.class));
    }

    @Test
    void skalHåndtereInntektsmeldingUtenStartdatoMedManuellJournalføring() {
        var dokument = joarkTestsupport
                .lagArkivJournalpostStrukturert(DokumentTypeId.INNTEKTSMELDING,
                        "testsoknader/inntektsmelding-manual-uten-startdato-foreldrepenger-periode-sample.xml");

        when(arkivTjeneste.hentArkivJournalpost(ARKIV_ID)).thenReturn(dokument);

        BehandlingTema actualBehandlingTema = BehandlingTema.FORELDREPENGER;
        dataWrapper.setBehandlingTema(actualBehandlingTema);
        dataWrapper.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);

        MottakMeldingDataWrapper wrapper = joarkTaskTestobjekt.doTask(dataWrapper);
        assertThat(wrapper.getAktørId()).hasValue(JoarkTestsupport.AKTØR_ID);
        assertThat(wrapper.getProsessTaskData().taskType()).isEqualTo(TaskType.forProsessTask(OpprettGSakOppgaveTask.class));
    }

    @Test
    void skalHåndtereInntektsmeldingUtenGyldigFNR() {
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
        assertThat(wrapper.getProsessTaskData().taskType()).isEqualTo(TaskType.forProsessTask(OpprettGSakOppgaveTask.class));
    }
}
