package no.nav.foreldrepenger.mottak.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.foreldrepenger.mottak.extensions.EntityManagerAwareFordelExtension;
import no.nav.foreldrepenger.mottak.extensions.EntityManagerAwareTest;
import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskInfo;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskStatus;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTypeInfo;
import no.nav.vedtak.felles.prosesstask.impl.ProsessTaskEventPubliserer;
import no.nav.vedtak.felles.prosesstask.impl.ProsessTaskRepositoryImpl;
import no.nav.vedtak.felles.prosesstask.impl.SubjectProvider;
import no.nav.vedtak.prosesstask.legacy.LegacySubjectProvider;

@ExtendWith(EntityManagerAwareFordelExtension.class)
@ExtendWith(MockitoExtension.class)
public class OverførTilGsakFeilhåndteringsalgoritmeTest extends EntityManagerAwareTest {

    private ProsessTaskRepository prosessTaskRepository;

    @Mock
    ProsessTaskEventPubliserer ss;
    private OverførTilGsakFeilhåndteringsalgoritme algoritme;
    private ProsessTaskTypeInfo type = lagType();

    @BeforeEach
    public void opprettAlgoritme() {
        SubjectProvider sp = new LegacySubjectProvider();
        prosessTaskRepository = new ProsessTaskRepositoryImpl(getEntityManager(), sp, ss);
        algoritme = new OverførTilGsakMedBackoffFeilhåndteringsalgoritme(prosessTaskRepository);
    }

    @Test
    public void skal_si_at_IntegrasjonException_ikke_skal_kjøres_på_nytt() throws Exception {
        assertThat(algoritme.skalKjørePåNytt(type, 1, new IntegrasjonException(mock(Feil.class)))).isFalse();
    }

    @Test
    public void skal_si_at_TekniskException_skal_kjøres_på_nytt() throws Exception {
        assertThat(algoritme.skalKjørePåNytt(type, 1, new TekniskException(mock(Feil.class)))).isTrue();
    }

    @Test
    public void skal_si_at_TekniskException_ikke_skal_kjøres_på_nytt_når_maks_antall_er_overskredet() throws Exception {
        assertThat(algoritme.skalKjørePåNytt(type, 2, new TekniskException(mock(Feil.class)))).isFalse();
    }

    @Test
    public void skal_opprette_prosess_task_for_å_sende_til_manuell_behandling_hos_gsak_når_det_er_integrasjonException() throws Exception {
        ProsessTaskData originalTaskData = new ProsessTaskData(TilJournalføringTask.TASKNAME);
        originalTaskData.setGruppe("1234");
        originalTaskData.setSekvens("3");

        IntegrasjonException exception = new IntegrasjonException(mock(Feil.class));

        Feil feil = algoritme.hendelserNårIkkeKjøresPåNytt(exception, originalTaskData);
        assertThat(feil).isSameAs(exception.getFeil());

        // Flush gjøres normalt rett etter feilhåndteringsalgoritmen har kjørt
        ((ProsessTaskRepositoryImpl) prosessTaskRepository).getEntityManager().flush();

        List<ProsessTaskData> oppgaver = prosessTaskRepository.finnAlle(ProsessTaskStatus.KLAR);
        assertThat(oppgaver).hasSize(1);
        ProsessTaskInfo oppgave = oppgaver.get(0);
        assertThat(oppgave.getTaskType()).isEqualTo(MidlJournalføringTask.TASKNAME);

        // skal ha samme gruppe for å vise at dette tilhører samme flyt
        assertThat(oppgave.getGruppe()).isEqualTo("1234");
        // må ha samme (eller lavere) sekvensnummer enn prosess_task som feilet, ellers
        // vil den ikke blir kjørt
        assertThat(oppgave.getSekvens()).isEqualTo("3");

    }

    private static ProsessTaskTypeInfo lagType() {
        ProsessTaskTypeInfo mock = mock(ProsessTaskTypeInfo.class);
        when(mock.getMaksForsøk()).thenReturn(2);
        return mock;
    }

}
