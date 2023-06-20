package no.nav.foreldrepenger.fordel.web.app.rest.journalføring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import no.nav.vedtak.exception.FunksjonellException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverdi.Journalstatus;
import no.nav.foreldrepenger.fordel.kodeverdi.Tema;
import no.nav.foreldrepenger.fordel.web.app.exceptions.FeilDto;
import no.nav.foreldrepenger.kontrakter.fordel.JournalpostIdDto;
import no.nav.foreldrepenger.mottak.journal.ArkivJournalpost;
import no.nav.foreldrepenger.mottak.journal.ArkivTjeneste;
import no.nav.foreldrepenger.mottak.journal.saf.DokumentInfo;
import no.nav.foreldrepenger.mottak.journal.saf.Journalpost;
import no.nav.foreldrepenger.mottak.klient.Fagsak;
import no.nav.foreldrepenger.mottak.klient.Los;
import no.nav.foreldrepenger.mottak.klient.TilhørendeEnhetDto;
import no.nav.foreldrepenger.mottak.klient.YtelseTypeDto;
import no.nav.foreldrepenger.mottak.person.PersonInformasjon;
import no.nav.vedtak.exception.ManglerTilgangException;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.integrasjon.dokarkiv.dto.Bruker;
import no.nav.vedtak.felles.integrasjon.oppgave.v1.Oppgave;
import no.nav.vedtak.felles.integrasjon.oppgave.v1.Oppgaver;
import no.nav.vedtak.felles.integrasjon.oppgave.v1.Oppgavestatus;
import no.nav.vedtak.felles.integrasjon.oppgave.v1.Oppgavetype;
import no.nav.vedtak.felles.integrasjon.oppgave.v1.Prioritet;
import no.nav.vedtak.sikkerhet.kontekst.IdentType;
import no.nav.vedtak.sikkerhet.kontekst.Kontekst;
import no.nav.vedtak.sikkerhet.kontekst.KontekstHolder;
import no.nav.vedtak.sikkerhet.kontekst.SikkerhetContext;

@ExtendWith(MockitoExtension.class)
class ManuellJournalføringRestTjenesteTest {

    private final String LIMIT = "50";
    @Mock
    private PersonInformasjon pdl;
    @Mock
    private Oppgaver oppgaver;
    @Mock
    private Fagsak fagsak;
    @Mock
    private ArkivTjeneste arkiv;
    @Mock
    private Los los;
    private final SaksbehandlerIdentDto saksbehandlerIdentDto = new SaksbehandlerIdentDto("123456");
    private final TilhørendeEnhetDto tilhørendeEnhetDto = new TilhørendeEnhetDto("9999", "Navn på enhet");

    private ManuellJournalføringRestTjeneste restTjeneste;

    @BeforeEach
    void setUp() {
        restTjeneste = new ManuellJournalføringRestTjeneste(oppgaver, pdl, arkiv, fagsak, los);
    }

