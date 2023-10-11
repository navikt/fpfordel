package no.nav.foreldrepenger.journalføring.oppgave;

import static no.nav.foreldrepenger.journalføring.oppgave.OppgaverTjeneste.LIMIT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.journalføring.domene.JournalpostId;
import no.nav.foreldrepenger.journalføring.oppgave.domene.NyOppgave;
import no.nav.foreldrepenger.journalføring.oppgave.lager.AktørId;
import no.nav.foreldrepenger.journalføring.oppgave.lager.OppgaveEntitet;
import no.nav.foreldrepenger.journalføring.oppgave.lager.OppgaveRepository;
import no.nav.foreldrepenger.journalføring.oppgave.lager.Status;
import no.nav.foreldrepenger.journalføring.oppgave.lager.YtelseType;
import no.nav.vedtak.felles.integrasjon.oppgave.v1.Oppgave;
import no.nav.vedtak.felles.integrasjon.oppgave.v1.Oppgaver;
import no.nav.vedtak.felles.integrasjon.oppgave.v1.Oppgavestatus;
import no.nav.vedtak.felles.integrasjon.oppgave.v1.Oppgavetype;
import no.nav.vedtak.felles.integrasjon.oppgave.v1.OpprettOppgave;
import no.nav.vedtak.felles.integrasjon.oppgave.v1.Prioritet;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;

@ExtendWith(MockitoExtension.class)
class OppgaverTjenesteTest {

    private Journalføringsoppgave oppgaver;
    @Mock
    private OppgaveRepository oppgaveRepository;
    @Mock
    private Oppgaver oppgaveKlient;

    @BeforeEach
    void setUp() {
        oppgaver = new OppgaverTjeneste(oppgaveRepository, oppgaveKlient, mock(ProsessTaskTjeneste.class));
    }

    @Test
    void opprettJournalføringsOppgaveGlobalt() {
        var oppgaveMock = mock(Oppgave.class);
        Long expectedId = 10L;
        when(oppgaveMock.id()).thenReturn(expectedId);
        when(oppgaveKlient.opprettetOppgave(any())).thenReturn(oppgaveMock);

        var id = oppgaver.opprettGosysJournalføringsoppgaveFor(NyOppgave.builder()
            .medJournalpostId(JournalpostId.fra("123456"))
            .medEnhetId("1234")
            .medAktørId(new AktørId("1234567890123"))
            .medSaksref("referanse")
            .medBehandlingTema(BehandlingTema.SVANGERSKAPSPENGER)
            .medBeskrivelse("Test beskrivelse")
            .build());

        assertEquals(expectedId.toString(), id);
        verifyNoInteractions(oppgaveRepository);
        verify(oppgaveKlient).opprettetOppgave(any(OpprettOppgave.class));
    }

    @Test
    void opprettJournalføringsOppgaveLokalt() {
        oppgaver = new OppgaverTjeneste(oppgaveRepository, oppgaveKlient, mock(ProsessTaskTjeneste.class));
        var expectedId = "11";
        when(oppgaveRepository.lagre(any(OppgaveEntitet.class))).thenReturn(expectedId);

        var id = oppgaver.opprettJournalføringsoppgaveFor(NyOppgave.builder().medJournalpostId(JournalpostId.fra("123456"))
            .medEnhetId("1234")
            .medAktørId(new AktørId("1234567890123"))
            .medSaksref("referanse")
            .medBehandlingTema(BehandlingTema.FORELDREPENGER)
            .medBeskrivelse("Test beskrivelse")
            .build());

        assertEquals(expectedId, id);

        var argumentsCaptor = ArgumentCaptor.forClass(OppgaveEntitet.class);
        verify(oppgaveRepository).lagre(argumentsCaptor.capture());
        assertThat(argumentsCaptor.getValue().getYtelseType()).isEqualTo(YtelseType.FP);

        verifyNoInteractions(oppgaveKlient);
    }

