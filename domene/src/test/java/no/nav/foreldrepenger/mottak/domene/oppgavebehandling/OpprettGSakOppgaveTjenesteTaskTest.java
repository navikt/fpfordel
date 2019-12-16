package no.nav.foreldrepenger.mottak.domene.oppgavebehandling;

import static no.nav.foreldrepenger.mottak.domene.oppgavebehandling.OpprettGSakOppgaveTask.TASKNAME;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.ARKIV_ID_KEY;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.BEHANDLINGSTEMA_KEY;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.DOKUMENTTYPE_ID_KEY;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.JOURNAL_ENHET;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.RETRY_KEY;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.TEMA_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverdi.Tema;
import no.nav.foreldrepenger.mottak.behandlendeenhet.EnhetsTjeneste;
import no.nav.foreldrepenger.mottak.domene.dokument.Dokument;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentRepository;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.journal.dokumentforsendelse.DokumentforsendelseTestUtil;
import no.nav.foreldrepenger.mottak.journal.dokumentforsendelse.JournalTilstand;
import no.nav.tjeneste.virksomhet.behandleoppgave.v1.meldinger.WSOpprettOppgaveResponse;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumerMedCache;
import no.nav.vedtak.felles.integrasjon.behandleoppgave.BehandleoppgaveConsumer;
import no.nav.vedtak.felles.integrasjon.behandleoppgave.opprett.OpprettOppgaveRequest;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

public class OpprettGSakOppgaveTjenesteTaskTest {

    private static final String SAKSNUMMER = "9876543";

    private ProsessTaskRepository prosessTaskRepository;
    private BehandleoppgaveConsumer mockService;
    private DokumentRepository dokumentRepository;
    private AktørConsumerMedCache aktørConsumer;

    private String fordelingsOppgaveEnhetsId = "4825";

    private OpprettGSakOppgaveTask task;
    private EnhetsTjeneste enhetsidTjeneste;

    @Before
    public void setup() {
        prosessTaskRepository = Mockito.mock(ProsessTaskRepository.class);
        mockService = Mockito.mock(BehandleoppgaveConsumer.class);
        aktørConsumer = mock(AktørConsumerMedCache.class);
        enhetsidTjeneste = mock(EnhetsTjeneste.class);
        dokumentRepository = mock(DokumentRepository.class);
        when(enhetsidTjeneste.hentFordelingEnhetId(any(), any(), any(), any())).thenReturn(fordelingsOppgaveEnhetsId);
        task = new OpprettGSakOppgaveTask(prosessTaskRepository, mockService, enhetsidTjeneste, aktørConsumer);
    }

    @Test
    public void testServiceTask_journalforingsoppgave() {

        final String fodselsnummer = "99999999899";
        final String aktørId = "9000000000009";
        final BehandlingTema behandlingTema = BehandlingTema.ENGANGSSTØNAD_FØDSEL;

        ProsessTaskData taskData = new ProsessTaskData(TASKNAME);
        taskData.setProperty(TEMA_KEY, Tema.FORELDRE_OG_SVANGERSKAPSPENGER.getKode());
        taskData.setProperty(BEHANDLINGSTEMA_KEY, behandlingTema.getKode());
        taskData.setProperty(DOKUMENTTYPE_ID_KEY, DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL.getKode());
        taskData.setAktørId(aktørId);
        WSOpprettOppgaveResponse mockResponse = new WSOpprettOppgaveResponse();
        mockResponse.setOppgaveId("MOCK");

        String beskrivelse = DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL.getTermNavn();

        ArgumentCaptor<OpprettOppgaveRequest> captor = ArgumentCaptor.forClass(OpprettOppgaveRequest.class);

        when(mockService.opprettOppgave(captor.capture())).thenReturn(mockResponse);
        when(aktørConsumer.hentPersonIdentForAktørId(aktørId)).thenReturn(Optional.of(fodselsnummer));

        task.doTask(taskData);

        OpprettOppgaveRequest serviceRequest = captor.getValue();

        assertEquals(serviceRequest.getBeskrivelse(), beskrivelse);
        assertThat(serviceRequest.getOppgavetypeKode()).as("Forventer at oppgavekode er journalføring foreldrepenger").isEqualTo(OpprettGSakOppgaveTask.JFR_FOR);
    }

