package no.nav.foreldrepenger.journalføring.domene;

import no.nav.foreldrepenger.domene.BrukerId;
import no.nav.foreldrepenger.domene.YtelseType;
import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.journalføring.domene.oppgave.OppgaveEntitet;
import no.nav.foreldrepenger.journalføring.domene.oppgave.OppgaveRepository;
import no.nav.foreldrepenger.journalføring.domene.oppgave.Status;
import no.nav.vedtak.felles.integrasjon.oppgave.v1.*;
import no.nav.vedtak.felles.integrasjon.oppgave.v1.Oppgave;
import no.nav.vedtak.felles.integrasjon.oppgave.v1.Oppgavestatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OppgaverTjenesteTest {

    private JournalføringsOppgave oppgaver;
    @Mock
    private OppgaveRepository oppgaveRepository;
    @Mock
    private Oppgaver oppgaveKlient;

    @BeforeEach
    void setUp() {
        oppgaver = new OppgaverTjeneste(oppgaveRepository, oppgaveKlient, false);
    }

    @Test
    void opprettJournalføringsOppgaveGlobalt() {
        var oppgaveMock = mock(Oppgave.class);
        Long expectedId = 10L;
        when(oppgaveMock.id()).thenReturn(expectedId);
        when(oppgaveKlient.opprettetOppgave(any())).thenReturn(oppgaveMock);

        var id = oppgaver.opprettJournalføringsOppgave(
                "123456",
                "1234",
                "1234567890123",
                "referanse",
                BehandlingTema.FORELDREPENGER.getOffisiellKode(),
                "Test beskrivelse");

        assertEquals(expectedId.toString(), id);
        verifyNoInteractions(oppgaveRepository);
        verify(oppgaveKlient).opprettetOppgave(any(OpprettOppgave.class));
    }

    @Test
    void opprettJournalføringsOppgaveLokalt() {
        oppgaver = new OppgaverTjeneste(oppgaveRepository, oppgaveKlient, true);
        var expectedId = "11";
        when(oppgaveRepository.lagre(any(OppgaveEntitet.class))).thenReturn(expectedId);

        var id = oppgaver.opprettJournalføringsOppgave(
                "123456",
                "1234",
                "1234567890123",
                "referanse",
                BehandlingTema.FORELDREPENGER.getOffisiellKode(),
                "Test beskrivelse");

        assertEquals(expectedId, id);
        verifyNoInteractions(oppgaveKlient);
        verify(oppgaveRepository).lagre(any(OppgaveEntitet.class));
    }

    @Test
    void finnesÅpenJournalføringsoppgaveGlobalt() {
        var journalpostId = "1234";
        when(oppgaveRepository.harÅpenOppgave(journalpostId)).thenReturn(false);
        when(oppgaveKlient.finnÅpneJournalføringsoppgaverForJournalpost(journalpostId)).thenReturn(List.of(mock(Oppgave.class)));

        assertTrue(oppgaver.finnesÅpenJournalføringsoppgaveForJournalpost(journalpostId));
    }

    @Test
    void finnesÅpenJournalføringsoppgaveLokalt() {
        var journalpostId = "1234";
        when(oppgaveRepository.harÅpenOppgave(journalpostId)).thenReturn(true);

        assertTrue(oppgaver.finnesÅpenJournalføringsoppgaveForJournalpost(journalpostId));
        verifyNoInteractions(oppgaveKlient);
    }

    @Test
    void finnesÅpenJournalføringsoppgaveIngenOppgaverFunnet() {
        var journalpostId = "1234";
        when(oppgaveRepository.harÅpenOppgave(journalpostId)).thenReturn(false);
        when(oppgaveKlient.finnÅpneJournalføringsoppgaverForJournalpost(journalpostId)).thenReturn(List.of());
        assertFalse(oppgaver.finnesÅpenJournalføringsoppgaveForJournalpost(journalpostId));
    }

    @Test
    void ferdigstillÅpneJournalføringsOppgaverGlobalt() {
        var journalpostId = "1234";
        var oppgave1Mock = mock(Oppgave.class);
        when(oppgave1Mock.id()).thenReturn(1L);
        var oppgave2Mock = mock(Oppgave.class);
        when(oppgave2Mock.id()).thenReturn(2L);
        when(oppgaveKlient.finnÅpneJournalføringsoppgaverForJournalpost(journalpostId))
                .thenReturn(List.of(oppgave1Mock, oppgave2Mock));
        when(oppgaveRepository.harÅpenOppgave(journalpostId)).thenReturn(false);

        oppgaver.ferdigstillÅpneJournalføringsOppgaver(journalpostId);

        verify(oppgaveRepository).harÅpenOppgave(journalpostId);
        verify(oppgaveKlient, times(2)).ferdigstillOppgave(anyString());
        verifyNoMoreInteractions(oppgaveRepository, oppgaveKlient);
    }

    @Test
    void ferdigstillÅpneJournalføringsOppgaverLokalt() {
        var journalpostId = "1234";
        when(oppgaveKlient.finnÅpneJournalføringsoppgaverForJournalpost(journalpostId)).thenReturn(List.of());
        when(oppgaveRepository.harÅpenOppgave(journalpostId)).thenReturn(true);

        oppgaver.ferdigstillÅpneJournalføringsOppgaver(journalpostId);

        verify(oppgaveRepository).harÅpenOppgave(journalpostId);
        verify(oppgaveRepository).ferdigstillOppgave(journalpostId);
        verify(oppgaveKlient).finnÅpneJournalføringsoppgaverForJournalpost(journalpostId);
        verify(oppgaveKlient, never()).ferdigstillOppgave(anyString());
        verifyNoMoreInteractions(oppgaveRepository, oppgaveKlient);
    }

    @Test
    void hentOppgaveGlobalt() {
        var journalpostId = "1234";
        when(oppgaveRepository.harÅpenOppgave(journalpostId)).thenReturn(false);
        when(oppgaveKlient.hentOppgave(journalpostId)).thenReturn(gosysOppgave(journalpostId));

        var oppgave = oppgaver.hentOppgave(journalpostId);

        assertThat(oppgave.id()).isEqualTo(journalpostId);

        verify(oppgaveKlient).hentOppgave(journalpostId);
        verify(oppgaveRepository, never()).hentOppgave(journalpostId);
    }

    @Test
    void hentOppgaveLokalt() {
        var journalpostId = "1234";
        when(oppgaveRepository.harÅpenOppgave(journalpostId)).thenReturn(true);
        when(oppgaveRepository.hentOppgave(journalpostId)).thenReturn(lokalOppgave(journalpostId));

        var oppgave = oppgaver.hentOppgave(journalpostId);

        assertThat(oppgave.id()).isEqualTo(journalpostId);

        verify(oppgaveKlient, never()).hentOppgave(journalpostId);
        verify(oppgaveRepository).hentOppgave(journalpostId);
    }

    @Test
    void reserverOppgave() {
    }

    @Test
    void avreserverOppgave() {
    }

    @Test
    void finnÅpneOppgaverFor() {
    }

    @Test
    void mapTilYtelseType() {
    }

    private OppgaveEntitet lokalOppgave(String journalpostId) {
        return OppgaveEntitet.builder()
                .medJournalpostId(journalpostId)
                .medStatus(Status.AAPNET)
                .medBeskrivelse("test")
                .medEnhet("1234")
                .medBrukerId(new BrukerId("1234567890123"))
                .medFrist(LocalDate.now())
                .medYtelseType(YtelseType.FP)
                .medReservertAv("saksbehandler")
                .build();
    }

    private Oppgave gosysOppgave(String journalpostId) {
        return new Oppgave(Long.parseLong(journalpostId),
                journalpostId,
                null,
                "testRef",
                "1234567890123",
                null,
                null,
                Oppgavetype.JOURNALFØRING.getKode(),
                null,
                0,
                "1234",
                LocalDate.now(),
                LocalDate.now(),
                Prioritet.NORM,
                Oppgavestatus.AAPNET,
                "test beskrivelse",
                null
        );
    }
}
