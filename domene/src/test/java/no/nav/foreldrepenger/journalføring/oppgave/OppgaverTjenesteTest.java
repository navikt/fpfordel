package no.nav.foreldrepenger.journalføring.oppgave;

import static no.nav.foreldrepenger.journalføring.oppgave.OppgaverTjeneste.LIMIT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
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
import no.nav.foreldrepenger.mottak.behandlendeenhet.AnsattInfoKlient;
import no.nav.foreldrepenger.mottak.behandlendeenhet.EnhetsTjeneste;
import no.nav.foreldrepenger.mottak.behandlendeenhet.FilterKlient;
import no.nav.foreldrepenger.mottak.person.PersonTjeneste;
import no.nav.vedtak.felles.integrasjon.oppgave.v1.Oppgave;
import no.nav.vedtak.felles.integrasjon.oppgave.v1.Oppgaver;
import no.nav.vedtak.felles.integrasjon.oppgave.v1.Oppgavestatus;
import no.nav.vedtak.felles.integrasjon.oppgave.v1.Oppgavetype;
import no.nav.vedtak.felles.integrasjon.oppgave.v1.OpprettOppgave;
import no.nav.vedtak.felles.integrasjon.oppgave.v1.Prioritet;
import no.nav.vedtak.sikkerhet.kontekst.AnsattGruppe;

@ExtendWith(MockitoExtension.class)
class OppgaverTjenesteTest {

    private static final String VANLIG_ENHETSNR = "4321";
    private static final String VANLIG_JOURNALPOSTID = "1234";
    protected static final String AKTØR_ID = "1234567890123";

    private Journalføringsoppgave oppgaver;
    @Mock
    private OppgaveRepository oppgaveRepository;
    @Mock
    private Oppgaver oppgaveKlient;
    @Mock
    private EnhetsTjeneste enhetsTjeneste;
    @Mock
    private FilterKlient filterKlient;
    @Mock
    private AnsattInfoKlient ansattInfoKlient;
    @Mock
    private PersonTjeneste personTjeneste;

    @BeforeEach
    void setUp() {
        oppgaver = new OppgaverTjeneste(oppgaveRepository, oppgaveKlient, enhetsTjeneste, personTjeneste, filterKlient, ansattInfoKlient);
    }