    @Test
    public void testServiceTask_uten_aktørId_fordelingsoppgave() {
        String enhet = "4292";
        ProsessTaskData taskData = new ProsessTaskData(TASKNAME);
        taskData.setProperty(TEMA_KEY, Tema.FORELDRE_OG_SVANGERSKAPSPENGER.getKode());
        taskData.setProperty(BEHANDLINGSTEMA_KEY, BehandlingTema.ENGANGSSTØNAD_FØDSEL.getKode());
        taskData.setProperty(DOKUMENTTYPE_ID_KEY, DokumentTypeId.UDEFINERT.getKode());
        taskData.setProperty(JOURNAL_ENHET, enhet);
        String beskrivelse = BehandlingTema.ENGANGSSTØNAD_FØDSEL.getTermNavn();

        WSOpprettOppgaveResponse mockResponse = new WSOpprettOppgaveResponse();
        mockResponse.setOppgaveId("MOCK");

        ArgumentCaptor<OpprettOppgaveRequest> captor = ArgumentCaptor.forClass(OpprettOppgaveRequest.class);

        when(mockService.opprettOppgave(captor.capture())).thenReturn(mockResponse);
        when(enhetsidTjeneste.hentFordelingEnhetId(any(), any(), eq(Optional.of(enhet)), any())).thenReturn(enhet);

        task.doTask(taskData);

        OpprettOppgaveRequest serviceRequest = captor.getValue();
        assertThat(serviceRequest.getBeskrivelse()).as("Forventer at beskrivelse er fordelingsoppgave når vi ikke har aktørId.").isEqualTo(beskrivelse);
        assertThat(serviceRequest.getOppgavetypeKode()).as("Forventer at oppgavekode er fordeling foreldrepenger").isEqualTo(OpprettGSakOppgaveTask.JFR_FOR);
        assertThat(serviceRequest.getAnsvarligEnhetId()).as("Forventer journalførende enhet").isEqualTo(enhet);
    }

    @Test
    public void testSkalJournalføreDokumentForsendelse() {
        UUID forsendelseId = UUID.randomUUID();
        ProsessTaskData taskData = new ProsessTaskData(TASKNAME);
        taskData.setProperty(TEMA_KEY, Tema.FORELDRE_OG_SVANGERSKAPSPENGER.getKode());
        taskData.setProperty(BEHANDLINGSTEMA_KEY, BehandlingTema.FORELDREPENGER.getKode());
        taskData.setProperty(MottakMeldingDataWrapper.FORSENDELSE_ID_KEY, forsendelseId.toString());
        taskData.setProperty(DOKUMENTTYPE_ID_KEY, DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL.getKode());
        taskData.setProperty(RETRY_KEY, "J");
        taskData.setProperty(ARKIV_ID_KEY, DokumentforsendelseTestUtil.lagDokumentforsendelseRespons(JournalTilstand.MIDLERTIDIG_JOURNALFØRT, 3).getJournalpostId());

        List<Dokument> dokumenter = new ArrayList<>();
        dokumenter.addAll(DokumentforsendelseTestUtil.lagHoveddokumentMedXmlOgPdf(forsendelseId, DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL));

        when(dokumentRepository.hentEksaktDokumentMetadata(any(UUID.class))).thenReturn(DokumentforsendelseTestUtil.lagMetadata(forsendelseId, SAKSNUMMER));
        when(dokumentRepository.hentDokumenter(any(UUID.class))).thenReturn(dokumenter);

        WSOpprettOppgaveResponse mockResponse = new WSOpprettOppgaveResponse();
        mockResponse.setOppgaveId("MOCK");
        String beskrivelse = DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL.getTermNavn();

        ArgumentCaptor<OpprettOppgaveRequest> captor = ArgumentCaptor.forClass(OpprettOppgaveRequest.class);

        when(mockService.opprettOppgave(captor.capture())).thenReturn(mockResponse);

        task.doTask(taskData);

        OpprettOppgaveRequest serviceRequest = captor.getValue();
        assertThat(serviceRequest.getBeskrivelse()).as("Forventer at beskrivelse er fordelingsoppgave når vi ikke har aktørId.").isEqualTo(beskrivelse);
    }
}
