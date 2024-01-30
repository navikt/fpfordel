package no.nav.foreldrepenger.fordel.web.app.forvaltning.migrering;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.foreldrepenger.fordel.kodeverdi.Journalstatus;
import no.nav.foreldrepenger.fordel.kodeverdi.MottakKanal;
import no.nav.foreldrepenger.journalføring.oppgave.lager.OppgaveEntitet;
import no.nav.foreldrepenger.journalføring.oppgave.lager.OppgaveRepository;
import no.nav.foreldrepenger.journalføring.oppgave.lager.Status;
import no.nav.foreldrepenger.journalføring.oppgave.lager.YtelseType;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentRepository;
import no.nav.foreldrepenger.mottak.domene.dokument.Journalpost;
import no.nav.vedtak.mapper.json.DefaultJsonMapper;

@ExtendWith(MockitoExtension.class)
public class MigreringRestTest {

    private static final String JOURNALPOST_ID = "123456789";


    @Mock
    private DokumentRepository dokumentRepository;
    @Mock
    private OppgaveRepository oppgaveRepository;


    @Test
    void roundtrip_oppgaveDto() {
        // Arrange
        var beskrivelse = "Dette er en beskrivelse med navn og (dato)";
        var oppgave = OppgaveEntitet.builder()
            .medBeskrivelse(beskrivelse)
            .medJournalpostId(JOURNALPOST_ID)
            .medBrukerId("1111111111111")
            .medEnhet("4444")
            .medFrist(LocalDate.now().plusDays(1))
            .medStatus(Status.AAPNET)
            .medYtelseType(YtelseType.FP)
            .build();
        when(oppgaveRepository.hentAlleÅpneOppgaver()).thenReturn(List.of(oppgave));

        var forvaltning = new MigreringRestTjeneste(dokumentRepository, oppgaveRepository);
        var dtos = (MigreringOppgaveDto) (forvaltning.lesOppgaver().getEntity());
        var serializedDtos = DefaultJsonMapper.toJson(dtos);
        var deserDtos = DefaultJsonMapper.fromJson(serializedDtos, MigreringOppgaveDto.class);
        var deserInngående = forvaltning.lagreOppgaver(deserDtos);

        var hendelseCaptor = ArgumentCaptor.forClass(OppgaveEntitet.class);
        verify(oppgaveRepository, times(1)).lagre(hendelseCaptor.capture());

        var lagretInn = hendelseCaptor.getValue();
        assertThat(lagretInn.getJournalpostId()).isEqualTo(JOURNALPOST_ID);
        assertThat(lagretInn.getEnhet()).isEqualTo("4444");
        assertThat(lagretInn.getFrist()).isEqualTo(LocalDate.now().plusDays(1));
        assertThat(lagretInn.getBeskrivelse()).isEqualTo(beskrivelse);
        assertThat(lagretInn.getStatus()).isEqualTo(Status.AAPNET);
        assertThat(lagretInn.getYtelseType()).isEqualTo(YtelseType.FP);

    }


    @Test
    void roundtrip_journalpostDto() {
        // Arrange
        var ref = UUID.randomUUID().toString();
        var journalpost = new Journalpost(JOURNALPOST_ID, Journalstatus.MOTTATT.name(), MottakKanal.ALTINN.name(), ref, "FORDEL");
        when(dokumentRepository.hentAlleJournalposter()).thenReturn(List.of(journalpost));

        var forvaltning = new MigreringRestTjeneste(dokumentRepository, oppgaveRepository);
        var dtos = (MigreringJournalpostDto) (forvaltning.lesJournal().getEntity());
        var serializedDtos = DefaultJsonMapper.toJson(dtos);
        var deserDtos = DefaultJsonMapper.fromJson(serializedDtos, MigreringJournalpostDto.class);
        var deserInngående = forvaltning.lagreJournal(deserDtos);

        var hendelseCaptor = ArgumentCaptor.forClass(Object.class);
        verify(dokumentRepository, times(1)).lagre(hendelseCaptor.capture());

        var lagretInn = (Journalpost) hendelseCaptor.getValue();
        assertThat(lagretInn.getJournalpostId()).isEqualTo(JOURNALPOST_ID);
        assertThat(lagretInn.getTilstand()).isEqualTo("MOTTATT");
        assertThat(lagretInn.getKanal()).isEqualTo("ALTINN");
        assertThat(lagretInn.getReferanse()).isEqualTo(ref);
        assertThat(lagretInn.getOpprettetAv()).isEqualTo("FORDEL");

    }

}
