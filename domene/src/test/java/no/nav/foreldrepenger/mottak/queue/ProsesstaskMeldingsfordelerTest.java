package no.nav.foreldrepenger.mottak.queue;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.SQLException;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.fordel.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.Fagsystem;
import no.nav.melding.virksomhet.dokumentnotifikasjon.v1.XMLBehandlingstema;
import no.nav.melding.virksomhet.dokumentnotifikasjon.v1.XMLForsendelsesinformasjon;
import no.nav.melding.virksomhet.dokumentnotifikasjon.v1.XMLTema;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskInfo;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskStatus;
import no.nav.vedtak.felles.prosesstask.impl.ProsessTaskRepositoryImpl;
import no.nav.vedtak.felles.testutilities.db.RepositoryRule;


public class ProsesstaskMeldingsfordelerTest {
    private ProsessTaskRepository prosessTaskRepository;
    private ProsesstaskMeldingsfordeler meldingsFordeler;

    @Rule
    public RepositoryRule repoRule = new UnittestRepositoryRule();

    @Before
    public void setup() throws SQLException {
        prosessTaskRepository = new ProsessTaskRepositoryImpl(repoRule.getEntityManager(), null);
        meldingsFordeler = new ProsesstaskMeldingsfordeler(prosessTaskRepository);
    }

    @Test
    public void testSoknadEngangstonadOppretterKorrektTask() {
        BehandlingTema behandlingTemaKodeliste = BehandlingTema.ENGANGSSTØNAD_ADOPSJON;
        XMLForsendelsesinformasjon input = new XMLForsendelsesinformasjon();

        String offisiellKode = behandlingTemaKodeliste.getOffisiellKode();
        XMLBehandlingstema behandlingstema = new XMLBehandlingstema();
        behandlingstema.setValue(offisiellKode);
        input.setBehandlingstema(behandlingstema);
        input.setArkivId("12345");
        input.setArkivsystem(Fagsystem.GOSYS.getKode());
        XMLTema tema = new XMLTema();
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
        XMLForsendelsesinformasjon input = new XMLForsendelsesinformasjon();
        XMLBehandlingstema behandlingstema = new XMLBehandlingstema();
        behandlingstema.setValue("UgyldigTema");
        input.setArkivId("12345");
        input.setArkivsystem(Fagsystem.GOSYS.getKode());
        input.setBehandlingstema(behandlingstema);
        XMLTema tema = new XMLTema();
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
        XMLForsendelsesinformasjon input = new XMLForsendelsesinformasjon();
        input.setArkivId("12345");
        input.setArkivsystem(Fagsystem.GOSYS.getKode());
        XMLTema tema = new XMLTema();
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
