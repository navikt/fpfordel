package no.nav.foreldrepenger.mottak.domene.oppgavebehandling;

import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.ARKIV_ID_KEY;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.BEHANDLINGSTEMA_KEY;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.DOKUMENTTYPE_ID_KEY;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.JOURNAL_ENHET;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.RETRY_KEY;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.TEMA_KEY;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import no.nav.foreldrepenger.mottak.behandlendeenhet.JournalføringsOppgave;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.journal.DokumentArkivTestUtil;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;

@ExtendWith(MockitoExtension.class)
class OpprettGSakOppgaveTjenesteTaskTest {

    @Mock
    private ProsessTaskTjeneste prosessTaskTjeneste;
    private String fordelingsOppgaveEnhetsId = "4825";

    private OpprettGSakOppgaveTask task;
    @Mock
    private JournalføringsOppgave enhetsidTjeneste;

    @BeforeEach
    public void setup() {
        when(enhetsidTjeneste.hentFordelingEnhetId(any(), any(), any(), any())).thenReturn(fordelingsOppgaveEnhetsId);
        when(enhetsidTjeneste.opprettJournalføringsOppgave(any(), any(), any(), any(), any(), any())).thenReturn("99");
        task = new OpprettGSakOppgaveTask(prosessTaskTjeneste, enhetsidTjeneste);
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

        String beskrivelse = DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL.getTermNavn();

        task.doTask(taskData);

        verify(enhetsidTjeneste).opprettJournalføringsOppgave(null, fordelingsOppgaveEnhetsId, aktørId, null,
            BehandlingTema.ENGANGSSTØNAD.getOffisiellKode(), beskrivelse);
    }

    @Test
    void testServiceTask_uten_aktørId_fordelingsoppgave() {
        String enhet = "4292";
        var taskData = ProsessTaskData.forProsessTask(OpprettGSakOppgaveTask.class);
        taskData.setProperty(TEMA_KEY, Tema.FORELDRE_OG_SVANGERSKAPSPENGER.getKode());
        taskData.setProperty(BEHANDLINGSTEMA_KEY, BehandlingTema.ENGANGSSTØNAD_FØDSEL.getKode());
        taskData.setProperty(DOKUMENTTYPE_ID_KEY, DokumentTypeId.UDEFINERT.getKode());
        taskData.setProperty(JOURNAL_ENHET, enhet);
        String beskrivelse = "Journalføring " + BehandlingTema.ENGANGSSTØNAD_FØDSEL.getTermNavn();

        when(enhetsidTjeneste.hentFordelingEnhetId(any(), any(), eq(Optional.of(enhet)), any())).thenReturn(enhet);

        task.doTask(taskData);

        verify(enhetsidTjeneste).opprettJournalføringsOppgave(null, enhet, null, null, BehandlingTema.ENGANGSSTØNAD.getOffisiellKode(), beskrivelse);


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

        verify(enhetsidTjeneste).opprettJournalføringsOppgave(arkivId, fordelingsOppgaveEnhetsId, null, null,
            BehandlingTema.FORELDREPENGER.getOffisiellKode(), beskrivelse);
    }
}
