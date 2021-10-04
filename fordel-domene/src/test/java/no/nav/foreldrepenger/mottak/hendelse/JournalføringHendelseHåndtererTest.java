package no.nav.foreldrepenger.mottak.hendelse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.MottakKanal;
import no.nav.foreldrepenger.fordel.kodeverdi.Tema;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentRepository;
import no.nav.foreldrepenger.mottak.extensions.FPfordelEntityManagerAwareExtension;
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskInfo;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;

@ExtendWith(MockitoExtension.class)
@ExtendWith(FPfordelEntityManagerAwareExtension.class)
class JournalføringHendelseHåndtererTest {

    @Mock
    private ProsessTaskTjeneste taskTjeneste;
    private DokumentRepository dokumentRepository;
    private JournalføringHendelseHåndterer hendelseHåndterer;

    @BeforeEach
    void setup(EntityManager em) {
        dokumentRepository = new DokumentRepository(em);
        hendelseHåndterer = new JournalføringHendelseHåndterer(taskTjeneste, dokumentRepository);
    }

    @Test
    void testSoknadEngangstonadOppretterKorrektTask(EntityManager em) {
        var builder = JournalfoeringHendelseRecord.newBuilder()
                .setHendelsesId("12345").setVersjon(1)
                .setHendelsesType("MidlertidigJournalført")
                .setTemaNytt(Tema.FORELDRE_OG_SVANGERSKAPSPENGER.getOffisiellKode()).setTemaGammelt("")
                .setBehandlingstema(BehandlingTema.ENGANGSSTØNAD_FØDSEL.getOffisiellKode())
                .setMottaksKanal(MottakKanal.SELVBETJENING.getKode())
                .setKanalReferanseId("minfil.pdf")
                .setJournalpostId(12345L)
                .setJournalpostStatus("M");

        hendelseHåndterer.handleMessage(builder.build());
        em.flush();

        var captor = ArgumentCaptor.forClass(ProsessTaskData.class);
        verify(taskTjeneste, times(1)).lagre(captor.capture());
        List<ProsessTaskData> result = captor.getAllValues();
        assertThat(result).as("Forventer at en prosesstask er lagt til").hasSize(1);

        ProsessTaskInfo prosessTaskData = result.get(0);
        assertThat(prosessTaskData.getTaskType()).as("Forventer at prosesstask av korrekt type blir opprettet. ")
                .isEqualToIgnoringCase("fordeling.hentFraJoark");

    }

    @Test
    void testSoknadUkjentTypeSendesLikevelTilNesteSteg(EntityManager em) {
        var builder = JournalfoeringHendelseRecord.newBuilder()
                .setHendelsesId("12345").setVersjon(1)
                .setHendelsesType("MidlertidigJournalført")
                .setTemaNytt(Tema.FORELDRE_OG_SVANGERSKAPSPENGER.getOffisiellKode()).setTemaGammelt("")
                .setMottaksKanal(MottakKanal.ALTINN.getKode())
                .setKanalReferanseId("AR325657")
                .setJournalpostId(12345L)
                .setJournalpostStatus("M");

        hendelseHåndterer.handleMessage(builder.build());
        em.flush();

        var captor = ArgumentCaptor.forClass(ProsessTaskData.class);
        verify(taskTjeneste, times(1)).lagre(captor.capture());
        List<ProsessTaskData> result = captor.getAllValues();
        assertThat(result).as("Forventer at en prosesstask er lagt til").hasSize(1);
        ProsessTaskInfo prosessTaskData = result.get(0);
        assertThat(prosessTaskData.getTaskType()).as("Forventer at prosesstask av korrekt type blir opprettet. ")
                .isEqualToIgnoringCase("fordeling.hentFraJoark");

    }

    @Test
    void testDokumentFraKloningUtsettes(EntityManager em) {
        var builder = JournalfoeringHendelseRecord.newBuilder()
                .setHendelsesId("12345").setVersjon(1)
                .setHendelsesType("MidlertidigJournalført")
                .setTemaNytt(Tema.FORELDRE_OG_SVANGERSKAPSPENGER.getOffisiellKode()).setTemaGammelt("")
                .setMottaksKanal(MottakKanal.HELSENETTET.getKode())
                .setKanalReferanseId("")
                .setJournalpostId(12345L)
                .setJournalpostStatus("M");

        hendelseHåndterer.handleMessage(builder.build());
        em.flush();

        var captor = ArgumentCaptor.forClass(ProsessTaskData.class);
        verify(taskTjeneste, times(1)).lagre(captor.capture());
        List<ProsessTaskData> result = captor.getAllValues();
        assertThat(result).as("Forventer at en prosesstask er lagt til").hasSize(1);
        ProsessTaskInfo prosessTaskData = result.get(0);
        assertThat(prosessTaskData.getNesteKjøringEtter()).isAfter(LocalDateTime.now().plusHours(1));
    }

    @Test
    void testDokumentFraEESSIIgnoreres(EntityManager em) {
        var builder = JournalfoeringHendelseRecord.newBuilder()
                .setHendelsesId("12345").setVersjon(1)
                .setHendelsesType("MidlertidigJournalført")
                .setTemaNytt(Tema.FORELDRE_OG_SVANGERSKAPSPENGER.getOffisiellKode()).setTemaGammelt("")
                .setMottaksKanal(MottakKanal.EESSI.getKode())
                .setKanalReferanseId("minfil.pdf")
                .setJournalpostId(12345L)
                .setJournalpostStatus("M");

        hendelseHåndterer.handleMessage(builder.build());
        em.flush();

        verifyNoInteractions(taskTjeneste);
    }

    @Test
    void test_0argCtor() {
        new JournalføringHendelseHåndterer();
    }
}