    @Test
    @DisplayName("/oppgaver - ingen oppgaver = tom liste")
    void skal_levere_en_tom_liste_om_ingen_oppgaver_funnet() {

        when(los.hentTilhørendeEnheter(saksbehandlerIdentDto.ident())).thenReturn(List.of(tilhørendeEnhetDto));
        var oppgaveDtos = restTjeneste.hentÅpneOppgaverForSaksbehandler(saksbehandlerIdentDto);

        assertThat(oppgaveDtos).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("/oppgaver - 1 oppgave = liste med 1 oppgave med tittel.")
    void skal_levere_en_liste_med_oppgaver_med_tittel() {
        var expectedId = 123L;
        var expectedJournalpostId = "12334";
        var now = LocalDate.now();
        var beskrivelse = "beskrivelse";

        var journalføringOppgaver = List.of(
            opprettOppgave(expectedId, now, expectedJournalpostId, beskrivelse, BehandlingTema.FORELDREPENGER_FØDSEL, tilhørendeEnhetDto.enhetsnummer(), null));

        when(los.hentTilhørendeEnheter(saksbehandlerIdentDto.ident())).thenReturn(List.of(tilhørendeEnhetDto));
        when(oppgaver.finnÅpneOppgaverAvType(Oppgavetype.JOURNALFØRING, null, tilhørendeEnhetDto.enhetsnummer(), LIMIT)).thenReturn(journalføringOppgaver);

        var oppgaveDtos = restTjeneste.hentÅpneOppgaverForSaksbehandler(saksbehandlerIdentDto);

        assertThat(oppgaveDtos).isNotNull().hasSize(1);
        var oppgave = oppgaveDtos.get(0);
        assertThat(oppgave.journalpostId()).isEqualTo(expectedJournalpostId);
        assertThat(oppgave.id()).isEqualTo(expectedId);
        assertThat(oppgave.frist()).isEqualTo(now);
        assertThat(oppgave.aktørId()).isEqualTo("aktørId");
        assertThat(oppgave.fødselsnummer()).isNull();
        assertThat(oppgave.beskrivelse()).isEqualTo(beskrivelse);
        assertThat(oppgave.opprettetDato()).isEqualTo(now);
        assertThat(oppgave.prioritet()).isEqualTo(ManuellJournalføringRestTjeneste.OppgavePrioritet.NORM);
        assertThat(oppgave.ytelseType()).isEqualTo(YtelseTypeDto.FORELDREPENGER);
        assertThat(oppgave.enhetId()).isEqualTo(tilhørendeEnhetDto.enhetsnummer());
    }

    @Test
    @DisplayName("/oppgaver - 1 oppgave = liste med 1 oppgave uten tittel.")
    void skal_levere_en_liste_med_oppgaver_uten_tittel() {
        var expectedJournalPostUtenTittel = "1236";
        var expectedOppgaveId = 125L;
        var now = LocalDate.now();
        var beskrivelse = "beskrivelse";

        var journalføringOppgaver = List.of(opprettOppgave(expectedOppgaveId, now, expectedJournalPostUtenTittel, beskrivelse, BehandlingTema.SVANGERSKAPSPENGER, tilhørendeEnhetDto.enhetsnummer(), null));

        when(los.hentTilhørendeEnheter(saksbehandlerIdentDto.ident())).thenReturn(List.of(tilhørendeEnhetDto));
        when(oppgaver.finnÅpneOppgaverAvType(Oppgavetype.JOURNALFØRING, null, tilhørendeEnhetDto.enhetsnummer(), LIMIT)).thenReturn(journalføringOppgaver);

        var oppgaveDtos = restTjeneste.hentÅpneOppgaverForSaksbehandler(saksbehandlerIdentDto);

        assertThat(oppgaveDtos).isNotNull().hasSize(1);
        var oppgave = oppgaveDtos.get(0);
        assertThat(oppgave.journalpostId()).isEqualTo(expectedJournalPostUtenTittel);
        assertThat(oppgave.id()).isEqualTo(expectedOppgaveId);
        assertThat(oppgave.frist()).isEqualTo(now);
        assertThat(oppgave.aktørId()).isEqualTo("aktørId");
        assertThat(oppgave.fødselsnummer()).isNull();
        assertThat(oppgave.beskrivelse()).isEqualTo(beskrivelse);
        assertThat(oppgave.opprettetDato()).isEqualTo(now);
        assertThat(oppgave.prioritet()).isEqualTo(ManuellJournalføringRestTjeneste.OppgavePrioritet.NORM);
        assertThat(oppgave.ytelseType()).isEqualTo(YtelseTypeDto.SVANGERSKAPSPENGER);
        assertThat(oppgave.enhetId()).isEqualTo(tilhørendeEnhetDto.enhetsnummer());
    }


    @Test
    @DisplayName("/oppgaver - 2 oppgaver = liste med 2 oppgaver med ulike enheter.")
    void skal_levere_en_liste_med_oppgaver_på_ulike_enheter() {
        var now = LocalDate.now();
        var enhet1 = "111111";
        var enhet2 = "222222";

        when(los.hentTilhørendeEnheter(saksbehandlerIdentDto.ident())).thenReturn(List.of(new TilhørendeEnhetDto(enhet1, "Enhet 1"), new TilhørendeEnhetDto(enhet2, "Enhet 2")));

        var journalføringOppgaveEnhet1 = opprettOppgave(123L, now, "1111", "beskrivelse1", BehandlingTema.FORELDREPENGER_ADOPSJON, enhet1, null);
        var journalføringOppgaveEnhet2 = opprettOppgave(124L, now, "2222", "beskrivelse2", BehandlingTema.SVANGERSKAPSPENGER, enhet2, null);

        when(oppgaver.finnÅpneOppgaverAvType(Oppgavetype.JOURNALFØRING, null, enhet1, LIMIT)).thenReturn(List.of(journalføringOppgaveEnhet1));
        when(oppgaver.finnÅpneOppgaverAvType(Oppgavetype.JOURNALFØRING, null, enhet2, LIMIT)).thenReturn(List.of(journalføringOppgaveEnhet2));

        var oppgaveDtos = restTjeneste.hentÅpneOppgaverForSaksbehandler(saksbehandlerIdentDto);

        assertThat(oppgaveDtos).isNotNull().hasSize(2);
        var oppgave1 = oppgaveDtos.get(0);
        assertThat(oppgave1.ytelseType()).isEqualTo(YtelseTypeDto.FORELDREPENGER);
        assertThat(oppgave1.enhetId()).isEqualTo(enhet1);
        var oppgave2 = oppgaveDtos.get(1);
        assertThat(oppgave2.ytelseType()).isEqualTo(YtelseTypeDto.SVANGERSKAPSPENGER);
        assertThat(oppgave2.enhetId()).isEqualTo(enhet2);
    }

    @Test
    @DisplayName("/oppgaver - fnr på plass om aktør finnes.")
    void skal_levere_fnr_om_finnes() {
        var expectedId = 123L;
        var expectedJournalpostId = "12334";
        var now = LocalDate.now();
        var beskrivelse = "beskrivelse";
        var aktørId = "aktørId";
        var journalføringOppgaver = List.of(
            opprettOppgave(expectedId, now, expectedJournalpostId, beskrivelse, BehandlingTema.FORELDREPENGER_FØDSEL, tilhørendeEnhetDto.enhetsnummer(), null));

        when(los.hentTilhørendeEnheter(saksbehandlerIdentDto.ident())).thenReturn(List.of(tilhørendeEnhetDto));
        when(oppgaver.finnÅpneOppgaverAvType(Oppgavetype.JOURNALFØRING, null, tilhørendeEnhetDto.enhetsnummer(), LIMIT)).thenReturn(journalføringOppgaver);

        var fnr = "12344345678";
        when(pdl.hentPersonIdentForAktørId(aktørId)).thenReturn(Optional.of(fnr));
        var oppgaveDtos = restTjeneste.hentÅpneOppgaverForSaksbehandler(saksbehandlerIdentDto);

        assertThat(oppgaveDtos).isNotNull().hasSize(1);
        var oppgave = oppgaveDtos.get(0);
        assertThat(oppgave.aktørId()).isEqualTo(aktørId);
        assertThat(oppgave.fødselsnummer()).isEqualTo(fnr);
        assertThat(oppgave.ytelseType()).isEqualTo(YtelseTypeDto.FORELDREPENGER);
    }

    @Test
    @DisplayName("/oppgaver - ytelseType = Ukjent - om ikke FP behandlingTema")
    void skal_ha_ytelse_type_ukjent_om_det_ikke_lar_seg_utlede_fra_behandlingstema() {
        var expectedId = 123L;
        var expectedJournalpostId = "12334";
        var now = LocalDate.now();
        var beskrivelse = "beskrivelse";
        var journalføringOppgaver = List.of(opprettOppgave(expectedId, now, expectedJournalpostId, beskrivelse, BehandlingTema.OMS, tilhørendeEnhetDto.enhetsnummer(), null));

        when(los.hentTilhørendeEnheter(saksbehandlerIdentDto.ident())).thenReturn(List.of(tilhørendeEnhetDto));
        when(oppgaver.finnÅpneOppgaverAvType(Oppgavetype.JOURNALFØRING, null, tilhørendeEnhetDto.enhetsnummer(), LIMIT)).thenReturn(journalføringOppgaver);

        var oppgaveDtos = restTjeneste.hentÅpneOppgaverForSaksbehandler(saksbehandlerIdentDto);

        assertThat(oppgaveDtos).isNotNull().hasSize(1);
        assertThat(oppgaveDtos.get(0).ytelseType()).isNull();
    }

    @Test
    @DisplayName("/hent/dokument - dokument finnes.")
    void skal_levere_dokumentet() {
        var expectedJournalpostId = "12334";
        var expectedDokumentId = "12334";

        when(arkiv.hentDokumet(expectedJournalpostId, expectedDokumentId)).thenReturn("sdflsdflsdfls".getBytes(StandardCharsets.UTF_8));
        var response = restTjeneste.hentDokument(new JournalpostIdDto(expectedJournalpostId), new DokumentIdDto(expectedDokumentId));

        assertThat(response).isNotNull();
        assertThat(response.getMediaType()).isEqualTo(new MediaType("application", "pdf"));
        assertThat(response.getHeaders()).containsKey("Content-Disposition");
        assertThat(response.getEntity()).isOfAnyClassIn(ByteArrayInputStream.class);
    }

    @Test
    @DisplayName("/hent/dokument - 404 feil response med FeilDto - om dokumenter ikke finnes.")
    void skal_kaste_feil_ved_dokument_mangel() {
        var expectedJournalpostId = "12334";
        var expectedDokumentId = "12334";

        when(arkiv.hentDokumet(expectedJournalpostId, expectedDokumentId)).thenReturn(null);

        var response = restTjeneste.hentDokument(new JournalpostIdDto(expectedJournalpostId), new DokumentIdDto(expectedDokumentId));

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.getMediaType()).isEqualTo(MediaType.APPLICATION_JSON_TYPE);
        var entity = response.getEntity();
        assertThat(entity).isExactlyInstanceOf(FeilDto.class);
        assertThat(((FeilDto) entity).feilmelding()).contains(
            String.format("Dokument ikke funnet for journalpost= %s dokument= %s", expectedJournalpostId, expectedDokumentId));
    }

