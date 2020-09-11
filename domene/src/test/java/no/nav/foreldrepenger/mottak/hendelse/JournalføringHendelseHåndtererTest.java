package no.nav.foreldrepenger.mottak.hendelse;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.TimeZone;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.MottakKanal;
import no.nav.foreldrepenger.fordel.kodeverdi.Tema;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentRepository;
import no.nav.foreldrepenger.mottak.extensions.EntityManagerAwareExtension;
import no.nav.foreldrepenger.mottak.extensions.EntityManagerAwareTest;
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskInfo;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskStatus;
import no.nav.vedtak.felles.prosesstask.impl.ProsessTaskRepositoryImpl;

@ExtendWith(EntityManagerAwareExtension.class)
public class JournalføringHendelseHåndtererTest extends EntityManagerAwareTest {

    private ProsessTaskRepository prosessTaskRepository;
    private DokumentRepository dokumentRepository;
    private JournalføringHendelseHåndterer hendelseHåndterer;

    static {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Oslo"));
    }

    @BeforeEach
    public void setup() {
        prosessTaskRepository = new ProsessTaskRepositoryImpl(getEntityManager(), null, null);
        dokumentRepository = new DokumentRepository(getEntityManager());
        hendelseHåndterer = new JournalføringHendelseHåndterer(prosessTaskRepository, dokumentRepository);
    }

    @Test
    public void testSoknadEngangstonadOppretterKorrektTask() {
        var builder = JournalfoeringHendelseRecord.newBuilder()
                .setHendelsesId("12345").setVersjon(1)
                .setHendelsesType("MidlertidigJournalført")
                .setTemaNytt(Tema.FORELDRE_OG_SVANGERSKAPSPENGER.getOffisiellKode()).setTemaGammelt("")
                .setBehandlingstema(BehandlingTema.ENGANGSSTØNAD_FØDSEL.getOffisiellKode())
                .setMottaksKanal(MottakKanal.SELVBETJENING.getKode())
                .setKanalReferanseId("minfil.pdf")
                .setJournalpostId(12345L)
                .setJournalpostStatus("M");

        hendelseHåndterer.handleMessage(null, builder.build());
        getEntityManager().flush();
        List<ProsessTaskData> result = prosessTaskRepository.finnAlle(ProsessTaskStatus.KLAR);
        assertThat(result).as("Forventer at en prosesstask er lagt til").hasSize(1);

        ProsessTaskInfo prosessTaskData = result.get(0);
        assertThat(prosessTaskData.getTaskType()).as("Forventer at prosesstask av korrekt type blir opprettet. ")
                .isEqualToIgnoringCase("fordeling.hentFraJoark");

    }

    @Test
    public void testSoknadUkjentTypeSendesLikevelTilNesteSteg() {
        var builder = JournalfoeringHendelseRecord.newBuilder()
                .setHendelsesId("12345").setVersjon(1)
                .setHendelsesType("MidlertidigJournalført")
                .setTemaNytt(Tema.FORELDRE_OG_SVANGERSKAPSPENGER.getOffisiellKode()).setTemaGammelt("")
                .setMottaksKanal(MottakKanal.ALTINN.getKode())
                .setKanalReferanseId("AR325657")
                .setJournalpostId(12345L)
                .setJournalpostStatus("M");

        hendelseHåndterer.handleMessage(null, builder.build());
        getEntityManager().flush();

        List<ProsessTaskData> result = prosessTaskRepository.finnAlle(ProsessTaskStatus.KLAR);
        assertThat(result).as("Forventer at en prosesstask er lagt til").hasSize(1);
        ProsessTaskInfo prosessTaskData = result.get(0);
        assertThat(prosessTaskData.getTaskType()).as("Forventer at prosesstask av korrekt type blir opprettet. ")
                .isEqualToIgnoringCase("fordeling.hentFraJoark");

    }

    @Test
    public void testDokumentFraKloningIgnoreres() {
        JournalfoeringHendelseRecord.newBuilder()
                .setHendelsesId("12345").setVersjon(1)
                .setHendelsesType("MidlertidigJournalført")
                .setTemaNytt(Tema.FORELDRE_OG_SVANGERSKAPSPENGER.getOffisiellKode()).setTemaGammelt("")
                .setMottaksKanal(MottakKanal.SELVBETJENING.getKode())
                .setJournalpostId(12345L)
                .setJournalpostStatus("M");

        List<ProsessTaskData> result = prosessTaskRepository.finnAlle(ProsessTaskStatus.KLAR);
        assertThat(result).as("Forventer at en prosesstask er lagt til").isEmpty();
    }

    @Test
    public void test_0argCtor() {
        new JournalføringHendelseHåndterer();
    }
}
