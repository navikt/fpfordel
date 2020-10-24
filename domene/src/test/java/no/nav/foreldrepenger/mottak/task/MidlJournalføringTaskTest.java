package no.nav.foreldrepenger.mottak.task;

import static no.nav.foreldrepenger.mottak.journal.DokumentArkivTestUtil.BRUKER_ID;
import static no.nav.foreldrepenger.mottak.journal.DokumentArkivTestUtil.JOURNALPOST_ID;
import static no.nav.foreldrepenger.mottak.task.MidlJournalføringTask.TASKNAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverdi.Tema;
import no.nav.foreldrepenger.mottak.domene.dokument.Dokument;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentRepository;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.journal.ArkivTjeneste;
import no.nav.foreldrepenger.mottak.journal.DokumentArkivTestUtil;
import no.nav.foreldrepenger.mottak.journal.dokarkiv.DokArkivTjeneste;
import no.nav.foreldrepenger.mottak.journal.dokarkiv.model.JournalpostType;
import no.nav.foreldrepenger.mottak.journal.dokarkiv.model.OpprettJournalpostRequest;
import no.nav.foreldrepenger.mottak.journal.dokarkiv.model.OpprettJournalpostResponse;
import no.nav.foreldrepenger.mottak.person.PersonTjeneste;
import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseStatus;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

@ExtendWith(MockitoExtension.class)
public class MidlJournalføringTaskTest {
    private static final String FNR = "90000000009";
    private static final String NAVN = "Kari Etternavn";

    @Mock
    private ProsessTaskRepository prosessTaskRepositoryMock;
    @Mock
    private DokumentRepository dokumentRepositoryMock;
    @Mock
    private DokArkivTjeneste dokArkivTjeneste;
    @Mock
    private PersonTjeneste personTjeneste;

    private MidlJournalføringTask task;
    private UUID forsendelseId;

    @BeforeEach
    public void setup() throws Exception {
        forsendelseId = UUID.randomUUID();
        when(personTjeneste.hentPersonIdentForAktørId(BRUKER_ID)).thenReturn(Optional.of(FNR));
        when(personTjeneste.hentNavn(any())).thenReturn(NAVN);
        ArkivTjeneste arkivTjeneste = new ArkivTjeneste(null, dokArkivTjeneste, dokumentRepositoryMock, personTjeneste);
        task = new MidlJournalføringTask(prosessTaskRepositoryMock, arkivTjeneste, dokumentRepositoryMock);

    }

    @Test
    public void test_skalVedJournalføringAvDokumentForsendelseFåJournalTilstandEndeligJournalført() {
        var ptd = new ProsessTaskData(TASKNAME);
        ptd.setSekvens("1");
        MottakMeldingDataWrapper data = new MottakMeldingDataWrapper(ptd);

        List<Dokument> dokumenter = DokumentArkivTestUtil.lagHoveddokumentMedXmlOgPdf(forsendelseId, DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL);

        when(dokArkivTjeneste.opprettJournalpost(any(), anyBoolean())).thenReturn(new OpprettJournalpostResponse(JOURNALPOST_ID, false, List.of()));

        when(dokumentRepositoryMock.hentEksaktDokumentMetadata(any(UUID.class))).thenReturn(DokumentArkivTestUtil.lagMetadata(forsendelseId, null));
        when(dokumentRepositoryMock.hentDokumenter(any(UUID.class))).thenReturn(dokumenter);

        data.setForsendelseId(forsendelseId);
        data.setAktørId(BRUKER_ID);
        data.setBehandlingTema(BehandlingTema.FORELDREPENGER_FØDSEL);
        data.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);

        ArgumentCaptor<OpprettJournalpostRequest> dokCapture = ArgumentCaptor.forClass(OpprettJournalpostRequest.class);

        MottakMeldingDataWrapper target = task.doTask(data);

        verify(dokArkivTjeneste).opprettJournalpost(dokCapture.capture(), anyBoolean());
        verify(dokumentRepositoryMock).oppdaterForsendelseMedArkivId(any(UUID.class), any(), any(ForsendelseStatus.class));

        OpprettJournalpostRequest request = dokCapture.getValue();
        assertThat(request.getBruker().getId()).isEqualTo(BRUKER_ID);
        assertThat(request.getAvsenderMottaker().getNavn()).isEqualTo(NAVN);
        assertThat(request.getJournalpostType()).isEqualTo(JournalpostType.INNGAAENDE);
        assertThat(target.getArkivId()).isEqualTo(JOURNALPOST_ID);
    }
}
