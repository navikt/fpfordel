package no.nav.foreldrepenger.mottak.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import no.nav.foreldrepenger.fordel.dbstoette.UnittestRepositoryRule;
import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskInfo;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskStatus;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTypeInfo;
import no.nav.vedtak.felles.prosesstask.impl.ProsessTaskRepositoryImpl;
import no.nav.vedtak.felles.testutilities.cdi.CdiRunner;

@RunWith(CdiRunner.class)
public class OverførTilGsakFeilhåndteringsalgoritmeTest {

    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    @Inject
    private ProsessTaskRepository prosessTaskRepository;

    private OverførTilGsakFeilhåndteringsalgoritme algoritme;
    private ProsessTaskTypeInfo type = lagType();


    @Before
    public void opprettAlgoritme() {
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

        //Flush gjøres normalt rett etter feilhåndteringsalgoritmen har kjørt
        ((ProsessTaskRepositoryImpl) prosessTaskRepository).getEntityManager().flush();

        List<ProsessTaskData> oppgaver = prosessTaskRepository.finnAlle(ProsessTaskStatus.KLAR);
        assertThat(oppgaver).hasSize(1);
        ProsessTaskInfo oppgave = oppgaver.get(0);
        assertThat(oppgave.getTaskType()).isEqualTo(MidlJournalføringTask.TASKNAME);

        //skal ha samme gruppe for å vise at dette tilhører samme flyt
        assertThat(oppgave.getGruppe()).isEqualTo("1234");
        //må ha samme (eller lavere) sekvensnummer enn prosess_task som feilet, ellers vil den ikke blir kjørt
        assertThat(oppgave.getSekvens()).isEqualTo("3");

    }

    private ProsessTaskTypeInfo lagType() {
        ProsessTaskTypeInfo mock = mock(ProsessTaskTypeInfo.class);
        when(mock.getMaksForsøk()).thenReturn(2);
        return mock;
    }

}