    @Test
    void finnesÅpenJournalføringsoppgaveGlobalt() {
        var journalpostId = "1234";
        when(oppgaveRepository.harÅpenOppgave(journalpostId)).thenReturn(false);
        when(oppgaveKlient.finnÅpneJournalføringsoppgaverForJournalpost(journalpostId)).thenReturn(List.of(mock(Oppgave.class)));

        assertTrue(oppgaver.finnesÅpeneJournalføringsoppgaverFor(JournalpostId.fra(journalpostId)));
    }

    @Test
    void finnesÅpenJournalføringsoppgaveLokalt() {
        var journalpostId = "1234";
        when(oppgaveRepository.harÅpenOppgave(journalpostId)).thenReturn(true);

        assertTrue(oppgaver.finnesÅpeneJournalføringsoppgaverFor(JournalpostId.fra(journalpostId)));
        verifyNoInteractions(oppgaveKlient);
    }

    @Test
    void finnesÅpenJournalføringsoppgaveIngenOppgaverFunnet() {
        var journalpostId = "1234";
        when(oppgaveRepository.harÅpenOppgave(journalpostId)).thenReturn(false);
        when(oppgaveKlient.finnÅpneJournalføringsoppgaverForJournalpost(journalpostId)).thenReturn(List.of());
        assertFalse(oppgaver.finnesÅpeneJournalføringsoppgaverFor(JournalpostId.fra(journalpostId)));
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

        oppgaver.ferdigstillAlleÅpneJournalføringsoppgaverFor(JournalpostId.fra(journalpostId));

        verify(oppgaveRepository).harÅpenOppgave(journalpostId);
        verify(oppgaveKlient, times(2)).ferdigstillOppgave(anyString());
        verifyNoMoreInteractions(oppgaveRepository, oppgaveKlient);
    }

    @Test
    void ferdigstillÅpneJournalføringsOppgaverLokalt() {
        var journalpostId = "1234";
        when(oppgaveKlient.finnÅpneJournalføringsoppgaverForJournalpost(journalpostId)).thenReturn(List.of());
        when(oppgaveRepository.harÅpenOppgave(journalpostId)).thenReturn(true);

        oppgaver.ferdigstillAlleÅpneJournalføringsoppgaverFor(JournalpostId.fra(journalpostId));

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
        when(oppgaveKlient.finnÅpneJournalføringsoppgaverForJournalpost(journalpostId)).thenReturn(List.of(gosysOppgave(journalpostId)));

        var oppgave = oppgaver.hentOppgaveFor(JournalpostId.fra(journalpostId));

        assertThat(oppgave.journalpostId()).isEqualTo(journalpostId);

        verify(oppgaveKlient).finnÅpneJournalføringsoppgaverForJournalpost(journalpostId);
        verify(oppgaveRepository, never()).hentOppgave(journalpostId);
    }

    @Test
    void hentOppgaveLokalt() {
        var journalpostId = "1234";
        when(oppgaveRepository.harÅpenOppgave(journalpostId)).thenReturn(true);
        when(oppgaveRepository.hentOppgave(journalpostId)).thenReturn(lokalOppgave(journalpostId));

        var oppgave = oppgaver.hentOppgaveFor(JournalpostId.fra(journalpostId));

        assertThat(oppgave.journalpostId()).isEqualTo(journalpostId);

        verify(oppgaveKlient, never()).hentOppgave(journalpostId);
        verify(oppgaveRepository).hentOppgave(journalpostId);
    }

