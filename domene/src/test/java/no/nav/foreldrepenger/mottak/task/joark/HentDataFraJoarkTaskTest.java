package no.nav.foreldrepenger.mottak.task.joark;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverdi.Tema;
import no.nav.foreldrepenger.mottak.domene.oppgavebehandling.OpprettGSakOppgaveTask;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.journal.ArkivTjeneste;
import no.nav.foreldrepenger.mottak.task.HentOgVurderVLSakTask;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumerMedCache;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

@RunWith(MockitoJUnitRunner.class)
public class HentDataFraJoarkTaskTest {

    private static final String ARKIV_ID = JoarkTestsupport.ARKIV_ID;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private ProsessTaskData taskData;
    private HentDataFraJoarkTask joarkTaskTestobjekt;
    private MottakMeldingDataWrapper dataWrapper;

    @Mock
    private AktørConsumerMedCache aktørConsumer;
    @Mock
    private ArkivTjeneste arkivTjeneste;

    private JoarkTestsupport joarkTestsupport = new JoarkTestsupport();

    @Before
    public void setUp() throws Exception {
        initMocks(this);

        ProsessTaskRepository ptr = mock(ProsessTaskRepository.class);
        doReturn(Optional.of(JoarkTestsupport.AKTØR_ID)).when(aktørConsumer).hentAktørIdForPersonIdent(any());
        joarkTaskTestobjekt = spy(new HentDataFraJoarkTask(ptr, aktørConsumer, arkivTjeneste));

        taskData = new ProsessTaskData(HentDataFraJoarkTask.TASKNAME);
        taskData.setSekvens("1");
        dataWrapper = new MottakMeldingDataWrapper(taskData);
        dataWrapper.setArkivId(ARKIV_ID);
    }

    @Test
    public void skal_sende_til_manuell_behandling_ved_tom_dokumentlist() throws Exception {
        var dokument = joarkTestsupport.lagJArkivJournalpostUstrukturert(DokumentTypeId.UDEFINERT);
        when(arkivTjeneste.hentArkivJournalpost(ARKIV_ID)).thenReturn(dokument);

        BehandlingTema actualBehandlingTema = BehandlingTema.UDEFINERT;
        dataWrapper.setBehandlingTema(actualBehandlingTema);
        dataWrapper.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);

        MottakMeldingDataWrapper resultat = doTaskWithPrecondition(dataWrapper);