    @Test
    @DisplayName("/oppgave/detaljer - exception om journalpost mangler")
    void skal_kaste_exception_om_journalpost_mangler() {
        var expectedJournalpostId = "12334";

        when(arkiv.hentArkivJournalpost(expectedJournalpostId)).thenReturn(null);

        var journalpostId = new JournalpostIdDto(expectedJournalpostId);

        var ex = assertThrows(TekniskException.class, () -> restTjeneste.hentJournalpostDetaljer(journalpostId));

        assertThat(ex).isNotNull();
        assertThat(ex.getMessage()).isEqualTo("FORDEL-123:Journapost " + expectedJournalpostId + " finnes ikke i arkivet.");
    }

    @Test
    @DisplayName("/oppgave/detaljer - returner detaljer.")
    void skal_returnere_detaljer() {
        var expectedJournalpostId = "12334";

        when(arkiv.hentArkivJournalpost(expectedJournalpostId)).thenReturn(opprettJournalpost(expectedJournalpostId));

        var ex = restTjeneste.hentJournalpostDetaljer(new JournalpostIdDto(expectedJournalpostId));

        assertThat(ex).isNotNull();
        assertThat(ex.journalpostId()).isEqualTo(expectedJournalpostId);
    }

    @DisplayName("/bruker/hent - ok bruker finnes")
    void skal_levere_navn_til_bruker() {
        var expectedFnr = "11111122222";
        var brukerAktørId = "1234567890123";
        var navn = "Ola, Ola";

        when(pdl.hentAktørIdForPersonIdent(expectedFnr)).thenReturn(Optional.of(brukerAktørId));
        when(pdl.hentNavn(brukerAktørId)).thenReturn(navn);

        var request = new ManuellJournalføringRestTjeneste.HentBrukerDto(expectedFnr);
        var response = restTjeneste.hentBruker(request);

        assertThat(response).isNotNull();
        assertThat(response.navn()).isEqualTo(navn);
        assertThat(response.fødselsnummer()).isEqualTo(expectedFnr);
    }

