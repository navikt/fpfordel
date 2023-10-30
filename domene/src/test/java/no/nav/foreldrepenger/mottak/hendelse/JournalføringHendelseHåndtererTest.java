package no.nav.foreldrepenger.mottak.hendelse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.LocalDateTime;
import java.util.List;

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
import no.nav.joarkjournalfoeringhendelser.JournalfoeringHendelseRecord;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskInfo;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;

@ExtendWith(MockitoExtension.class)
class JournalføringHendelseHåndtererTest {

    @Mock
    private ProsessTaskTjeneste taskTjeneste;
    @Mock
    private DokumentRepository dokumentRepository;
    private JournalføringHendelseHåndterer hendelseHåndterer;

    @BeforeEach
    void setup() {
        hendelseHåndterer = new JournalføringHendelseHåndterer(taskTjeneste, dokumentRepository, 2);
    }

    @Test
    void testSoknadEngangstonadOppretterKorrektTask() {
        var builder = JournalfoeringHendelseRecord.newBuilder()
            .setHendelsesId("12345")
            .setVersjon(1)
            .setHendelsesType("JournalpostMottatt")
            .setTemaNytt(Tema.FORELDRE_OG_SVANGERSKAPSPENGER.getOffisiellKode())
            .setTemaGammelt("")
            .setBehandlingstema(BehandlingTema.ENGANGSSTØNAD_FØDSEL.getOffisiellKode())
            .setMottaksKanal(MottakKanal.SELVBETJENING.getKode())
            .setKanalReferanseId("minfil.pdf")
            .setJournalpostId(12345L)
            .setJournalpostStatus("M");

        hendelseHåndterer.handleMessage(builder.build());

        var captor = ArgumentCaptor.forClass(ProsessTaskData.class);
        verify(taskTjeneste, times(1)).lagre(captor.capture());
        var result = captor.getAllValues();
        assertThat(result).as("Forventer at en prosesstask er lagt til").hasSize(1);

        ProsessTaskInfo prosessTaskData = result.get(0);
        assertThat(prosessTaskData.getTaskType()).as("Forventer at prosesstask av korrekt type blir opprettet. ")
            .isEqualToIgnoringCase("fordeling.hentFraJoark");

    }

    @Test
    void testSoknadUkjentTypeSendesLikevelTilNesteSteg() {
        var builder = JournalfoeringHendelseRecord.newBuilder()
            .setHendelsesId("12345")
            .setVersjon(1)
            .setHendelsesType("JournalpostMottatt")
            .setTemaNytt(Tema.FORELDRE_OG_SVANGERSKAPSPENGER.getOffisiellKode())
            .setTemaGammelt("")
            .setMottaksKanal(MottakKanal.ALTINN.getKode())
            .setKanalReferanseId("AR325657")
            .setJournalpostId(12345L)
            .setJournalpostStatus("M");

        hendelseHåndterer.handleMessage(builder.build());

        var captor = ArgumentCaptor.forClass(ProsessTaskData.class);
        verify(taskTjeneste, times(1)).lagre(captor.capture());
        var result = captor.getAllValues();
        assertThat(result).as("Forventer at en prosesstask er lagt til").hasSize(1);
        ProsessTaskInfo prosessTaskData = result.get(0);
        assertThat(prosessTaskData.getTaskType()).as("Forventer at prosesstask av korrekt type blir opprettet. ")
            .isEqualToIgnoringCase("fordeling.hentFraJoark");

    }

    @Test
    void testDokumentFraKloningUtsettes() {
        var builder = JournalfoeringHendelseRecord.newBuilder()
            .setHendelsesId("12345")
            .setVersjon(1)
            .setHendelsesType("JournalpostMottatt")
            .setTemaNytt(Tema.FORELDRE_OG_SVANGERSKAPSPENGER.getOffisiellKode())
            .setTemaGammelt("")
            .setMottaksKanal(MottakKanal.HELSENETTET.getKode())
            .setKanalReferanseId("")
            .setJournalpostId(12345L)
            .setJournalpostStatus("M");

        hendelseHåndterer.handleMessage(builder.build());

        var captor = ArgumentCaptor.forClass(ProsessTaskData.class);
        verify(taskTjeneste, times(1)).lagre(captor.capture());
        var result = captor.getAllValues();
        assertThat(result).as("Forventer at en prosesstask er lagt til").hasSize(1);
        ProsessTaskInfo prosessTaskData = result.get(0);
        assertThat(prosessTaskData.getNesteKjøringEtter()).isAfter(LocalDateTime.now().plusHours(1));
    }

    @Test
    void testDokumentFraEESSIIgnoreres() {
        var builder = JournalfoeringHendelseRecord.newBuilder()
            .setHendelsesId("12345")
            .setVersjon(1)
            .setHendelsesType("JournalpostMottatt")
            .setTemaNytt(Tema.FORELDRE_OG_SVANGERSKAPSPENGER.getOffisiellKode())
            .setTemaGammelt("")
            .setMottaksKanal(MottakKanal.EESSI.getKode())
            .setKanalReferanseId("minfil.pdf")
            .setJournalpostId(12345L)
            .setJournalpostStatus("M");

        hendelseHåndterer.handleMessage(builder.build());

        verifyNoInteractions(taskTjeneste);
    }

    @Test
    void test_0argCtor() {
        assertThat(new JournalføringHendelseHåndterer()).isNotNull();
    }
}
