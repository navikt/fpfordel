package no.nav.foreldrepenger.mottak.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverdi.Tema;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentMetadata;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentRepository;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.journal.DokumentArkivTestUtil;
import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseStatus;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

@ExtendWith(MockitoExtension.class)
public class SlettForsendelseTaskTest {
    private static final String SAKSNUMMER = "9876543";
    private static final String AKTØR_ID = "9000000000009";

    @Mock
    private ProsessTaskRepository prosessTaskRepositoryMock;
    @Mock
    private DokumentRepository dokumentRepositoryMock;

    private SlettForsendelseTask task;
    private ProsessTaskData ptd;
    private UUID forsendelseId;

    @BeforeEach
    public void setup() {
        forsendelseId = UUID.randomUUID();
        task = new SlettForsendelseTask(prosessTaskRepositoryMock, dokumentRepositoryMock);
        ptd = new ProsessTaskData(SlettForsendelseTask.TASKNAME);
        ptd.setSekvens("1");
    }

    @Test
    public void test_skalSletteJournalførtForsendelse() {
        MottakMeldingDataWrapper data = new MottakMeldingDataWrapper(ptd);

        DokumentArkivTestUtil.lagHoveddokumentMedXmlOgPdf(forsendelseId, DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL);
        DokumentMetadata metadata = DokumentArkivTestUtil.lagMetadata(forsendelseId, SAKSNUMMER);
        metadata.setArkivId(SAKSNUMMER);
        metadata.setStatus(ForsendelseStatus.GOSYS);

        when(dokumentRepositoryMock.hentUnikDokumentMetadata(any(UUID.class))).thenReturn(Optional.of(metadata));

        data.setForsendelseId(forsendelseId);
        data.setAktørId(AKTØR_ID);
        data.setBehandlingTema(BehandlingTema.FORELDREPENGER_FØDSEL);
        data.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);

        MottakMeldingDataWrapper target = task.doTask(data);

        verify(dokumentRepositoryMock).slettForsendelse(forsendelseId);
        assertThat(target).isNull();
    }

    @Test
    public void test_skalIkkeSlettePendingForsendelse() {
        MottakMeldingDataWrapper data = new MottakMeldingDataWrapper(ptd);

        DokumentArkivTestUtil.lagHoveddokumentMedXmlOgPdf(forsendelseId, DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL);
        DokumentMetadata metadata = DokumentArkivTestUtil.lagMetadata(forsendelseId, SAKSNUMMER);
        metadata.setStatus(ForsendelseStatus.PENDING);

        when(dokumentRepositoryMock.hentUnikDokumentMetadata(any(UUID.class))).thenReturn(Optional.of(metadata));

        data.setForsendelseId(forsendelseId);
        data.setAktørId(AKTØR_ID);
        data.setBehandlingTema(BehandlingTema.FORELDREPENGER_FØDSEL);
        data.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);

        task.doTask(data);

        verify(dokumentRepositoryMock, never()).slettForsendelse(any());
    }

    @Test
    public void test_skalIkkeSletteForsendelseUtenSaksnummer() {
        MottakMeldingDataWrapper data = new MottakMeldingDataWrapper(ptd);

        DokumentArkivTestUtil.lagHoveddokumentMedXmlOgPdf(forsendelseId, DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL);
        DokumentMetadata metadata = DokumentArkivTestUtil.lagMetadata(forsendelseId, null);
        metadata.setStatus(ForsendelseStatus.FPSAK);

        when(dokumentRepositoryMock.hentUnikDokumentMetadata(any(UUID.class))).thenReturn(Optional.of(metadata));

        data.setForsendelseId(forsendelseId);
        data.setAktørId(AKTØR_ID);
        data.setBehandlingTema(BehandlingTema.FORELDREPENGER_FØDSEL);
        data.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);

        task.doTask(data);

        verify(dokumentRepositoryMock, never()).slettForsendelse(any());
    }
}