    @Test
    void reserverOppgaveGosys() {
        var gosysOppgaveId = "5678";
        var journalpostId = "1234";
        var saksbehandler = "TestIdent";
        var oppgave = no.nav.foreldrepenger.journalføring.oppgave.domene.Oppgave.builder()
            .medJournalpostId(journalpostId)
            .medOppgaveId(gosysOppgaveId)
            .medKilde(no.nav.foreldrepenger.journalføring.oppgave.domene.Oppgave.Kilde.GOSYS)
            .medAktivDato(LocalDate.now()).medTildeltEnhetsnr("4867")
            .medStatus(no.nav.foreldrepenger.journalføring.oppgave.domene.Oppgavestatus.AAPNET)
            .medFristFerdigstillelse(LocalDate.now().plusDays(1))
            .medBeskrivelse("tom")
            .build();
        when(oppgaveRepository.harÅpenOppgave(gosysOppgaveId)).thenReturn(false);

        oppgaver.reserverOppgaveFor(oppgave, saksbehandler);

        verify(oppgaveKlient).reserverOppgave(gosysOppgaveId, saksbehandler);
        verifyNoMoreInteractions(oppgaveRepository);
    }

    @Test
    void reserverOppgaveLokalt() {
        var journalpostId = "1234";
        var saksbehandler = "TestIdent";
        var oppgave = no.nav.foreldrepenger.journalføring.oppgave.domene.Oppgave.builder()
            .medJournalpostId(journalpostId)
            .medOppgaveId(journalpostId)
            .medKilde(no.nav.foreldrepenger.journalføring.oppgave.domene.Oppgave.Kilde.LOKAL)
            .medAktivDato(LocalDate.now()).medTildeltEnhetsnr("4867")
            .medStatus(no.nav.foreldrepenger.journalføring.oppgave.domene.Oppgavestatus.AAPNET)
            .medFristFerdigstillelse(LocalDate.now().plusDays(1))
            .medBeskrivelse("tom")
            .build();
        when(oppgaveRepository.harÅpenOppgave(journalpostId)).thenReturn(true);
        when(oppgaveRepository.hentOppgave(journalpostId)).thenReturn(lokalOppgave(journalpostId));
        var argumentCaptor = ArgumentCaptor.forClass(OppgaveEntitet.class);

        oppgaver.reserverOppgaveFor(oppgave, saksbehandler);

        verify(oppgaveRepository).lagre(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().getReservertAv()).isEqualTo(saksbehandler);
        verifyNoMoreInteractions(oppgaveRepository);
        verifyNoInteractions(oppgaveKlient);
    }

    @Test
    void avreserverOppgaveGosys() {
        var gosysOppgaveId = "5678";
        var journalpostId = "1234";
        var oppgave = no.nav.foreldrepenger.journalføring.oppgave.domene.Oppgave.builder()
            .medJournalpostId(journalpostId)
            .medOppgaveId(gosysOppgaveId)
            .medKilde(no.nav.foreldrepenger.journalføring.oppgave.domene.Oppgave.Kilde.GOSYS)
            .medAktivDato(LocalDate.now()).medTildeltEnhetsnr("4867")
            .medStatus(no.nav.foreldrepenger.journalføring.oppgave.domene.Oppgavestatus.AAPNET)
            .medFristFerdigstillelse(LocalDate.now().plusDays(1))
            .medBeskrivelse("tom")
            .build();
        when(oppgaveRepository.harÅpenOppgave(gosysOppgaveId)).thenReturn(false);

        oppgaver.avreserverOppgaveFor(oppgave);

        verify(oppgaveKlient).avreserverOppgave(gosysOppgaveId);
        verifyNoMoreInteractions(oppgaveRepository);
    }

    @Test
    void avreserverOppgaveLokalt() {
        var journalpostId = "1234";
        var oppgave = no.nav.foreldrepenger.journalføring.oppgave.domene.Oppgave.builder()
            .medJournalpostId(journalpostId)
            .medOppgaveId(journalpostId)
            .medKilde(no.nav.foreldrepenger.journalføring.oppgave.domene.Oppgave.Kilde.LOKAL)
            .medAktivDato(LocalDate.now()).medTildeltEnhetsnr("4867")
            .medStatus(no.nav.foreldrepenger.journalføring.oppgave.domene.Oppgavestatus.AAPNET)
            .medFristFerdigstillelse(LocalDate.now().plusDays(1))
            .medBeskrivelse("tom")
            .build();
        when(oppgaveRepository.harÅpenOppgave(journalpostId)).thenReturn(true);
        when(oppgaveRepository.hentOppgave(journalpostId)).thenReturn(lokalOppgave(journalpostId));
        var argumentCaptor = ArgumentCaptor.forClass(OppgaveEntitet.class);

        oppgaver.avreserverOppgaveFor(oppgave);

        verify(oppgaveRepository).lagre(argumentCaptor.capture());
        assertNull(argumentCaptor.getValue().getReservertAv());
        verifyNoMoreInteractions(oppgaveRepository);
        verifyNoInteractions(oppgaveKlient);
    }

