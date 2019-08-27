package no.nav.foreldrepenger.mottak.queue;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.SQLException;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.fordel.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.fordel.kodeverk.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverk.Fagsystem;
import no.nav.foreldrepenger.fordel.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.fordel.kodeverk.KodeverkRepositoryImpl;
import no.nav.melding.virksomhet.dokumentnotifikasjon.v1.Behandlingstema;
import no.nav.melding.virksomhet.dokumentnotifikasjon.v1.Forsendelsesinformasjon;
import no.nav.melding.virksomhet.dokumentnotifikasjon.v1.Tema;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskInfo;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskStatus;
import no.nav.vedtak.felles.prosesstask.impl.ProsessTaskRepositoryImpl;
import no.nav.vedtak.felles.testutilities.db.RepositoryRule;

public class ProsesstaskMeldingsfordelerTest {
    private ProsessTaskRepository prosessTaskRepository;
    private KodeverkRepository kodeverkRepository;
    private ProsesstaskMeldingsfordeler meldingsFordeler;

    @Rule
    public RepositoryRule repoRule = new UnittestRepositoryRule();

    @Before
    public void setup() throws SQLException {
        prosessTaskRepository = new ProsessTaskRepositoryImpl(repoRule.getEntityManager(), null);
        kodeverkRepository = new KodeverkRepositoryImpl(repoRule.getEntityManager());
        meldingsFordeler = new ProsesstaskMeldingsfordeler(prosessTaskRepository, kodeverkRepository);
    }

    @Test
    public void testSoknadEngangstonadOppretterKorrektTask() {
        Forsendelsesinformasjon input = new Forsendelsesinformasjon();
        BehandlingTema behandlingTemaKodeliste = kodeverkRepository.finn(BehandlingTema.class, BehandlingTema.ENGANGSSTØNAD_ADOPSJON);
        String offisiellKode = behandlingTemaKodeliste.getOffisiellKode();
        Behandlingstema behandlingstema = new Behandlingstema();
        behandlingstema.setValue(offisiellKode);
        input.setBehandlingstema(behandlingstema);
        input.setArkivId("12345");
        input.setArkivsystem(Fagsystem.GOSYS.getOffisiellKode());
        Tema tema = new Tema();
        tema.setValue("FOR");
        input.setTema(tema);

        meldingsFordeler.execute(input);
        repoRule.getRepository().flush();

        List<ProsessTaskData> result = prosessTaskRepository.finnAlle(ProsessTaskStatus.KLAR);
        assertThat(result).as("Forventer at en prosesstask er lagt til").hasSize(1);

        ProsessTaskInfo prosessTaskData = result.get(0);
        assertThat(prosessTaskData.getTaskType()).as("Forventer at prosesstask av korrekt type blir opprettet. ")
                .isEqualToIgnoringCase("fordeling.hentFraJoark");

    }

    @Test
    public void testSoknadUkjentTypeSendesLikevelTilNesteSteg() {
        Forsendelsesinformasjon input = new Forsendelsesinformasjon();
        Behandlingstema behandlingstema = new Behandlingstema();
        behandlingstema.setValue("UgyldigTema");
        input.setArkivId("12345");
        input.setArkivsystem(Fagsystem.GOSYS.getOffisiellKode());
        input.setBehandlingstema(behandlingstema);
        Tema tema = new Tema();
        tema.setValue("FOR");
        input.setTema(tema);
        meldingsFordeler.execute(input);
        repoRule.getRepository().flush();

        List<ProsessTaskData> result = prosessTaskRepository.finnAlle(ProsessTaskStatus.KLAR);
        assertThat(result).as("Forventer at en prosesstask er lagt til").hasSize(1);
        ProsessTaskInfo prosessTaskData = result.get(0);
        assertThat(prosessTaskData.getTaskType()).as("Forventer at prosesstask av korrekt type blir opprettet. ")
                .isEqualToIgnoringCase("fordeling.hentFraJoark");

    }

    @Test
    public void testDokumentUtenBehandlingsTemaSendesLikevelTilNesteSteg() {
        Forsendelsesinformasjon input = new Forsendelsesinformasjon();
        input.setArkivId("12345");
        input.setArkivsystem(Fagsystem.GOSYS.getOffisiellKode());
        Tema tema = new Tema();
        tema.setValue("FOR");
        input.setTema(tema);
        meldingsFordeler.execute(input);
        repoRule.getRepository().flush();

        List<ProsessTaskData> result = prosessTaskRepository.finnAlle(ProsessTaskStatus.KLAR);
        assertThat(result).as("Forventer at en prosesstask er lagt til").hasSize(1);
        ProsessTaskInfo prosessTaskData = result.get(0);
        assertThat(prosessTaskData.getTaskType()).as("Forventer at prosesstask av korrekt type blir opprettet. ")
                .isEqualToIgnoringCase("fordeling.hentFraJoark");
    }

    @Test
    public void test_0argCtor() {
        new ProsesstaskMeldingsfordeler();
    }
}