        assertThat(resultat.getProsessTaskData().getTaskType()).isEqualTo(OpprettGSakOppgaveTask.TASKNAME);
    }

    @Test
    public void skal_sende_til_manuell_behandling_ved_manglende_bruker() throws Exception {
        var dokument = joarkTestsupport
                .lagArkivJournalpostUstrukturert(Collections.emptyList());
        when(arkivTjeneste.hentArkivJournalpost(ARKIV_ID)).thenReturn(dokument);

        BehandlingTema actualBehandlingTema = BehandlingTema.ENGANGSSTØNAD;
        dataWrapper.setBehandlingTema(actualBehandlingTema);
        dataWrapper.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        dataWrapper.setAktørId(null);

        MottakMeldingDataWrapper resultat = doTaskWithPrecondition(dataWrapper);

        assertThat(resultat.getProsessTaskData().getTaskType()).isEqualTo(OpprettGSakOppgaveTask.TASKNAME);
    }

    @Test
    public void skal_sende_til_vl_es_fødsel() throws Exception {
        dataWrapper.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_FØDSEL);
        dataWrapper.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        var dokument = joarkTestsupport
                .lagJArkivJournalpostUstrukturert();
        when(arkivTjeneste.hentArkivJournalpost(ARKIV_ID)).thenReturn(dokument);
        when(arkivTjeneste.oppdaterRettMangler(any(), any(), any(), any())).thenReturn(true);

        MottakMeldingDataWrapper resultat = doTaskWithPrecondition(dataWrapper);

        assertThat(resultat.getProsessTaskData().getTaskType()).isEqualTo(HentOgVurderVLSakTask.TASKNAME);
    }

    @Test
    public void skal_sende_til_vl_es_adopsjon() throws Exception {
        dataWrapper.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_ADOPSJON);
        dataWrapper.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        var dokument = joarkTestsupport
                .lagJArkivJournalpostUstrukturert();
        when(arkivTjeneste.hentArkivJournalpost(ARKIV_ID)).thenReturn(dokument);
        when(arkivTjeneste.oppdaterRettMangler(any(), any(), any(), any())).thenReturn(true);
        MottakMeldingDataWrapper resultat = doTaskWithPrecondition(dataWrapper);

        assertThat(resultat.getProsessTaskData().getTaskType()).isEqualTo(HentOgVurderVLSakTask.TASKNAME);
    }

    @Test
    public void skal_sende_til_vl_fp_im_2019() throws Exception {
        dataWrapper.setBehandlingTema(BehandlingTema.FORELDREPENGER);
        dataWrapper.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        var dokument = joarkTestsupport
                .lagArkivJournalpostStrukturert(DokumentTypeId.INNTEKTSMELDING,
                        "testsoknader/inntektsmelding-elektronisk-sample.xml");
        when(arkivTjeneste.hentArkivJournalpost(ARKIV_ID)).thenReturn(dokument);

        MottakMeldingDataWrapper resultat = doTaskWithPrecondition(dataWrapper);

        assertThat(resultat.getProsessTaskData().getTaskType()).isEqualTo(HentOgVurderVLSakTask.TASKNAME);
    }

    @Test
    public void skal_sende_til_manuell_fp_im_2018() throws Exception {
        dataWrapper.setBehandlingTema(BehandlingTema.FORELDREPENGER);
        dataWrapper.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        var dokument = joarkTestsupport
                .lagArkivJournalpostStrukturert(DokumentTypeId.INNTEKTSMELDING,
                        "testsoknader/inntektsmelding-manual-sample.xml");
        when(arkivTjeneste.hentArkivJournalpost(ARKIV_ID)).thenReturn(dokument);

        MottakMeldingDataWrapper resultat = doTaskWithPrecondition(dataWrapper);

        assertThat(resultat.getProsessTaskData().getTaskType()).isEqualTo(OpprettGSakOppgaveTask.TASKNAME);
    }

    @Test
    public void skal_sende_til_manuell_behandling_hvis_behandlingstema_er_undefinert() throws Exception {
        var dokument = joarkTestsupport
                .lagJArkivJournalpostUstrukturert(DokumentTypeId.UDEFINERT);
        when(arkivTjeneste.hentArkivJournalpost(ARKIV_ID)).thenReturn(dokument);

        dataWrapper.setBehandlingTema(BehandlingTema.UDEFINERT);
        dataWrapper.setTema(Tema.UDEFINERT);
        MottakMeldingDataWrapper resultat = doTaskWithPrecondition(dataWrapper);

        assertThat(resultat.getProsessTaskData().getTaskType()).isEqualTo(OpprettGSakOppgaveTask.TASKNAME);
    }

    @Test
    public void skal_sende_til_vl_hvis_dokmenttype_kan_håndteres() throws Exception {
        var dokument = joarkTestsupport.lagJArkivJournalpostUstrukturert();
        when(arkivTjeneste.hentArkivJournalpost(ARKIV_ID)).thenReturn(dokument);
        when(arkivTjeneste.oppdaterRettMangler(any(), any(), any(), any())).thenReturn(true);
        dataWrapper.setBehandlingTema(BehandlingTema.UDEFINERT);
        dataWrapper.setTema(Tema.UDEFINERT);
        MottakMeldingDataWrapper resultat = doTaskWithPrecondition(dataWrapper);

        assertThat(resultat.getProsessTaskData().getTaskType()).isEqualTo(HentOgVurderVLSakTask.TASKNAME);
    }

    @Test
    public void skal_sende_inntektsmelding_for_far_til_sjekk_vl() throws Exception {
        dataWrapper.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        dataWrapper.setBehandlingTema(BehandlingTema.FORELDREPENGER);
        var dokument = joarkTestsupport
                .lagArkivJournalpostStrukturert(DokumentTypeId.INNTEKTSMELDING, "testsoknader/inntektsmelding-far.xml");
        when(arkivTjeneste.hentArkivJournalpost(ARKIV_ID)).thenReturn(dokument);

        MottakMeldingDataWrapper resultat = doTaskWithPrecondition(dataWrapper);

        assertThat(resultat.getProsessTaskData().getTaskType()).isEqualTo(HentOgVurderVLSakTask.TASKNAME);
    }

    @Test
    public void skal_sende_inntektsmelding_til_vl_hvis_gjelder_svangerskapspenger() throws Exception {

        dataWrapper.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        dataWrapper.setBehandlingTema(BehandlingTema.SVANGERSKAPSPENGER);
        var dokument = joarkTestsupport
                .lagArkivJournalpostStrukturert(DokumentTypeId.INNTEKTSMELDING, "testsoknader/inntektsmelding-svp.xml");
        doReturn(Optional.of(JoarkTestsupport.BRUKER_FNR)).when(aktørConsumer)
                .hentPersonIdentForAktørId(eq(JoarkTestsupport.AKTØR_ID));
        when(arkivTjeneste.hentArkivJournalpost(ARKIV_ID)).thenReturn(dokument);

        MottakMeldingDataWrapper resultat = doTaskWithPrecondition(dataWrapper);

        assertThat(resultat.getProsessTaskData().getTaskType()).isEqualTo(HentOgVurderVLSakTask.TASKNAME);
    }

    @Test
    public void skal_sende_inntektsmelding_for_far_om_svangerskapspenger_til_manuell_behandling() throws Exception {
        dataWrapper.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        dataWrapper.setBehandlingTema(BehandlingTema.SVANGERSKAPSPENGER);
        var dokument = joarkTestsupport
                .lagArkivJournalpostStrukturert(DokumentTypeId.INNTEKTSMELDING, "testsoknader/inntektsmelding-far-svp.xml");
        String fnrPåInntektsmelding = "99999999999";
        doReturn(Optional.of(fnrPåInntektsmelding)).when(aktørConsumer)
                .hentPersonIdentForAktørId(eq(JoarkTestsupport.AKTØR_ID));
        when(arkivTjeneste.hentArkivJournalpost(ARKIV_ID)).thenReturn(dokument);

        MottakMeldingDataWrapper resultat = doTaskWithPrecondition(dataWrapper);

        assertThat(resultat.getProsessTaskData().getTaskType()).isEqualTo(OpprettGSakOppgaveTask.TASKNAME);
    }

    @Test
    public void test_validerDatagrunnlag_skal_feile_ved_manglende_arkiv_id() throws Exception {
        dataWrapper.setArkivId("");
        expectedException.expect(TekniskException.class);
        doTaskWithPrecondition(dataWrapper);
    }

    @Test
    public void test_validerDatagrunnlag_uten_feil() throws Exception {
        dataWrapper.setArkivId("123456");
        joarkTaskTestobjekt.precondition(dataWrapper);
    }

    @Test
    public void test_post_condition_skal_kaste_feilmelding_når_aktørId_mangler() {
        dataWrapper.setAktørId(null);

        expectedException.expect(TekniskException.class);
        expectedException.expectMessage("FP-638068");

        joarkTaskTestobjekt.postcondition(dataWrapper);
    }

    private MottakMeldingDataWrapper doTaskWithPrecondition(MottakMeldingDataWrapper data) {
        joarkTaskTestobjekt.precondition(data);
        return joarkTaskTestobjekt.doTask(data);
    }
}
