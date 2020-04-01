package no.nav.foreldrepenger.mottak.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentRepository;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.tjeneste.KlargjørForVLTjeneste;
import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseStatus;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

public class KlargjorForVLTaskTest {


    private static final String ARKIV_ID = "234567";
    private static final String SAKSNUMMER = "234567";

    private KlargjorForVLTask task;
    private ProsessTaskData ptd;

    @Mock
    private ProsessTaskRepository prosessTaskRepositoryMock;

    @Mock
    private KlargjørForVLTjeneste klargjørForVLTjeneste;

    @Mock
    private DokumentRepository dokumentRepository;

    private UUID forsendelseId;

    @Before
    public void setup() {
        prosessTaskRepositoryMock = mock(ProsessTaskRepository.class);
        klargjørForVLTjeneste = mock(KlargjørForVLTjeneste.class);
        dokumentRepository = mock(DokumentRepository.class);
        forsendelseId = UUID.randomUUID();
        task = new KlargjorForVLTask(prosessTaskRepositoryMock, klargjørForVLTjeneste, dokumentRepository);
        ptd = new ProsessTaskData(KlargjorForVLTask.TASKNAME);
        ptd.setSekvens("1");

    }

    @Test
    public void test_utfør_mangler_precondition() {
        MottakMeldingDataWrapper data = new MottakMeldingDataWrapper(ptd);
        Exception fangetFeil = null;
        try {
            toTaskWithPrecondition(data);
        } catch (Exception ex) {
            fangetFeil = ex;
        }
        assertThat(fangetFeil).isInstanceOf(TekniskException.class);
        assertThat(((TekniskException) fangetFeil).getFeil().getKode()).isEqualTo("FP-941984");
    }

    @Test
    public void test_utfor_klargjor_uten_xml_i_payload() {

        MottakMeldingDataWrapper data = new MottakMeldingDataWrapper(ptd);
        data.setArkivId(ARKIV_ID);
        data.setSaksnummer(SAKSNUMMER);
        data.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_ADOPSJON);
        data.setDokumentTypeId(DokumentTypeId.SØKNAD_ENGANGSSTØNAD_ADOPSJON);
        data.setForsendelseMottattTidspunkt(LocalDateTime.now());
        data.setPayload("pay the load");
        data.setForsendelseId(UUID.randomUUID());

        MottakMeldingDataWrapper neste = toTaskWithPrecondition(data);

        verify(klargjørForVLTjeneste).klargjørForVL(any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
        assertThat(neste).isNotNull();
        assertThat(neste.getProsessTaskData().getTaskType()).isEqualTo(SlettForsendelseTask.TASKNAME);
    }

    @Test
    public void test_utfor_klargjor_med_alle_nodvendige_data() {
        MottakMeldingDataWrapper data = new MottakMeldingDataWrapper(ptd);
        data.setArkivId(ARKIV_ID);
        data.setSaksnummer(SAKSNUMMER);
        data.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_ADOPSJON);
        data.setDokumentTypeId(DokumentTypeId.SØKNAD_ENGANGSSTØNAD_ADOPSJON);
        data.setForsendelseMottattTidspunkt(LocalDateTime.now());
        data.setPayload("<xml>test<xml>");
        data.setForsendelseId(UUID.randomUUID());

        MottakMeldingDataWrapper neste = toTaskWithPrecondition(data);

        verify(klargjørForVLTjeneste).klargjørForVL(any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
        assertThat(neste).isNotNull();
        assertThat(neste.getProsessTaskData().getTaskType()).isEqualTo(SlettForsendelseTask.TASKNAME);
    }

    @Test
    public void test_oppdater_metadata_hvis_forsendelseId_er_satt() {
        MottakMeldingDataWrapper data = new MottakMeldingDataWrapper(ptd);
        data.setArkivId(ARKIV_ID);
        data.setSaksnummer(SAKSNUMMER);
        data.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_ADOPSJON);
        data.setDokumentTypeId(DokumentTypeId.SØKNAD_ENGANGSSTØNAD_ADOPSJON);
        data.setForsendelseMottattTidspunkt(LocalDateTime.now());
        data.setPayload("<xml>test<xml>");
        data.setForsendelseId(forsendelseId);

        MottakMeldingDataWrapper neste = toTaskWithPrecondition(data);

        verify(klargjørForVLTjeneste).klargjørForVL(any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
        verify(dokumentRepository).oppdaterForsendelseMetadata(forsendelseId, ARKIV_ID, SAKSNUMMER, ForsendelseStatus.FPSAK);
        assertThat(neste).isNotNull();
        assertThat(neste.getProsessTaskData().getTaskType()).isEqualTo(SlettForsendelseTask.TASKNAME);
    }

    @Test
    public void test_avsluuter_hvis_forsendelseId_ikke_satt() {
        MottakMeldingDataWrapper data = new MottakMeldingDataWrapper(ptd);
        data.setArkivId(ARKIV_ID);
        data.setSaksnummer(SAKSNUMMER);
        data.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_ADOPSJON);
        data.setDokumentTypeId(DokumentTypeId.SØKNAD_ENGANGSSTØNAD_ADOPSJON);
        data.setForsendelseMottattTidspunkt(LocalDateTime.now());
        data.setPayload("<xml>test<xml>");

        MottakMeldingDataWrapper neste = toTaskWithPrecondition(data);

        verify(klargjørForVLTjeneste).klargjørForVL(any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
        assertThat(neste).isNull();
    }

    private MottakMeldingDataWrapper toTaskWithPrecondition(MottakMeldingDataWrapper data) {
        task.precondition(data);
        return task.doTask(data);
    }
}
