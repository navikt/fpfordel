package no.nav.foreldrepenger.mottak.task.joark;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
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
import no.nav.foreldrepenger.mottak.journal.ArkivTjeneste;
import no.nav.foreldrepenger.mottak.person.PersonInformasjon;
import no.nav.foreldrepenger.mottak.task.TilJournalføringTask;
import no.nav.foreldrepenger.mottak.tjeneste.Destinasjon;
import no.nav.foreldrepenger.mottak.tjeneste.DestinasjonsRuter;
import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseStatus;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;
import no.nav.vedtak.felles.prosesstask.api.TaskType;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
class HentDataFraJoarkTaskTest {

    private static final String ARKIV_ID = JoarkTestsupport.ARKIV_ID;
    private static final TaskType TASK_TIL_JOURNAL = TaskType.forProsessTask(TilJournalføringTask.class);
    private static final TaskType TASK_GOSYS = TaskType.forProsessTask(OpprettGSakOppgaveTask.class);

    private ProsessTaskData taskData;
    private HentDataFraJoarkTask joarkTaskTestobjekt;
    private MottakMeldingDataWrapper dataWrapper;

    @Mock
    ProsessTaskTjeneste ptr;
    @Mock
    private PersonInformasjon aktørConsumer;
    @Mock
    private ArkivTjeneste arkivTjeneste;
    @Mock
    private DestinasjonsRuter vurderVLSaker;

    private JoarkTestsupport joarkTestsupport = new JoarkTestsupport();

    @BeforeEach
    void setUp() {
        doReturn(Optional.of(JoarkTestsupport.AKTØR_ID)).when(aktørConsumer).hentAktørIdForPersonIdent(any());
        joarkTaskTestobjekt = spy(new HentDataFraJoarkTask(ptr, vurderVLSaker, aktørConsumer, arkivTjeneste));
        taskData = ProsessTaskData.forProsessTask(HentDataFraJoarkTask.class);
        dataWrapper = new MottakMeldingDataWrapper(taskData);
        dataWrapper.setArkivId(ARKIV_ID);
    }

    @Test
    void skal_sende_til_manuell_behandling_ved_tom_dokumentlist() {
        var dokument = joarkTestsupport.lagJArkivJournalpostUstrukturert(DokumentTypeId.UDEFINERT);
        when(arkivTjeneste.hentArkivJournalpost(ARKIV_ID)).thenReturn(dokument);

        BehandlingTema actualBehandlingTema = BehandlingTema.UDEFINERT;
        dataWrapper.setBehandlingTema(actualBehandlingTema);
        dataWrapper.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);

        MottakMeldingDataWrapper resultat = doTaskWithPrecondition(dataWrapper);

