package no.nav.foreldrepenger.mottak.domene.oppgavebehandling;

import static no.nav.foreldrepenger.mottak.domene.oppgavebehandling.OpprettGSakOppgaveTask.OPPGAVETYPER_JFR;
import static no.nav.foreldrepenger.mottak.domene.oppgavebehandling.OpprettGSakOppgaveTask.TASKNAME;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.ARKIV_ID_KEY;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.BEHANDLINGSTEMA_KEY;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.DOKUMENTTYPE_ID_KEY;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.JOURNAL_ENHET;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.RETRY_KEY;
import static no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper.TEMA_KEY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
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
import no.nav.foreldrepenger.mottak.behandlendeenhet.EnhetsInfo;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.journal.DokumentArkivTestUtil;
import no.nav.vedtak.felles.integrasjon.oppgave.v1.Oppgave;
import no.nav.vedtak.felles.integrasjon.oppgave.v1.Oppgaver;
import no.nav.vedtak.felles.integrasjon.oppgave.v1.Oppgavestatus;
import no.nav.vedtak.felles.integrasjon.oppgave.v1.OpprettOppgave;
import no.nav.vedtak.felles.integrasjon.oppgave.v1.Prioritet;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

@ExtendWith(MockitoExtension.class)
class OpprettGSakOppgaveTjenesteTaskTest {

    private static final Oppgave OPPGAVE = new Oppgave(99L, null, null, null, null,
            Tema.FORELDRE_OG_SVANGERSKAPSPENGER.getOffisiellKode(), null, null, null, 1, "4806",
            LocalDate.now().plusDays(1), LocalDate.now(), Prioritet.NORM, Oppgavestatus.AAPNET);

    @Mock
    private ProsessTaskRepository prosessTaskRepository;
    @Mock
    private Oppgaver oppgaver;

    private String fordelingsOppgaveEnhetsId = "4825";

    private OpprettGSakOppgaveTask task;
    @Mock
    private EnhetsInfo enhetsidTjeneste;

    @BeforeEach
    public void setup() {
        when(enhetsidTjeneste.hentFordelingEnhetId(any(), any(), any(), any())).thenReturn(fordelingsOppgaveEnhetsId);
        task = new OpprettGSakOppgaveTask(prosessTaskRepository, enhetsidTjeneste, oppgaver);
    }

    @Test
    void testServiceTask_journalforingsoppgave() {

        final String aktørId = "9000000000009";
        final BehandlingTema behandlingTema = BehandlingTema.ENGANGSSTØNAD_FØDSEL;

        var taskData = new ProsessTaskData(TASKNAME);
        taskData.setProperty(TEMA_KEY, Tema.FORELDRE_OG_SVANGERSKAPSPENGER.getKode());
        taskData.setProperty(BEHANDLINGSTEMA_KEY, behandlingTema.getKode());
        taskData.setProperty(DOKUMENTTYPE_ID_KEY, DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL.getKode());
        taskData.setAktørId(aktørId);

        String beskrivelse = DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL.getTermNavn();

        var captor = ArgumentCaptor.forClass(OpprettOppgave.class);
        when(oppgaver.opprettetOppgave(captor.capture())).thenReturn(OPPGAVE);

        task.doTask(taskData);

        OpprettOppgave request = captor.getValue();
        assertThat(request.getBeskrivelse()).isEqualTo(beskrivelse);
        assertThat(request.getOppgavetype()).isEqualTo(OPPGAVETYPER_JFR);
    }

    @Test
    void testServiceTask_uten_aktørId_fordelingsoppgave() {
        String enhet = "4292";
        var taskData = new ProsessTaskData(TASKNAME);
        taskData.setProperty(TEMA_KEY, Tema.FORELDRE_OG_SVANGERSKAPSPENGER.getKode());
        taskData.setProperty(BEHANDLINGSTEMA_KEY, BehandlingTema.ENGANGSSTØNAD_FØDSEL.getKode());
        taskData.setProperty(DOKUMENTTYPE_ID_KEY, DokumentTypeId.UDEFINERT.getKode());
        taskData.setProperty(JOURNAL_ENHET, enhet);
        String beskrivelse = BehandlingTema.ENGANGSSTØNAD_FØDSEL.getTermNavn();

        var captor = ArgumentCaptor.forClass(OpprettOppgave.class);
        when(oppgaver.opprettetOppgave(captor.capture())).thenReturn(OPPGAVE);
        when(enhetsidTjeneste.hentFordelingEnhetId(any(), any(), eq(Optional.of(enhet)), any())).thenReturn(enhet);

        task.doTask(taskData);

        OpprettOppgave request = captor.getValue();
        assertThat(request.getBeskrivelse()).isEqualTo(beskrivelse);
        assertThat(request.getOppgavetype()).isEqualTo(OPPGAVETYPER_JFR);
        assertThat(request.getTildeltEnhetsnr()).isEqualTo(enhet);
    }

    @Test
    void testSkalJournalføreDokumentForsendelse() {
        var forsendelseId = UUID.randomUUID();
        var taskData = new ProsessTaskData(TASKNAME);
        taskData.setProperty(TEMA_KEY, Tema.FORELDRE_OG_SVANGERSKAPSPENGER.getKode());
        taskData.setProperty(BEHANDLINGSTEMA_KEY, BehandlingTema.FORELDREPENGER.getKode());
        taskData.setProperty(MottakMeldingDataWrapper.FORSENDELSE_ID_KEY, forsendelseId.toString());
        taskData.setProperty(DOKUMENTTYPE_ID_KEY, DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL.getKode());
        taskData.setProperty(RETRY_KEY, "J");
        taskData.setProperty(ARKIV_ID_KEY, DokumentArkivTestUtil.lagOpprettRespons(false).journalpostId());

        String beskrivelse = DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL.getTermNavn();

        var captor = ArgumentCaptor.forClass(OpprettOppgave.class);
        when(oppgaver.opprettetOppgave(captor.capture())).thenReturn(OPPGAVE);

        task.doTask(taskData);

        OpprettOppgave request = captor.getValue();
        assertThat(request.getBeskrivelse()).isEqualTo(beskrivelse);
    }
}
