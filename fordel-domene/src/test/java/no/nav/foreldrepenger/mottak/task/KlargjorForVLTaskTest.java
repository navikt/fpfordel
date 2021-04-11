package no.nav.foreldrepenger.mottak.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.tjeneste.KlargjørForVLTjeneste;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

@ExtendWith(MockitoExtension.class)
class KlargjorForVLTaskTest {

    private static final String ARKIV_ID = "234567";
    private static final String SAKSNUMMER = "234567";

    private KlargjorForVLTask task;
    private ProsessTaskData ptd;

    @Mock
    private ProsessTaskRepository prosessTaskRepositoryMock;

    @Mock
    private KlargjørForVLTjeneste klargjørForVLTjeneste;

    private UUID forsendelseId;

    @BeforeEach
    void setup() {
        forsendelseId = UUID.randomUUID();
        task = new KlargjorForVLTask(prosessTaskRepositoryMock, klargjørForVLTjeneste);
        ptd = new ProsessTaskData(KlargjorForVLTask.TASKNAME);
        ptd.setSekvens("1");

    }

    @Test
    void test_utfør_mangler_precondition() {
        var data = new MottakMeldingDataWrapper(ptd);
        var fangetFeil = assertThrows(TekniskException.class, () -> toTaskWithPrecondition(data));
        assertThat(fangetFeil.getKode()).isEqualTo("FP-941984");
    }

    @Test
    void test_utfor_klargjor_uten_xml_i_payload() {
        var data = new MottakMeldingDataWrapper(ptd);
        data.setArkivId(ARKIV_ID);
        data.setSaksnummer(SAKSNUMMER);
        data.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_ADOPSJON);
        data.setDokumentTypeId(DokumentTypeId.SØKNAD_ENGANGSSTØNAD_ADOPSJON);
        data.setForsendelseMottattTidspunkt(LocalDateTime.now());
        data.setPayload("pay the load");
        data.setForsendelseId(UUID.randomUUID());
        var neste = toTaskWithPrecondition(data);
        verify(klargjørForVLTjeneste).klargjørForVL(any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
        assertThat(neste).isNotNull();
        assertThat(neste.getProsessTaskData().getTaskType()).isEqualTo(SlettForsendelseTask.TASKNAME);
    }

    @Test
    void test_utfor_klargjor_med_alle_nodvendige_data() {
        var data = new MottakMeldingDataWrapper(ptd);
        data.setArkivId(ARKIV_ID);
        data.setSaksnummer(SAKSNUMMER);
        data.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_ADOPSJON);
        data.setDokumentTypeId(DokumentTypeId.SØKNAD_ENGANGSSTØNAD_ADOPSJON);
        data.setForsendelseMottattTidspunkt(LocalDateTime.now());
        data.setPayload("<xml>test<xml>");
        data.setForsendelseId(UUID.randomUUID());
        var neste = toTaskWithPrecondition(data);
        verify(klargjørForVLTjeneste).klargjørForVL(any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
        assertThat(neste).isNotNull();
        assertThat(neste.getProsessTaskData().getTaskType()).isEqualTo(SlettForsendelseTask.TASKNAME);
    }

    @Test
    void test_oppdater_metadata_hvis_forsendelseId_er_satt() {
        var data = new MottakMeldingDataWrapper(ptd);
        data.setArkivId(ARKIV_ID);
        data.setSaksnummer(SAKSNUMMER);
        data.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_ADOPSJON);
        data.setDokumentTypeId(DokumentTypeId.SØKNAD_ENGANGSSTØNAD_ADOPSJON);
        data.setForsendelseMottattTidspunkt(LocalDateTime.now());
        data.setPayload("<xml>test<xml>");
        data.setForsendelseId(forsendelseId);
        var neste = toTaskWithPrecondition(data);
        verify(klargjørForVLTjeneste).klargjørForVL(any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
        assertThat(neste).isNotNull();
        assertThat(neste.getProsessTaskData().getTaskType()).isEqualTo(SlettForsendelseTask.TASKNAME);
    }

    @Test
    void test_avslutter_hvis_forsendelseId_ikke_satt() {
        var data = new MottakMeldingDataWrapper(ptd);
        data.setArkivId(ARKIV_ID);
        data.setSaksnummer(SAKSNUMMER);
        data.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_ADOPSJON);
        data.setDokumentTypeId(DokumentTypeId.SØKNAD_ENGANGSSTØNAD_ADOPSJON);
        data.setForsendelseMottattTidspunkt(LocalDateTime.now());
        data.setPayload("<xml>test<xml>");
        var neste = toTaskWithPrecondition(data);
        verify(klargjørForVLTjeneste).klargjørForVL(any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
        assertThat(neste).isNull();
    }

    private MottakMeldingDataWrapper toTaskWithPrecondition(MottakMeldingDataWrapper data) {
        task.precondition(data);
        return task.doTask(data);
    }
}
