package no.nav.foreldrepenger.mottak.domene.oppgavebehandling;

import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.ARKIV_ID_KEY;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.BEHANDLINGSTEMA_KEY;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.DOKUMENTTYPE_ID_KEY;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.JOURNAL_ENHET;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.RETRY_KEY;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.TEMA_KEY;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import no.nav.foreldrepenger.journalføring.domene.JournalpostId;
import no.nav.foreldrepenger.journalføring.oppgave.Journalføringsoppgave;
import no.nav.foreldrepenger.journalføring.oppgave.domene.NyOppgave;
import no.nav.foreldrepenger.mottak.behandlendeenhet.EnhetsTjeneste;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.journal.DokumentArkivTestUtil;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;

@ExtendWith(MockitoExtension.class)
class OpprettGSakOppgaveTjenesteTaskTest {
    private final String FORDELINGSOPPGAVE_ENHET_ID = "4825";
    private OpprettGSakOppgaveTask task;

    @Mock
    private ProsessTaskTjeneste prosessTaskTjeneste;
    @Mock
    private EnhetsTjeneste enhetsidTjeneste;
    @Mock
    private Journalføringsoppgave oppgaverTjeneste;

    @BeforeEach
    public void setup() {
        when(enhetsidTjeneste.hentFordelingEnhetId(any(), any(), any(), any())).thenReturn(FORDELINGSOPPGAVE_ENHET_ID);
        task = new OpprettGSakOppgaveTask(prosessTaskTjeneste, oppgaverTjeneste, enhetsidTjeneste);
    }

    @Test
    void testServiceTask_journalforingsoppgave() {

        final String aktørId = "9000000000009";
        final BehandlingTema behandlingTema = BehandlingTema.ENGANGSSTØNAD_FØDSEL;

        var taskData = ProsessTaskData.forProsessTask(OpprettGSakOppgaveTask.class);
        taskData.setProperty(TEMA_KEY, Tema.FORELDRE_OG_SVANGERSKAPSPENGER.getKode());
        taskData.setProperty(BEHANDLINGSTEMA_KEY, behandlingTema.getKode());
        taskData.setProperty(DOKUMENTTYPE_ID_KEY, DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL.getKode());
        taskData.setAktørId(aktørId);
        var arkivId = "123456";
        taskData.setProperty(ARKIV_ID_KEY, arkivId);

        String beskrivelse = DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL.getTermNavn();

        task.doTask(taskData);

        verify(oppgaverTjeneste).opprettJournalføringsoppgaveFor(buildNyOppgave(arkivId, FORDELINGSOPPGAVE_ENHET_ID, aktørId, BehandlingTema.ENGANGSSTØNAD, beskrivelse));
        verify(oppgaverTjeneste, never()).opprettGosysJournalføringsoppgaveFor(any(NyOppgave.class));
    }

    @Test
    void testServiceTask_uten_aktørId_fordelingsoppgave_klage_gosys() {
        String klageEnhet = EnhetsTjeneste.NK_ENHET_ID;
        var taskData = ProsessTaskData.forProsessTask(OpprettGSakOppgaveTask.class);
        taskData.setProperty(TEMA_KEY, Tema.FORELDRE_OG_SVANGERSKAPSPENGER.getKode());
        taskData.setProperty(BEHANDLINGSTEMA_KEY, BehandlingTema.ENGANGSSTØNAD_FØDSEL.getKode());
        taskData.setProperty(DOKUMENTTYPE_ID_KEY, DokumentTypeId.UDEFINERT.getKode());
        taskData.setProperty(JOURNAL_ENHET, klageEnhet);
        var arkivId = "123456";
        taskData.setProperty(ARKIV_ID_KEY, arkivId);
        String beskrivelse = "Journalføring " + BehandlingTema.ENGANGSSTØNAD_FØDSEL.getTermNavn();

        when(enhetsidTjeneste.hentFordelingEnhetId(any(), any(), eq(Optional.of(klageEnhet)), any())).thenReturn(klageEnhet);

        task.doTask(taskData);

        verify(oppgaverTjeneste).opprettGosysJournalføringsoppgaveFor(buildNyOppgave(arkivId, klageEnhet, null, BehandlingTema.ENGANGSSTØNAD, beskrivelse));
        verify(oppgaverTjeneste, never()).opprettJournalføringsoppgaveFor(any(NyOppgave.class));
    }

    @Test
    void testSkalJournalføreDokumentForsendelse() {
        var forsendelseId = UUID.randomUUID();
        var taskData = ProsessTaskData.forProsessTask(OpprettGSakOppgaveTask.class);
        taskData.setProperty(TEMA_KEY, Tema.FORELDRE_OG_SVANGERSKAPSPENGER.getKode());
        taskData.setProperty(BEHANDLINGSTEMA_KEY, BehandlingTema.FORELDREPENGER.getKode());
        taskData.setProperty(MottakMeldingDataWrapper.FORSENDELSE_ID_KEY, forsendelseId.toString());
        taskData.setProperty(DOKUMENTTYPE_ID_KEY, DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL.getKode());
        taskData.setProperty(RETRY_KEY, "J");
        var arkivId = DokumentArkivTestUtil.lagOpprettRespons(false).journalpostId();
        taskData.setProperty(ARKIV_ID_KEY, arkivId);

        String beskrivelse = DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL.getTermNavn();

        task.doTask(taskData);

        verify(oppgaverTjeneste).opprettJournalføringsoppgaveFor(buildNyOppgave(arkivId, FORDELINGSOPPGAVE_ENHET_ID, null, BehandlingTema.FORELDREPENGER, beskrivelse));
        verify(oppgaverTjeneste, never()).opprettGosysJournalføringsoppgaveFor(any(NyOppgave.class));
    }

    @Test
    void testAnnetDokumentType_annet_gosys() {
        var forsendelseId = UUID.randomUUID();
        var taskData = ProsessTaskData.forProsessTask(OpprettGSakOppgaveTask.class);
        taskData.setProperty(TEMA_KEY, Tema.FORELDRE_OG_SVANGERSKAPSPENGER.getKode());
        taskData.setProperty(BEHANDLINGSTEMA_KEY, BehandlingTema.FORELDREPENGER.getKode());
        taskData.setProperty(MottakMeldingDataWrapper.FORSENDELSE_ID_KEY, forsendelseId.toString());
        taskData.setProperty(DOKUMENTTYPE_ID_KEY, DokumentTypeId.ANNET.getKode());
        taskData.setProperty(RETRY_KEY, "J");
        var arkivId = DokumentArkivTestUtil.lagOpprettRespons(false).journalpostId();
        taskData.setProperty(ARKIV_ID_KEY, arkivId);

        String beskrivelse = DokumentTypeId.ANNET.getTermNavn();

        task.doTask(taskData);

        verify(oppgaverTjeneste).opprettGosysJournalføringsoppgaveFor(buildNyOppgave(arkivId, FORDELINGSOPPGAVE_ENHET_ID, null, BehandlingTema.FORELDREPENGER, beskrivelse));
        verify(oppgaverTjeneste, never()).opprettJournalføringsoppgaveFor(any(NyOppgave.class));
    }

    private NyOppgave buildNyOppgave(String arkivId, String enhet, String aktørId, BehandlingTema behandlingTema, String beskrivelse) {
        return NyOppgave.builder()
            .medJournalpostId(JournalpostId.fra(arkivId))
            .medEnhetId(enhet)
            .medAktørId(aktørId)
            .medSaksref(null)
            .medBehandlingTema(behandlingTema)
            .medBeskrivelse(beskrivelse)
            .build();
    }
}