    @Test
    @DisplayName("/bruker/oppdater - exception om journalpost mangler")
    void skal_kaste_exception_om_journalpostId_mangler() {
        var request = new ManuellJournalføringRestTjeneste.OppdaterBrukerDto(null, null);
        var ex = assertThrows(NullPointerException.class, () -> restTjeneste.oppdaterBruker(request));

        assertThat(ex).isNotNull();
        assertThat(ex.getMessage()).isEqualTo("JournalpostId må være satt.");
    }

    @Test
    @DisplayName("/bruker/oppdater - exception om fødselsnummer mangler")
    void skal_kaste_exception_om_fnr_mangler() {
        var request = new ManuellJournalføringRestTjeneste.OppdaterBrukerDto("1234", null);
        var ex = assertThrows(NullPointerException.class, () -> restTjeneste.oppdaterBruker(request));

        assertThat(ex).isNotNull();
        assertThat(ex.getMessage()).isEqualTo("FNR/DNR må være satt.");
    }

    @Test
    @DisplayName("/bruker/oppdater - oppdater med Bruker om den ikke er satt")
    void skal_oppdatere_bruker_om_ikke_satt() {
        var expectedJournalpostId = "12334";
        var expectedFnr = "11111122222";

        when(arkiv.hentArkivJournalpost(expectedJournalpostId)).thenReturn(opprettJournalpost(expectedJournalpostId));

        var request = new ManuellJournalføringRestTjeneste.OppdaterBrukerDto(expectedJournalpostId, expectedFnr);
        var journalpostDetaljerDto = restTjeneste.oppdaterBruker(request);

        assertThat(journalpostDetaljerDto).isNotNull();
        verify(arkiv).oppdaterJournalpostBruker(expectedJournalpostId, expectedFnr);
        verify(arkiv, times(2)).hentArkivJournalpost(expectedJournalpostId);
    }