    @Test
    void opprettJournalføringsOppgaveGlobalt() {
        var oppgaveMock = mock(Oppgave.class);
        Long expectedId = 10L;
        when(oppgaveMock.id()).thenReturn(expectedId);
        when(oppgaveKlient.opprettetOppgave(any())).thenReturn(oppgaveMock);

        var id = oppgaver.opprettGosysJournalføringsoppgaveFor(NyOppgave.builder()
            .medJournalpostId(JournalpostId.fra("123456"))
            .medEnhetId(VANLIG_ENHETSNR)
            .medAktørId(new AktørId(AKTØR_ID))
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
        oppgaver = new OppgaverTjeneste(oppgaveRepository, oppgaveKlient, enhetsTjeneste, personTjeneste, filterKlient, ansattInfoKlient);
        var expectedId = "11";
        when(oppgaveRepository.lagre(any(OppgaveEntitet.class))).thenReturn(expectedId);

        var id = oppgaver.opprettJournalføringsoppgaveFor(NyOppgave.builder()
            .medJournalpostId(JournalpostId.fra("123456"))
            .medEnhetId(VANLIG_ENHETSNR)
            .medAktørId(new AktørId(AKTØR_ID))
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
        var journalpostId = VANLIG_JOURNALPOSTID;
        when(oppgaveRepository.harÅpenOppgave(journalpostId)).thenReturn(false);
        when(oppgaveKlient.finnÅpneJournalføringsoppgaverForJournalpost(journalpostId)).thenReturn(List.of(mock(Oppgave.class)));

        assertTrue(oppgaver.finnesÅpeneJournalføringsoppgaverFor(JournalpostId.fra(journalpostId)));
    }

    @Test
    void finnesÅpenJournalføringsoppgaveLokalt() {
        var journalpostId = VANLIG_JOURNALPOSTID;
        when(oppgaveRepository.harÅpenOppgave(journalpostId)).thenReturn(true);

        assertTrue(oppgaver.finnesÅpeneJournalføringsoppgaverFor(JournalpostId.fra(journalpostId)));
        verifyNoInteractions(oppgaveKlient);
    }

    @Test
    void finnesÅpenJournalføringsoppgaveIngenOppgaverFunnet() {
        var journalpostId = VANLIG_JOURNALPOSTID;
        when(oppgaveRepository.harÅpenOppgave(journalpostId)).thenReturn(false);
        when(oppgaveKlient.finnÅpneJournalføringsoppgaverForJournalpost(journalpostId)).thenReturn(List.of());
        assertFalse(oppgaver.finnesÅpeneJournalføringsoppgaverFor(JournalpostId.fra(journalpostId)));
    }

    @Test
    void ferdigstillÅpneJournalføringsOppgaverGlobalt() {
        var journalpostId = VANLIG_JOURNALPOSTID;
        var oppgave1Mock = mock(Oppgave.class);
        when(oppgave1Mock.id()).thenReturn(1L);
        var oppgave2Mock = mock(Oppgave.class);
        when(oppgave2Mock.id()).thenReturn(2L);
        when(oppgaveKlient.finnÅpneJournalføringsoppgaverForJournalpost(journalpostId)).thenReturn(List.of(oppgave1Mock, oppgave2Mock));
        when(oppgaveRepository.harÅpenOppgave(journalpostId)).thenReturn(false);

        oppgaver.ferdigstillAlleÅpneJournalføringsoppgaverFor(JournalpostId.fra(journalpostId));

        verify(oppgaveRepository).harÅpenOppgave(journalpostId);
        verify(oppgaveKlient, times(2)).ferdigstillOppgave(anyString());
        verifyNoMoreInteractions(oppgaveRepository, oppgaveKlient);
    }

    @Test
    void ferdigstillÅpneJournalføringsOppgaverLokalt() {
        var journalpostId = VANLIG_JOURNALPOSTID;
        when(oppgaveKlient.finnÅpneJournalføringsoppgaverForJournalpost(journalpostId)).thenReturn(List.of());
        when(oppgaveRepository.harÅpenOppgave(journalpostId)).thenReturn(true);

        oppgaver.ferdigstillAlleÅpneJournalføringsoppgaverFor(JournalpostId.fra(journalpostId));

        verify(oppgaveRepository).harÅpenOppgave(journalpostId);
        verify(oppgaveRepository).avsluttOppgaveMedStatus(journalpostId, Status.FERDIGSTILT);
        verify(oppgaveKlient).finnÅpneJournalføringsoppgaverForJournalpost(journalpostId);
        verify(oppgaveKlient, never()).ferdigstillOppgave(anyString());
        verifyNoMoreInteractions(oppgaveRepository, oppgaveKlient);
    }

    @Test
    void hentOppgaveGlobalt() {
        var journalpostId = VANLIG_JOURNALPOSTID;
        when(oppgaveRepository.harÅpenOppgave(journalpostId)).thenReturn(false);
        when(oppgaveKlient.finnÅpneJournalføringsoppgaverForJournalpost(journalpostId)).thenReturn(List.of(gosysOppgave(journalpostId)));

        var oppgave = oppgaver.hentOppgaveFor(JournalpostId.fra(journalpostId));

        assertThat(oppgave.journalpostId()).isEqualTo(journalpostId);

        verify(oppgaveKlient).finnÅpneJournalføringsoppgaverForJournalpost(journalpostId);
        verify(oppgaveRepository, never()).hentOppgave(journalpostId);
    }

    @Test
    void hentOppgaveLokalt() {
        var journalpostId = VANLIG_JOURNALPOSTID;
        when(oppgaveRepository.harÅpenOppgave(journalpostId)).thenReturn(true);
        when(oppgaveRepository.hentOppgave(journalpostId)).thenReturn(lokalÅpnetOppgave(journalpostId, VANLIG_ENHETSNR));

        var oppgave = oppgaver.hentOppgaveFor(JournalpostId.fra(journalpostId));

        assertThat(oppgave.journalpostId()).isEqualTo(journalpostId);

        verify(oppgaveKlient, never()).hentOppgave(journalpostId);
        verify(oppgaveRepository).hentOppgave(journalpostId);
    }

    @Test
    void reserverOppgaveGosys() {
        var gosysOppgaveId = "5678";
        var journalpostId = VANLIG_JOURNALPOSTID;
        var saksbehandler = "TestIdent";
        var oppgave = no.nav.foreldrepenger.journalføring.oppgave.domene.Oppgave.builder()
            .medJournalpostId(journalpostId)
            .medOppgaveId(gosysOppgaveId)
            .medKilde(no.nav.foreldrepenger.journalføring.oppgave.domene.Oppgave.Kilde.GOSYS)
            .medAktivDato(LocalDate.now())
            .medTildeltEnhetsnr("4867")
            .medStatus(no.nav.foreldrepenger.journalføring.oppgave.domene.Oppgavestatus.AAPNET)
            .medFristFerdigstillelse(LocalDate.now().plusDays(1))
            .medBeskrivelse("tom")
            .build();
        when(oppgaveRepository.harÅpenOppgave(journalpostId)).thenReturn(false);

        oppgaver.reserverOppgaveFor(oppgave, saksbehandler);

        verify(oppgaveKlient).reserverOppgave(gosysOppgaveId, saksbehandler);
        verifyNoMoreInteractions(oppgaveRepository);
    }

    @Test
    void reserverOppgaveLokalt() {
        var journalpostId = VANLIG_JOURNALPOSTID;
        var saksbehandler = "TestIdent";
        var oppgave = no.nav.foreldrepenger.journalføring.oppgave.domene.Oppgave.builder()
            .medJournalpostId(journalpostId)
            .medOppgaveId(journalpostId)
            .medKilde(no.nav.foreldrepenger.journalføring.oppgave.domene.Oppgave.Kilde.LOKAL)
            .medAktivDato(LocalDate.now())
            .medTildeltEnhetsnr("4867")
            .medStatus(no.nav.foreldrepenger.journalføring.oppgave.domene.Oppgavestatus.AAPNET)
            .medFristFerdigstillelse(LocalDate.now().plusDays(1))
            .medBeskrivelse("tom")
            .build();
        when(oppgaveRepository.harÅpenOppgave(journalpostId)).thenReturn(true);
        when(oppgaveRepository.hentOppgave(journalpostId)).thenReturn(lokalÅpnetOppgave(journalpostId, VANLIG_ENHETSNR));
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
        var journalpostId = VANLIG_JOURNALPOSTID;
        var oppgave = no.nav.foreldrepenger.journalføring.oppgave.domene.Oppgave.builder()
            .medJournalpostId(journalpostId)
            .medOppgaveId(gosysOppgaveId)
            .medKilde(no.nav.foreldrepenger.journalføring.oppgave.domene.Oppgave.Kilde.GOSYS)
            .medAktivDato(LocalDate.now())
            .medTildeltEnhetsnr("4867")
            .medStatus(no.nav.foreldrepenger.journalføring.oppgave.domene.Oppgavestatus.AAPNET)
            .medFristFerdigstillelse(LocalDate.now().plusDays(1))
            .medBeskrivelse("tom")
            .build();
        when(oppgaveRepository.harÅpenOppgave(journalpostId)).thenReturn(false);

        oppgaver.avreserverOppgaveFor(oppgave);

        verify(oppgaveKlient).avreserverOppgave(gosysOppgaveId);
        verifyNoMoreInteractions(oppgaveRepository);
    }

    @Test
    void avreserverOppgaveLokalt() {
        var journalpostId = VANLIG_JOURNALPOSTID;
        var oppgave = no.nav.foreldrepenger.journalføring.oppgave.domene.Oppgave.builder()
            .medJournalpostId(journalpostId)
            .medOppgaveId(journalpostId)
            .medKilde(no.nav.foreldrepenger.journalføring.oppgave.domene.Oppgave.Kilde.LOKAL)
            .medAktivDato(LocalDate.now())
            .medTildeltEnhetsnr("4867")
            .medStatus(no.nav.foreldrepenger.journalføring.oppgave.domene.Oppgavestatus.AAPNET)
            .medFristFerdigstillelse(LocalDate.now().plusDays(1))
            .medBeskrivelse("tom")
            .build();
        when(oppgaveRepository.harÅpenOppgave(journalpostId)).thenReturn(true);
        when(oppgaveRepository.hentOppgave(journalpostId)).thenReturn(lokalÅpnetOppgave(journalpostId, VANLIG_ENHETSNR));
        var argumentCaptor = ArgumentCaptor.forClass(OppgaveEntitet.class);

        oppgaver.avreserverOppgaveFor(oppgave);

        verify(oppgaveRepository).lagre(argumentCaptor.capture());
        assertNull(argumentCaptor.getValue().getReservertAv());
        verifyNoMoreInteractions(oppgaveRepository);
        verifyNoInteractions(oppgaveKlient);
    }

    @Test
    void finnAlleÅpneOppgaver() {
        when(filterKlient.filterIdenter(any())).thenReturn(Set.of());

        var alleOppgaver = oppgaver.finnÅpneOppgaverFiltrert();

        assertThat(alleOppgaver).isEmpty();

        verify(oppgaveRepository).hentAlleÅpneOppgaver();
        verify(oppgaveKlient).finnÅpneOppgaverAvType(Oppgavetype.JOURNALFØRING, null, null, LIMIT);
    }

    @Test
    void finnAlleÅpneOppgaverForToEnhet() {
        when(filterKlient.filterIdenter(any())).thenReturn(Set.of());

        var alleOppgaver = oppgaver.finnÅpneOppgaverFiltrert();

        assertThat(alleOppgaver).isEmpty();

        verify(oppgaveRepository).hentAlleÅpneOppgaver();
        verify(oppgaveKlient).finnÅpneOppgaverAvType(eq(Oppgavetype.JOURNALFØRING), isNull(), isNull(), eq(LIMIT));
        verify(oppgaveKlient).finnÅpneOppgaverAvType(eq(Oppgavetype.JOURNALFØRING), isNull(), isNull(), eq(LIMIT));
    }

    @Test
    void finnFaktiskÅpneOppgaver() {
        var lokalOppgave = lokalÅpnetOppgave("1234567", VANLIG_ENHETSNR);
        var gosysOppgave = gosysOppgave("76543231");
        when(oppgaveRepository.hentAlleÅpneOppgaver()).thenReturn(List.of(lokalOppgave));
        when(oppgaveKlient.finnÅpneOppgaverAvType(Oppgavetype.JOURNALFØRING, null, null, LIMIT)).thenReturn(List.of(gosysOppgave));
        when(filterKlient.filterIdenter(any())).thenReturn(Set.of(AKTØR_ID));

        var alleOppgaver = oppgaver.finnÅpneOppgaverFiltrert();

        assertThat(alleOppgaver).isNotEmpty().hasSize(2);

        verify(oppgaveRepository).hentAlleÅpneOppgaver();
        verify(oppgaveKlient).finnÅpneOppgaverAvType(Oppgavetype.JOURNALFØRING, null, null, LIMIT);
    }

    @Test
    void finnFaktiskÅpneOppgaver_filtreUtKlageAlltid() {
        when(oppgaveRepository.hentAlleÅpneOppgaver()).thenReturn(List.of(lokalÅpnetOppgave("1234567", EnhetsTjeneste.NK_ENHET_ID)));
        when(oppgaveKlient.finnÅpneOppgaverAvType(Oppgavetype.JOURNALFØRING, null, null, LIMIT)).thenReturn(List.of(gosysOppgave("76543231")));
        when(filterKlient.filterIdenter(any())).thenReturn(Set.of(AKTØR_ID));

        var alleOppgaver = oppgaver.finnÅpneOppgaverFiltrert();

        assertThat(alleOppgaver).isNotEmpty().hasSize(1);

        verify(oppgaveRepository).hentAlleÅpneOppgaver();
        verify(oppgaveKlient).finnÅpneOppgaverAvType(Oppgavetype.JOURNALFØRING, null, null, LIMIT);
    }

    @Test
    void finnFaktiskÅpneOppgaver_filtreUtKlageAlltid_BrukerIKlageEnhet() {
        when(oppgaveRepository.hentAlleÅpneOppgaver()).thenReturn(List.of(lokalÅpnetOppgave("1234567", EnhetsTjeneste.NK_ENHET_ID)));
        var gosysOppgave = gosysOppgave("76543231");
        when(oppgaveKlient.finnÅpneOppgaverAvType(Oppgavetype.JOURNALFØRING, null, null, LIMIT)).thenReturn(List.of(gosysOppgave));

        when(filterKlient.filterIdenter(any())).thenReturn(Set.of(AKTØR_ID));

        var alleOppgaver = oppgaver.finnÅpneOppgaverFiltrert();

        assertThat(alleOppgaver).isNotEmpty().hasSize(1);
        assertThat(alleOppgaver.getFirst().oppgaveId()).isEqualTo(gosysOppgave.id().toString());

        verify(oppgaveRepository).hentAlleÅpneOppgaver();
        verify(oppgaveKlient).finnÅpneOppgaverAvType(Oppgavetype.JOURNALFØRING, null, null, LIMIT);
    }

    @Test
    void finnFaktiskÅpneOppgaver_filtreK6Alltid_BrukerIkkeIK6Ehnet() {
        var tillattAktørId = "1234567890124";
        var lokalOppgave = lokalÅpnetOppgave("1234567", EnhetsTjeneste.SF_ENHET_ID);
        when(oppgaveRepository.hentAlleÅpneOppgaver()).thenReturn(List.of(lokalOppgave));
        var gosysOppgave = gosysOppgave("76543231", tillattAktørId);
        when(oppgaveKlient.finnÅpneOppgaverAvType(Oppgavetype.JOURNALFØRING, null, null, LIMIT)).thenReturn(List.of(gosysOppgave));

        when(filterKlient.filterIdenter(any())).thenReturn(Set.of(tillattAktørId));

        var alleOppgaver = oppgaver.finnÅpneOppgaverFiltrert();

        assertThat(alleOppgaver).isNotEmpty().hasSize(1);
        assertThat(alleOppgaver.getFirst().oppgaveId()).isEqualTo(gosysOppgave.id().toString());

        verify(oppgaveRepository).hentAlleÅpneOppgaver();
        verify(oppgaveKlient).finnÅpneOppgaverAvType(Oppgavetype.JOURNALFØRING, null, null, LIMIT);
    }

    @Test
    void finnFaktiskÅpneOppgaver_filtreAlleSpesjaloppgaver_BrukerIkkeNoeEnhet() {
        when(oppgaveRepository.hentAlleÅpneOppgaver()).thenReturn(List.of(lokalÅpnetOppgave("1234567", EnhetsTjeneste.SKJERMET_ENHET_ID)));
        var gosysOppgave = gosysOppgave("76543231");
        when(oppgaveKlient.finnÅpneOppgaverAvType(Oppgavetype.JOURNALFØRING, null, null, LIMIT)).thenReturn(List.of(gosysOppgave));

        when(filterKlient.filterIdenter(any())).thenReturn(Set.of(AKTØR_ID));
        when(ansattInfoKlient.medlemAvAnsattGruppe(AnsattGruppe.SKJERMET)).thenReturn(true);

        var alleOppgaver = oppgaver.finnÅpneOppgaverFiltrert();

        assertThat(alleOppgaver).isNotEmpty().hasSize(2);

        verify(oppgaveRepository).hentAlleÅpneOppgaver();
        verify(oppgaveKlient).finnÅpneOppgaverAvType(Oppgavetype.JOURNALFØRING, null, null, LIMIT);
    }

    @Test
    void finnFaktiskÅpneOppgaver_filtreAlleSpesjaloppgaver_BrukerIkketilgangTilEnhet() {
        when(oppgaveRepository.hentAlleÅpneOppgaver()).thenReturn(List.of(lokalÅpnetOppgave("1234567", EnhetsTjeneste.SF_ENHET_ID)));
        var gosysOppgave = gosysOppgave("76543231");
        when(oppgaveKlient.finnÅpneOppgaverAvType(Oppgavetype.JOURNALFØRING, null, null, LIMIT)).thenReturn(List.of(gosysOppgave));

        when(filterKlient.filterIdenter(any())).thenReturn(Set.of(AKTØR_ID));
        when(ansattInfoKlient.medlemAvAnsattGruppe(AnsattGruppe.STRENGTFORTROLIG)).thenReturn(false);

        var alleOppgaver = oppgaver.finnÅpneOppgaverFiltrert();

        assertThat(alleOppgaver).isNotEmpty().hasSize(1);

        verify(oppgaveRepository).hentAlleÅpneOppgaver();
        verify(oppgaveKlient).finnÅpneOppgaverAvType(Oppgavetype.JOURNALFØRING, null, null, LIMIT);
    }

    @Test
    void finnFaktiskÅpneOppgaver_filtreIkkeOmAktørErUkjent() {
        when(oppgaveRepository.hentAlleÅpneOppgaver()).thenReturn(List.of(lokalOppgave("1234567", null, VANLIG_ENHETSNR, Status.AAPNET)));
        var expectedOppgaveId = "76543231";
        var gosysOppgave = gosysOppgave(expectedOppgaveId);
        when(oppgaveKlient.finnÅpneOppgaverAvType(Oppgavetype.JOURNALFØRING, null, null, LIMIT)).thenReturn(List.of(gosysOppgave));
        when(filterKlient.filterIdenter(any())).thenReturn(Set.of(AKTØR_ID));

        var alleOppgaver = oppgaver.finnÅpneOppgaverFiltrert();

        assertThat(alleOppgaver).isNotEmpty().hasSize(2);

        verify(oppgaveRepository).hentAlleÅpneOppgaver();
        verify(oppgaveKlient).finnÅpneOppgaverAvType(Oppgavetype.JOURNALFØRING, null, null, LIMIT);
    }

    @Test
    void finnÅpneOppgaverFiltrert_filtrerUtOppgaverSendtTilGosys() {
        when(oppgaveRepository.hentOppgaverFlyttetTilGosys()).thenReturn(List.of(lokalOppgave("1234567", AKTØR_ID, VANLIG_ENHETSNR, Status.FLYTTET_TIL_GOSYS)));
        when(oppgaveKlient.finnÅpneOppgaverAvType(Oppgavetype.JOURNALFØRING, null, null, LIMIT))
            .thenReturn(List.of(gosysOppgave("76543231"), gosysOppgave("1234567")));
        when(filterKlient.filterIdenter(any())).thenReturn(Set.of(AKTØR_ID));

        var alleOppgaver = oppgaver.finnÅpneOppgaverFiltrert();

        assertThat(alleOppgaver).hasSize(1);
        assertThat(alleOppgaver.getFirst().journalpostId()).isEqualTo("76543231");

        verify(oppgaveRepository).hentAlleÅpneOppgaver();
        verify(oppgaveRepository).hentOppgaverFlyttetTilGosys();
        verify(oppgaveKlient).finnÅpneOppgaverAvType(Oppgavetype.JOURNALFØRING, null, null, LIMIT);
    }

    @Test
    void finnÅpneOppgaverFiltrert_filtrerUtGlobaleOppgaverUtenJournalpostId() {
        when(oppgaveRepository.hentOppgaverFlyttetTilGosys()).thenReturn(Collections.emptyList());
        when(oppgaveKlient.finnÅpneOppgaverAvType(Oppgavetype.JOURNALFØRING, null, null, LIMIT))
                .thenReturn(List.of(gosysOppgave(null), gosysOppgave(null)));
        when(filterKlient.filterIdenter(any())).thenReturn(Set.of(AKTØR_ID));

        var alleOppgaver = oppgaver.finnÅpneOppgaverFiltrert();

        assertThat(alleOppgaver).isEmpty();

        verify(oppgaveRepository).hentAlleÅpneOppgaver();
        verify(oppgaveRepository).hentOppgaverFlyttetTilGosys();
        verify(oppgaveKlient).finnÅpneOppgaverAvType(Oppgavetype.JOURNALFØRING, null, null, LIMIT);
    }

    @Test
    void flyttOppgaveTilGosys() {
        var journalpostId = VANLIG_JOURNALPOSTID;

        var oppgaveMock = mock(Oppgave.class);
        Long expectedId = 11L;
        when(oppgaveMock.id()).thenReturn(expectedId);
        when(oppgaveKlient.opprettetOppgave(any())).thenReturn(oppgaveMock);

        when(oppgaveRepository.harÅpenOppgave(journalpostId)).thenReturn(true);
        when(oppgaveRepository.hentOppgave(journalpostId)).thenReturn(lokalÅpnetOppgave(journalpostId, VANLIG_ENHETSNR));

        oppgaver.flyttLokalOppgaveTilGosys(JournalpostId.fra(journalpostId));

        verify(oppgaveRepository).avsluttOppgaveMedStatus(journalpostId, Status.FLYTTET_TIL_GOSYS);
        verify(oppgaveKlient).opprettetOppgave(any(OpprettOppgave.class));
        verifyNoMoreInteractions(oppgaveRepository, oppgaveKlient);
    }

    @Test
    void flyttOppgaveTilGosysUtenAktørId() {
        var journalpostId = VANLIG_JOURNALPOSTID;

        var oppgaveMock = mock(Oppgave.class);
        Long expectedId = 11L;
        when(oppgaveMock.id()).thenReturn(expectedId);
        when(oppgaveKlient.opprettetOppgave(any())).thenReturn(oppgaveMock);

        when(oppgaveRepository.harÅpenOppgave(journalpostId)).thenReturn(true);
        when(oppgaveRepository.hentOppgave(journalpostId)).thenReturn(lokalOppgave(journalpostId, null, null, Status.AAPNET));

        oppgaver.flyttLokalOppgaveTilGosys(JournalpostId.fra(journalpostId));

        verify(oppgaveRepository).avsluttOppgaveMedStatus(journalpostId, Status.FLYTTET_TIL_GOSYS);
        verify(oppgaveKlient).opprettetOppgave(any(OpprettOppgave.class));
        verifyNoMoreInteractions(oppgaveRepository, oppgaveKlient);
    }

    @Test
    void oppdaterBrukerSomFinnes() {
        var journalpostId = VANLIG_JOURNALPOSTID;
        var oppgave = no.nav.foreldrepenger.journalføring.oppgave.domene.Oppgave.builder()
            .medJournalpostId(journalpostId)
            .medOppgaveId(journalpostId)
            .medKilde(no.nav.foreldrepenger.journalføring.oppgave.domene.Oppgave.Kilde.LOKAL)
            .medAktivDato(LocalDate.now())
            .medTildeltEnhetsnr("4867")
            .medStatus(no.nav.foreldrepenger.journalføring.oppgave.domene.Oppgavestatus.AAPNET)
            .medFristFerdigstillelse(LocalDate.now().plusDays(1))
            .medBeskrivelse("tom")
            .build();
        when(oppgaveRepository.harÅpenOppgave(journalpostId)).thenReturn(true);
        when(oppgaveRepository.hentOppgave(journalpostId)).thenReturn(lokalÅpnetOppgave(journalpostId, VANLIG_ENHETSNR));
        var fødselsnummer = "12342112345";
        var aktørId = AKTØR_ID;
        when(personTjeneste.hentAktørIdForPersonIdent(fødselsnummer)).thenReturn(Optional.of(aktørId));

        var argumentCaptor = ArgumentCaptor.forClass(OppgaveEntitet.class);

        oppgaver.oppdaterBruker(oppgave, fødselsnummer);

        verify(oppgaveRepository).lagre(argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().getBrukerId()).isNotNull();
        assertThat(argumentCaptor.getValue().getBrukerId().getId()).isEqualTo(aktørId);
        verifyNoMoreInteractions(oppgaveRepository);
        verifyNoInteractions(oppgaveKlient);
    }

    @Test
    void oppdaterBrukerSomIkkeFinnes_KastException() {
        var journalpostId = VANLIG_JOURNALPOSTID;
        var oppgave = no.nav.foreldrepenger.journalføring.oppgave.domene.Oppgave.builder()
            .medJournalpostId(journalpostId)
            .medOppgaveId(journalpostId)
            .medKilde(no.nav.foreldrepenger.journalføring.oppgave.domene.Oppgave.Kilde.LOKAL)
            .medAktivDato(LocalDate.now())
            .medTildeltEnhetsnr("4867")
            .medStatus(no.nav.foreldrepenger.journalføring.oppgave.domene.Oppgavestatus.AAPNET)
            .medFristFerdigstillelse(LocalDate.now().plusDays(1))
            .medBeskrivelse("tom")
            .build();
        when(oppgaveRepository.harÅpenOppgave(journalpostId)).thenReturn(true);
        when(oppgaveRepository.hentOppgave(journalpostId)).thenReturn(lokalÅpnetOppgave(journalpostId, VANLIG_ENHETSNR));
        var fødselsnummer = "12342112345";
        when(personTjeneste.hentAktørIdForPersonIdent(fødselsnummer)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> oppgaver.oppdaterBruker(oppgave, fødselsnummer));
    }

    private OppgaveEntitet lokalOppgave(String journalpostId, String aktørId, String enhet, Status status) {
        var entitet = OppgaveEntitet.builder()
            .medJournalpostId(journalpostId)
            .medStatus(status)
            .medBeskrivelse("test")
            .medEnhet(enhet != null ? enhet : VANLIG_ENHETSNR)
            .medBrukerId(aktørId)
            .medFrist(LocalDate.now())
            .medYtelseType(YtelseType.FP)
            .medReservertAv("saksbehandler")
            .build();
        entitet.setOpprettetTidspunkt(LocalDateTime.now());
        return entitet;
    }

    private OppgaveEntitet lokalÅpnetOppgave(String journalpostId, String enhet) {
        return lokalOppgave(journalpostId, AKTØR_ID, enhet, Status.AAPNET);
    }

    private Oppgave gosysOppgave(String journalpostId) {
        return gosysOppgave(journalpostId, AKTØR_ID);
    }

    private Oppgave gosysOppgave(String journalpostId, String aktørId) {
        return new Oppgave(1L, journalpostId, null, "testRef", aktørId, null, null,
            Oppgavetype.JOURNALFØRING, null, 0, VANLIG_ENHETSNR, LocalDate.now(), LocalDate.now(), Prioritet.NORM, Oppgavestatus.AAPNET,
            "test beskrivelse", null);
    }
}