    @Test
    void finnAlleÅpneOppgaver() {
        var alleOppgaver = oppgaver.finnÅpneOppgaverFor(Set.of());

        assertThat(alleOppgaver).isEmpty();

        verify(oppgaveRepository).hentAlleÅpneOppgaver();
        verify(oppgaveKlient).finnÅpneOppgaverAvType(Oppgavetype.JOURNALFØRING, null, null, LIMIT);
        verifyNoMoreInteractions(oppgaveRepository, oppgaveKlient);
    }

    @Test
    void finnAlleÅpneOppgaverForEnhet() {
        var enhet = "1234";
        var alleOppgaver = oppgaver.finnÅpneOppgaverFor(Set.of(enhet));

        assertThat(alleOppgaver).isEmpty();

        verify(oppgaveRepository).hentÅpneOppgaverFor(enhet);
        verify(oppgaveKlient).finnÅpneOppgaverAvType(Oppgavetype.JOURNALFØRING, null, enhet, LIMIT);
        verifyNoMoreInteractions(oppgaveRepository, oppgaveKlient);
    }

    @Test
    void finnAlleÅpneOppgaverForToEnhet() {
        var enhet = "1234";
        var enhet2 = "4321";
        var alleOppgaver = oppgaver.finnÅpneOppgaverFor(Set.of(enhet, enhet2));

        assertThat(alleOppgaver).isEmpty();

        verify(oppgaveRepository).hentÅpneOppgaverFor(enhet);
        verify(oppgaveRepository).hentÅpneOppgaverFor(enhet2);
        verify(oppgaveKlient).finnÅpneOppgaverAvType(eq(Oppgavetype.JOURNALFØRING), isNull(), eq(enhet), eq(LIMIT));
        verify(oppgaveKlient).finnÅpneOppgaverAvType(eq(Oppgavetype.JOURNALFØRING), isNull(), eq(enhet2), eq(LIMIT));
        verifyNoMoreInteractions(oppgaveRepository, oppgaveKlient);
    }

    @Test
    void finnFaktiskÅpneOppgaver() {
        when(oppgaveRepository.hentAlleÅpneOppgaver()).thenReturn(List.of(lokalOppgave("1234567")));
        when(oppgaveKlient.finnÅpneOppgaverAvType(Oppgavetype.JOURNALFØRING, null, null, LIMIT))
                .thenReturn(List.of(gosysOppgave("76543231")));

        var alleOppgaver = oppgaver.finnÅpneOppgaverFor(Set.of());

        assertThat(alleOppgaver).isNotEmpty().hasSize(2);

        verify(oppgaveRepository).hentAlleÅpneOppgaver();
        verify(oppgaveKlient).finnÅpneOppgaverAvType(Oppgavetype.JOURNALFØRING, null, null, LIMIT);
        verifyNoMoreInteractions(oppgaveRepository, oppgaveKlient);
    }

    private OppgaveEntitet lokalOppgave(String journalpostId) {
        return OppgaveEntitet.builder()
                .medJournalpostId(journalpostId)
                .medStatus(Status.AAPNET)
                .medBeskrivelse("test")
                .medEnhet("1234")
                .medBrukerId("1234567890123")
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
                "4321",
                LocalDate.now(),
                LocalDate.now(),
                Prioritet.NORM,
                Oppgavestatus.AAPNET,
                "test beskrivelse",
                null
        );
    }
}