        assertThat(resultat.getProsessTaskData().taskType()).isEqualTo(TASK_GOSYS);
    }

    @Test
    void skal_sende_til_manuell_behandling_ved_manglende_bruker() {
        var dokument = joarkTestsupport
                .lagArkivJournalpostUstrukturert(Collections.emptyList());
        when(arkivTjeneste.hentArkivJournalpost(ARKIV_ID)).thenReturn(dokument);

        BehandlingTema actualBehandlingTema = BehandlingTema.ENGANGSSTØNAD;
        dataWrapper.setBehandlingTema(actualBehandlingTema);
        dataWrapper.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        dataWrapper.setAktørId(null);

        MottakMeldingDataWrapper resultat = doTaskWithPrecondition(dataWrapper);

        assertThat(resultat.getProsessTaskData().taskType()).isEqualTo(TASK_GOSYS);
    }

    @Test
    void skal_sende_til_vl_es_fødsel() {
        dataWrapper.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_FØDSEL);
        dataWrapper.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        var dokument = joarkTestsupport
                .lagJArkivJournalpostUstrukturert();
        when(arkivTjeneste.hentArkivJournalpost(ARKIV_ID)).thenReturn(dokument);
        when(arkivTjeneste.oppdaterRettMangler(any(), any(), any(), any())).thenReturn(true);
        when(vurderVLSaker.bestemDestinasjon(any())).thenReturn(Destinasjon.FPSAK_UTEN_SAK);
        when(vurderVLSaker.opprettSak(any())).thenReturn("456");
        when(vurderVLSaker.kanOppretteSak(any())).thenReturn(true);

        MottakMeldingDataWrapper resultat = doTaskWithPrecondition(dataWrapper);

        assertThat(resultat.getProsessTaskData().taskType()).isEqualTo(TASK_TIL_JOURNAL);
    }

    @Test
    void skal_sende_til_vl_es_adopsjon() {
        dataWrapper.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_ADOPSJON);
        dataWrapper.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        var dokument = joarkTestsupport
                .lagJArkivJournalpostUstrukturert();
        when(arkivTjeneste.hentArkivJournalpost(ARKIV_ID)).thenReturn(dokument);
        when(arkivTjeneste.oppdaterRettMangler(any(), any(), any(), any())).thenReturn(true);
        when(vurderVLSaker.bestemDestinasjon(any())).thenReturn(Destinasjon.FPSAK_UTEN_SAK);
        when(vurderVLSaker.opprettSak(any())).thenReturn("789");
        when(vurderVLSaker.kanOppretteSak(any())).thenReturn(true);

        MottakMeldingDataWrapper resultat = doTaskWithPrecondition(dataWrapper);

        assertThat(resultat.getProsessTaskData().taskType()).isEqualTo(TASK_TIL_JOURNAL);
    }

    @Test
    void skal_sende_til_vl_fp_im_2019() {
        dataWrapper.setBehandlingTema(BehandlingTema.FORELDREPENGER);
        dataWrapper.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        var dokument = joarkTestsupport
                .lagArkivJournalpostStrukturert(DokumentTypeId.INNTEKTSMELDING,
                        "testsoknader/inntektsmelding-elektronisk-sample.xml");
        when(arkivTjeneste.hentArkivJournalpost(ARKIV_ID)).thenReturn(dokument);
        when(vurderVLSaker.bestemDestinasjon(any())).thenReturn(new Destinasjon(ForsendelseStatus.FPSAK, "123"));

        MottakMeldingDataWrapper resultat = doTaskWithPrecondition(dataWrapper);

        assertThat(resultat.getProsessTaskData().taskType()).isEqualTo(TASK_TIL_JOURNAL);
    }

    @Test
    void skal_sende_til_manuell_fp_im_2018() {
        dataWrapper.setBehandlingTema(BehandlingTema.FORELDREPENGER);
        dataWrapper.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        var dokument = joarkTestsupport
                .lagArkivJournalpostStrukturert(DokumentTypeId.INNTEKTSMELDING,
                        "testsoknader/inntektsmelding-manual-sample.xml");
        when(arkivTjeneste.hentArkivJournalpost(ARKIV_ID)).thenReturn(dokument);

        MottakMeldingDataWrapper resultat = doTaskWithPrecondition(dataWrapper);

        assertThat(resultat.getProsessTaskData().taskType()).isEqualTo(TASK_GOSYS);
    }

    @Test
    void skal_sende_til_manuell_behandling_hvis_behandlingstema_er_undefinert() {
        var dokument = joarkTestsupport
                .lagJArkivJournalpostUstrukturert(DokumentTypeId.UDEFINERT);
        when(arkivTjeneste.hentArkivJournalpost(ARKIV_ID)).thenReturn(dokument);

        dataWrapper.setBehandlingTema(BehandlingTema.UDEFINERT);
        dataWrapper.setTema(Tema.UDEFINERT);
        MottakMeldingDataWrapper resultat = doTaskWithPrecondition(dataWrapper);

        assertThat(resultat.getProsessTaskData().taskType()).isEqualTo(TASK_GOSYS);
    }

    @Test
    void skal_sende_til_vl_hvis_dokmenttype_kan_håndteres() {
        var dokument = joarkTestsupport.lagJArkivJournalpostUstrukturert();
        when(arkivTjeneste.hentArkivJournalpost(ARKIV_ID)).thenReturn(dokument);
        when(arkivTjeneste.oppdaterRettMangler(any(), any(), any(), any())).thenReturn(true);
        when(vurderVLSaker.bestemDestinasjon(any())).thenReturn(new Destinasjon(ForsendelseStatus.FPSAK, "123"));
        dataWrapper.setBehandlingTema(BehandlingTema.UDEFINERT);
        dataWrapper.setTema(Tema.UDEFINERT);
        MottakMeldingDataWrapper resultat = doTaskWithPrecondition(dataWrapper);

        assertThat(resultat.getProsessTaskData().taskType()).isEqualTo(TASK_TIL_JOURNAL);
    }

    @Test
    void skal_sende_klage_til_sjekk_vl() {
        dataWrapper.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        dataWrapper.setBehandlingTema(BehandlingTema.UDEFINERT);
        var dokument = joarkTestsupport.lagJArkivJournalpostKlage();
        when(arkivTjeneste.hentArkivJournalpost(ARKIV_ID)).thenReturn(dokument);
        when(vurderVLSaker.bestemDestinasjon(any())).thenReturn(Destinasjon.GOSYS);
        when(vurderVLSaker.kanOppretteSak(any())).thenReturn(false);

        MottakMeldingDataWrapper resultat = doTaskWithPrecondition(dataWrapper);

        assertThat(resultat.getProsessTaskData().taskType()).isEqualTo(TASK_GOSYS);
    }

    @Test
    void skal_sende_inntektsmelding_for_far_til_sjekk_vl() {
        dataWrapper.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        dataWrapper.setBehandlingTema(BehandlingTema.FORELDREPENGER);
        var dokument = joarkTestsupport
                .lagArkivJournalpostStrukturert(DokumentTypeId.INNTEKTSMELDING, "testsoknader/inntektsmelding-far.xml");
        when(arkivTjeneste.hentArkivJournalpost(ARKIV_ID)).thenReturn(dokument);
        when(vurderVLSaker.bestemDestinasjon(any())).thenReturn(Destinasjon.GOSYS);

        MottakMeldingDataWrapper resultat = doTaskWithPrecondition(dataWrapper);

        assertThat(resultat.getProsessTaskData().taskType()).isEqualTo(TASK_GOSYS);
    }

    @Test
    public void skal_sende_inntektsmelding_til_vl_hvis_gjelder_svangerskapspenger() {
        dataWrapper.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        dataWrapper.setBehandlingTema(BehandlingTema.SVANGERSKAPSPENGER);
        var dokument = joarkTestsupport
                .lagArkivJournalpostStrukturert(DokumentTypeId.INNTEKTSMELDING, "testsoknader/inntektsmelding-svp.xml");
        doReturn(Optional.of(JoarkTestsupport.BRUKER_FNR)).when(aktørConsumer)
                .hentPersonIdentForAktørId(eq(JoarkTestsupport.AKTØR_ID));
        when(arkivTjeneste.hentArkivJournalpost(ARKIV_ID)).thenReturn(dokument);
        when(vurderVLSaker.bestemDestinasjon(any())).thenReturn(Destinasjon.FPSAK_UTEN_SAK);
        when(vurderVLSaker.opprettSak(any())).thenReturn("123");
        when(vurderVLSaker.kanOppretteSak(any())).thenReturn(true);

        MottakMeldingDataWrapper resultat = doTaskWithPrecondition(dataWrapper);

        assertThat(resultat.getProsessTaskData().taskType()).isEqualTo(TASK_TIL_JOURNAL);
    }

    @Test
    void skal_sende_inntektsmelding_for_far_om_svangerskapspenger_til_manuell_behandling() {
        dataWrapper.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        dataWrapper.setBehandlingTema(BehandlingTema.SVANGERSKAPSPENGER);
        var dokument = joarkTestsupport
                .lagArkivJournalpostStrukturert(DokumentTypeId.INNTEKTSMELDING, "testsoknader/inntektsmelding-far-svp.xml");
        String fnrPåInntektsmelding = "99999999999";
        doReturn(Optional.of(fnrPåInntektsmelding)).when(aktørConsumer)
                .hentPersonIdentForAktørId(eq(JoarkTestsupport.AKTØR_ID));
        when(arkivTjeneste.hentArkivJournalpost(ARKIV_ID)).thenReturn(dokument);

        MottakMeldingDataWrapper resultat = doTaskWithPrecondition(dataWrapper);

        assertThat(resultat.getProsessTaskData().taskType()).isEqualTo(TASK_GOSYS);
    }

    @Test
    void skal_sende_til_manuell_behandling_ved_presatt_saksnummer_ikke_vl() {
        var dokument = joarkTestsupport.lagJArkivJournalpostKlageMedSaksnummer("INFOTRYGD");
        when(arkivTjeneste.hentArkivJournalpost(ARKIV_ID)).thenReturn(dokument);
        when(vurderVLSaker.bestemDestinasjon(dataWrapper)).thenReturn(Destinasjon.GOSYS);

        BehandlingTema actualBehandlingTema = BehandlingTema.UDEFINERT;
        dataWrapper.setBehandlingTema(actualBehandlingTema);
        dataWrapper.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        dataWrapper.setInnkommendeSaksnummer("VL");
        dataWrapper.setSaksnummer("VL");


        MottakMeldingDataWrapper resultat = doTaskWithPrecondition(dataWrapper);

        assertThat(resultat.getProsessTaskData().taskType()).isEqualTo(TASK_GOSYS);
    }

    @Test
    void skal_sende_til_manuell_behandling_ved_presatt_saksnummer_vl() {
        var dokument = joarkTestsupport.lagJArkivJournalpostKlageMedSaksnummer("VL");
        when(arkivTjeneste.hentArkivJournalpost(ARKIV_ID)).thenReturn(dokument);
        when(arkivTjeneste.oppdaterRettMangler(any(),any(),any(),any())).thenReturn(true);
        when(vurderVLSaker.bestemDestinasjon(any())).thenReturn(new Destinasjon(ForsendelseStatus.FPSAK, "VL"));

        BehandlingTema actualBehandlingTema = BehandlingTema.UDEFINERT;
        dataWrapper.setInnkommendeSaksnummer("VL");
        dataWrapper.setSaksnummer("VL");
        dataWrapper.setBehandlingTema(actualBehandlingTema);
        dataWrapper.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);

        MottakMeldingDataWrapper resultat = doTaskWithPrecondition(dataWrapper);

        assertThat(resultat.getProsessTaskData().taskType()).isEqualTo(TASK_TIL_JOURNAL);
    }

    @Test
    void test_validerDatagrunnlag_skal_feile_ved_manglende_arkiv_id() {
        dataWrapper.setArkivId("");
        assertThrows(TekniskException.class, () -> doTaskWithPrecondition(dataWrapper));
    }

    @Test
    void test_validerDatagrunnlag_uten_feil() {
        dataWrapper.setArkivId("123456");
        joarkTaskTestobjekt.precondition(dataWrapper);
    }

    @Test
    void test_post_condition_skal_kaste_feilmelding_når_aktørId_mangler() {
        dataWrapper.setAktørId(null);
        var e = assertThrows(TekniskException.class, () -> joarkTaskTestobjekt.postcondition(dataWrapper));
        assertTrue(e.getMessage().contains("FP-638068"));
    }

    private MottakMeldingDataWrapper doTaskWithPrecondition(MottakMeldingDataWrapper data) {
        joarkTaskTestobjekt.precondition(data);
        return joarkTaskTestobjekt.doTask(data);
    }
}