    @Test
    @DisplayName("/bruker/oppdater - ikke oppdater om bruker finnes på journalposten allerede.")
    void skal_ikke_oppdatere_bruker_om_den_er_satt_allerede() {
        var expectedJournalpostId = "12334";
        var brukerAktørId = "1234567890123";
        var expectedFnr = "11111122222";

        when(arkiv.hentArkivJournalpost(expectedJournalpostId)).thenReturn(getStandardBuilder(expectedJournalpostId, brukerAktørId).build());
        when(pdl.hentPersonIdentForAktørId(brukerAktørId)).thenReturn(Optional.of(expectedFnr));

        var request = new ManuellJournalføringRestTjeneste.OppdaterBrukerDto(expectedJournalpostId, expectedFnr);
        var journalpostDetaljerDto = restTjeneste.oppdaterBruker(request);

        assertThat(journalpostDetaljerDto).isNotNull();
        verify(arkiv, times(0)).oppdaterJournalpostBruker(expectedJournalpostId, expectedFnr);
        verify(arkiv, times(2)).hentArkivJournalpost(expectedJournalpostId);
    }

    @Test
    @DisplayName("/oppgave/reserver - exception om avreserverer en reservasjon til en annen bruker.")
    void skal_kaste_exception_hvis_oppgave_reservert_an_annen_saksbehandler() {
        var expectedOppgaveId = 123L;

        when(oppgaver.hentOppgave(anyString())).thenReturn(
            opprettOppgave(expectedOppgaveId, LocalDate.now(), "12334", "test", BehandlingTema.FORELDREPENGER, "7070", "John"));

        var request = new ManuellJournalføringRestTjeneste.ReserverOppgaveDto(String.valueOf(expectedOppgaveId), 1,null);

        Exception ex;
        try (var utilities = Mockito.mockStatic(KontekstHolder.class)) {
            utilities.when(KontekstHolder::getKontekst).thenReturn(new TestKontekst("Mike"));
            assertThat(KontekstHolder.getKontekst().getUid()).isEqualTo("Mike");
            ex = assertThrows(ManglerTilgangException.class, () -> restTjeneste.oppgaveReserver(request));
        }

        assertThat(ex).isNotNull();
        assertThat(ex.getMessage()).contains("Kan ikke avreservere en oppgave som tilhører en annen saksbehandler.");

        verify(oppgaver, times(0)).reserverOppgave(anyString(), anyString());
        verify(oppgaver, times(0)).avreserverOppgave(anyString());
    }

    @Test
    @DisplayName("/oppgave/reserver - skal kunne avreservere sin egen oppgave.")
    void skal_kunne_avreservere_en_ledig_oppgave() {
        var expectedOppgaveId = 123L;

        when(oppgaver.hentOppgave(anyString())).thenReturn(
            opprettOppgave(expectedOppgaveId, LocalDate.now(), "12334", "test", BehandlingTema.FORELDREPENGER, "7070", "John"));

        var request = new ManuellJournalføringRestTjeneste.ReserverOppgaveDto(String.valueOf(expectedOppgaveId), 1, "");
        Response response;

        try (var utilities = Mockito.mockStatic(KontekstHolder.class)) {
            utilities.when(KontekstHolder::getKontekst).thenReturn(new TestKontekst("John"));
            assertThat(KontekstHolder.getKontekst().getUid()).isEqualTo("John");
            response = restTjeneste.oppgaveReserver(request);
        }

        assertThat(response).isNotNull();
        assertThat(response.getStatusInfo()).isEqualTo(Response.Status.OK);

        verify(oppgaver).avreserverOppgave("123");
        verify(oppgaver, times(0)).reserverOppgave(anyString(), anyString());
    }

    private ArkivJournalpost opprettJournalpost(String journalpostId) {
        return getStandardBuilder(journalpostId, null).build();
    }

    private static ArkivJournalpost.Builder getStandardBuilder(String journalpostId, String brukerAktørId) {
        return ArkivJournalpost.getBuilder()
            .medJournalpostId(journalpostId)
            .medBrukerAktørId(brukerAktørId)
            .medHovedtype(DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL)
            .medTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER)
            .medTilstand(Journalstatus.MOTTATT)
            .medJournalpost(opprettOriginalJournalpost(journalpostId, brukerAktørId));
    }

    private static Journalpost opprettOriginalJournalpost(String journalpostId, String brukerAktørId) {
        return new Journalpost(journalpostId,
            null,
            null,
            null,
            "tittel",
            null,
            null,
            null,
            null,
            null,
            brukerAktørId != null ? new Bruker(brukerAktørId, Bruker.BrukerIdType.AKTOERID): null,
            null,
            null,
            List.of(),
            List.of(new DokumentInfo("555", "tittel", "brevkode", null, null)));
    }

    private static Oppgave opprettOppgave(long expectedId, LocalDate now, String expectedJournalpostId, String beskrivelse, BehandlingTema behandlingTema, String enhetsNr, String reservertAv) {
        return new Oppgave(
            expectedId,
            expectedJournalpostId,
            "FPSAK",
            null,
            "aktørId",
            "tema",
            behandlingTema.getOffisiellKode(),
            Oppgavetype.JOURNALFØRING.getKode(),
            "behType",
            1,
            enhetsNr,
            now,
            now,
            Prioritet.NORM,
            Oppgavestatus.AAPNET,
            beskrivelse,
            reservertAv);
    }

    class TestKontekst implements Kontekst {

        private String suid;

        public TestKontekst(String suid) {
            this.suid = suid;
        }

        @Override
        public SikkerhetContext getContext() {
            return null;
        }

        @Override
        public String getUid() {
            return this.suid;
        }

        @Override
        public String getKompaktUid() {
            return null;
        }

        @Override
        public IdentType getIdentType() {
            return null;
        }

        @Override
        public String getKonsumentId() {
            return null;
        }
    }
}
