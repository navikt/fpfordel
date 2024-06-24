package no.nav.foreldrepenger.fordel.web.app.rest.journalføring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverdi.Journalstatus;
import no.nav.foreldrepenger.fordel.kodeverdi.Tema;
import no.nav.foreldrepenger.fordel.web.app.exceptions.FeilDto;
import no.nav.foreldrepenger.journalføring.domene.JournalpostId;
import no.nav.foreldrepenger.journalføring.oppgave.Journalføringsoppgave;
import no.nav.foreldrepenger.journalføring.oppgave.domene.Oppgave;
import no.nav.foreldrepenger.journalføring.oppgave.domene.Oppgavestatus;
import no.nav.foreldrepenger.journalføring.oppgave.lager.YtelseType;
import no.nav.foreldrepenger.kontrakter.fordel.JournalpostIdDto;
import no.nav.foreldrepenger.mottak.journal.ArkivJournalpost;
import no.nav.foreldrepenger.mottak.journal.ArkivTjeneste;
import no.nav.foreldrepenger.mottak.journal.saf.DokumentInfo;
import no.nav.foreldrepenger.mottak.journal.saf.Journalpost;
import no.nav.foreldrepenger.mottak.klient.Fagsak;
import no.nav.foreldrepenger.mottak.person.PersonInformasjon;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.integrasjon.dokarkiv.dto.Bruker;
import no.nav.vedtak.sikkerhet.kontekst.IdentType;
import no.nav.vedtak.sikkerhet.kontekst.Kontekst;
import no.nav.vedtak.sikkerhet.kontekst.KontekstHolder;
import no.nav.vedtak.sikkerhet.kontekst.SikkerhetContext;

@ExtendWith(MockitoExtension.class)
class JournalføringRestTjenesteTest {

    private static final String ENHET = "4321";

    @Mock
    private PersonInformasjon pdl;
    @Mock
    private Journalføringsoppgave oppgaveTjeneste;
    @Mock
    private Fagsak fagsak;
    @Mock
    private ArkivTjeneste arkiv;

    private JournalføringRestTjeneste restTjeneste;

    @BeforeEach
    void setUp() {
        restTjeneste = new JournalføringRestTjeneste(oppgaveTjeneste, pdl, arkiv, fagsak);
    }

    @Test
    @DisplayName("/oppgaver - ingen oppgaver = tom liste")
    void skal_levere_en_tom_liste_om_ingen_oppgaver_funnet() {
        var oppgaveDtos = restTjeneste.hentÅpneOppgaver();

        assertThat(oppgaveDtos).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("/oppgaver - 1 oppgave = liste med 1 oppgave med tittel.")
    void skal_levere_en_liste_med_oppgaver_med_tittel() {
        var expectedJournalpostId = "12334";
        var now = LocalDate.now();
        var beskrivelse = "beskrivelse";

        var journalføringOppgaver = List.of(
            opprettOppgave(expectedJournalpostId, now, beskrivelse, ENHET, null, YtelseType.FP));

        when(oppgaveTjeneste.finnÅpneOppgaverFiltrert()).thenReturn(journalføringOppgaver);

        var oppgaveDtos = restTjeneste.hentÅpneOppgaver();

        assertThat(oppgaveDtos).isNotNull().hasSize(1);
        var oppgave = oppgaveDtos.get(0);
        assertThat(oppgave.journalpostId()).isEqualTo(expectedJournalpostId);
        assertThat(oppgave.frist()).isEqualTo(now);
        assertThat(oppgave.aktørId()).isEqualTo("aktørId");
        assertThat(oppgave.fødselsnummer()).isNull();
        assertThat(oppgave.beskrivelse()).isEqualTo(beskrivelse);
        assertThat(oppgave.opprettetDato()).isEqualTo(now);
        assertThat(oppgave.ytelseType()).isEqualTo(YtelseTypeDto.FORELDREPENGER);
        assertThat(oppgave.enhetId()).isEqualTo(ENHET);
        assertThat(oppgave.kilde()).isEqualTo(JournalføringRestTjeneste.OppgaveKilde.GOSYS);
    }

    @Test
    @DisplayName("/oppgaver - 1 oppgave = liste med 1 oppgave uten tittel.")
    void skal_levere_en_liste_med_oppgaver_uten_tittel() {
        var expectedJournalPostUtenTittel = "1236";
        var now = LocalDate.now();
        var beskrivelse = "beskrivelse";

        var journalføringOppgaver = List.of(opprettOppgave(expectedJournalPostUtenTittel, now, beskrivelse, ENHET, null,
            YtelseType.SVP));

        when(oppgaveTjeneste.finnÅpneOppgaverFiltrert()).thenReturn(journalføringOppgaver);

        var oppgaveDtos = restTjeneste.hentÅpneOppgaver();

        assertThat(oppgaveDtos).isNotNull().hasSize(1);
        var oppgave = oppgaveDtos.get(0);
        assertThat(oppgave.journalpostId()).isEqualTo(expectedJournalPostUtenTittel);
        assertThat(oppgave.frist()).isEqualTo(now);
        assertThat(oppgave.aktørId()).isEqualTo("aktørId");
        assertThat(oppgave.fødselsnummer()).isNull();
        assertThat(oppgave.beskrivelse()).isEqualTo(beskrivelse);
        assertThat(oppgave.opprettetDato()).isEqualTo(now);
        assertThat(oppgave.ytelseType()).isEqualTo(YtelseTypeDto.SVANGERSKAPSPENGER);
        assertThat(oppgave.enhetId()).isEqualTo(ENHET);
    }

    @Test
    @DisplayName("/oppgaver - 2 oppgaver = liste med 2 oppgaver med ulike enheter.")
    void skal_levere_en_liste_med_oppgaver_på_ulike_enheter() {
        var now = LocalDate.now();
        var enhet1 = "111111";
        var enhet2 = "222222";

        var journalføringOppgaveEnhet1 = opprettOppgave("1111", now, "beskrivelse1", enhet1, null, YtelseType.FP);
        var journalføringOppgaveEnhet2 = opprettOppgave("2222", now, "beskrivelse2", enhet2, null, YtelseType.SVP);

        when(oppgaveTjeneste.finnÅpneOppgaverFiltrert()).thenReturn(List.of(journalføringOppgaveEnhet1, journalføringOppgaveEnhet2));

        var oppgaveDtos = restTjeneste.hentÅpneOppgaver();

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
        var expectedJournalpostId = "12334";
        var now = LocalDate.now();
        var beskrivelse = "beskrivelse";
        var aktørId = "aktørId";
        var journalføringOppgaver = List.of(
            opprettOppgave(expectedJournalpostId, now, beskrivelse, ENHET, null, YtelseType.FP));

        when(oppgaveTjeneste.finnÅpneOppgaverFiltrert()).thenReturn(journalføringOppgaver);

        var fnr = "12344345678";
        when(pdl.hentPersonIdentForAktørId(aktørId)).thenReturn(Optional.of(fnr));
        var oppgaveDtos = restTjeneste.hentÅpneOppgaver();

        assertThat(oppgaveDtos).isNotNull().hasSize(1);
        var oppgave = oppgaveDtos.get(0);
        assertThat(oppgave.aktørId()).isEqualTo(aktørId);
        assertThat(oppgave.fødselsnummer()).isEqualTo(fnr);
        assertThat(oppgave.ytelseType()).isEqualTo(YtelseTypeDto.FORELDREPENGER);
    }

    @Test
    @DisplayName("/oppgaver - ytelseType = Ukjent - om ikke FP behandlingTema")
    void skal_ha_ytelse_type_ukjent_om_det_ikke_lar_seg_utlede_fra_behandlingstema() {
        var expectedJournalpostId = "12334";
        var now = LocalDate.now();
        var beskrivelse = "beskrivelse";
        var journalføringOppgaver = List.of(opprettOppgave(expectedJournalpostId, now, beskrivelse, ENHET, null, null));

        when(oppgaveTjeneste.finnÅpneOppgaverFiltrert()).thenReturn(journalføringOppgaver);

        var oppgaveDtos = restTjeneste.hentÅpneOppgaver();

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

        var ex = restTjeneste.hentJournalpostDetaljer(journalpostId);

        assertThat(ex).isNotNull();
        assertThat(ex.getStatus()).isEqualTo(HttpURLConnection.HTTP_NO_CONTENT);
    }

    @Test
    @DisplayName("/oppgave/detaljer - returner detaljer.")
    void skal_returnere_detaljer() {
        var expectedJournalpostId = "12334";

        when(arkiv.hentArkivJournalpost(expectedJournalpostId)).thenReturn(opprettJournalpost(expectedJournalpostId));

        var ex = restTjeneste.hentJournalpostDetaljer(new JournalpostIdDto(expectedJournalpostId));

        assertThat(ex).isNotNull();
        assertThat(ex.getStatus()).isEqualTo(HttpURLConnection.HTTP_OK);
        assertThat(((JournalpostDetaljerDto) ex.getEntity()).journalpostId()).isEqualTo(expectedJournalpostId);
    }

    @DisplayName("/bruker/hent - ok bruker finnes")
    void skal_levere_navn_til_bruker() {
        var expectedFnr = "11111122222";
        var brukerAktørId = "1234567890123";
        var navn = "Ola, Ola";

        when(pdl.hentAktørIdForPersonIdent(expectedFnr)).thenReturn(Optional.of(brukerAktørId));
        when(pdl.hentNavn(BehandlingTema.FORELDREPENGER, brukerAktørId)).thenReturn(navn);

        var request = new JournalføringRestTjeneste.HentBrukerDto(expectedFnr);
        var response = restTjeneste.hentBruker(request);

        assertThat(response).isNotNull();
        assertThat(response.navn()).isEqualTo(navn);
        assertThat(response.fødselsnummer()).isEqualTo(expectedFnr);
    }

    @Test
    @DisplayName("/bruker/oppdater - exception om journalpost mangler")
    void skal_kaste_exception_om_journalpostId_mangler() {
        var request = new JournalføringRestTjeneste.OppdaterBrukerDto(null, null);
        var ex = assertThrows(NullPointerException.class, () -> restTjeneste.oppdaterBruker(request));

        assertThat(ex).isNotNull();
        assertThat(ex.getMessage()).isEqualTo("JournalpostId må være satt.");
    }

    @Test
    @DisplayName("/bruker/oppdater - exception om fødselsnummer mangler")
    void skal_kaste_exception_om_fnr_mangler() {
        var request = new JournalføringRestTjeneste.OppdaterBrukerDto("1234", null);
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

        var request = new JournalføringRestTjeneste.OppdaterBrukerDto(expectedJournalpostId, expectedFnr);
        var journalpostDetaljerDto = restTjeneste.oppdaterBruker(request);

        assertThat(journalpostDetaljerDto).isNotNull();
        verify(arkiv).oppdaterJournalpostBruker(expectedJournalpostId, expectedFnr);
        verify(arkiv, times(2)).hentArkivJournalpost(expectedJournalpostId);
    }

    @Test
    @DisplayName("/bruker/oppdater - oppdater med ny Bruker om brukeren er av type UKJENT")
    void skal_oppdatere_bruker_om_satt_til_UKJENT() {
        var expectedJournalpostId = "12334";
        var expectedFnr = "11111122222";

        when(arkiv.hentArkivJournalpost(expectedJournalpostId)).thenReturn(getStandardBuilder(expectedJournalpostId, "test", Bruker.BrukerIdType.UKJENT).build());

        var request = new JournalføringRestTjeneste.OppdaterBrukerDto(expectedJournalpostId, expectedFnr);
        var journalpostDetaljerDto = restTjeneste.oppdaterBruker(request);

        assertThat(journalpostDetaljerDto).isNotNull();
        verify(arkiv).oppdaterJournalpostBruker(expectedJournalpostId, expectedFnr);
        verify(arkiv, times(2)).hentArkivJournalpost(expectedJournalpostId);
    }

    @Test
    @DisplayName("/bruker/oppdater - oppdater med ny Bruker om brukeren er av type ORGNR")
    void skal_oppdatere_bruker_om_satt_til_ORGNR() {
        var expectedJournalpostId = "12334";
        var expectedFnr = "11111122222";

        when(arkiv.hentArkivJournalpost(expectedJournalpostId)).thenReturn(getStandardBuilder(expectedJournalpostId, "123456789", Bruker.BrukerIdType.ORGNR).build());

        var request = new JournalføringRestTjeneste.OppdaterBrukerDto(expectedJournalpostId, expectedFnr);
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

        when(arkiv.hentArkivJournalpost(expectedJournalpostId)).thenReturn(getStandardBuilder(expectedJournalpostId, brukerAktørId, Bruker.BrukerIdType.AKTOERID).build());
        when(pdl.hentPersonIdentForAktørId(brukerAktørId)).thenReturn(Optional.of(expectedFnr));

        var request = new JournalføringRestTjeneste.OppdaterBrukerDto(expectedJournalpostId, expectedFnr);
        var journalpostDetaljerDto = restTjeneste.oppdaterBruker(request);

        assertThat(journalpostDetaljerDto).isNotNull();
        verify(arkiv, times(0)).oppdaterJournalpostBruker(expectedJournalpostId, expectedFnr);
        verify(arkiv, times(2)).hentArkivJournalpost(expectedJournalpostId);
    }

    @Test
    @DisplayName("/oppgave/reserver - exception om avreserverer en reservasjon til en annen bruker.")
    void skal_kaste_exception_hvis_oppgave_reservert_an_annen_saksbehandler() {
        var expectedJournalpostId = "123";

        when(oppgaveTjeneste.hentOppgaveFor(any(JournalpostId.class))).thenReturn(
            opprettOppgave("12334", LocalDate.now(), "test", "7070", "John", YtelseType.FP));

        var request = new JournalføringRestTjeneste.ReserverOppgaveDto(expectedJournalpostId,null);

        Exception ex;
        try (var utilities = Mockito.mockStatic(KontekstHolder.class)) {
            utilities.when(KontekstHolder::getKontekst).thenReturn(new TestKontekst("Mike"));
            assertThat(KontekstHolder.getKontekst().getUid()).isEqualTo("Mike");
            ex = assertThrows(TekniskException.class, () -> restTjeneste.oppgaveReserver(request));
        }

        assertThat(ex).isNotNull();
        assertThat(ex.getMessage()).contains("Kan ikke avreservere en oppgave som allerede tilhører til en annen saksbehandler.");

        verify(oppgaveTjeneste, never()).reserverOppgaveFor(any(), anyString());
        verify(oppgaveTjeneste, never()).avreserverOppgaveFor(any());
    }

    @Test
    @DisplayName("/oppgave/reserver - skal kunne avreservere sin egen oppgave.")
    void skal_kunne_avreservere_en_ledig_oppgave() {
        var expectedJournalpostId = "123";
        var oppgave = opprettOppgave(expectedJournalpostId, LocalDate.now(), "test", "7070", "John", YtelseType.FP);

        when(oppgaveTjeneste.hentOppgaveFor(any(JournalpostId.class))).thenReturn(oppgave);

        var request = new JournalføringRestTjeneste.ReserverOppgaveDto(expectedJournalpostId, "");
        Response response;

        try (var utilities = Mockito.mockStatic(KontekstHolder.class)) {
            utilities.when(KontekstHolder::getKontekst).thenReturn(new TestKontekst("John"));
            assertThat(KontekstHolder.getKontekst().getUid()).isEqualTo("John");
            response = restTjeneste.oppgaveReserver(request);
        }

        assertThat(response).isNotNull();
        assertThat(response.getStatusInfo()).isEqualTo(Response.Status.OK);

        verify(oppgaveTjeneste).avreserverOppgaveFor(oppgave);
        verify(oppgaveTjeneste, never()).reserverOppgaveFor(any(), anyString());
    }

    private ArkivJournalpost opprettJournalpost(String journalpostId) {
        return getStandardBuilder(journalpostId, null, null).build();
    }

    private static ArkivJournalpost.Builder getStandardBuilder(String journalpostId, String brukerAktørId, Bruker.BrukerIdType brukerIdType) {
        return ArkivJournalpost.getBuilder()
            .medJournalpostId(journalpostId)
            .medBrukerAktørId(Bruker.BrukerIdType.AKTOERID.equals(brukerIdType) ? brukerAktørId : null)
            .medHovedtype(DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL)
            .medTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER)
            .medTilstand(Journalstatus.MOTTATT)
            .medJournalpost(opprettOriginalJournalpost(journalpostId, brukerAktørId, brukerIdType));
    }

    private static Journalpost opprettOriginalJournalpost(String journalpostId, String brukerAktørId, Bruker.BrukerIdType brukerIdType) {
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
            brukerAktørId != null ? new Bruker(brukerAktørId, Optional.ofNullable(brukerIdType).orElse(Bruker.BrukerIdType.AKTOERID)): null,
            null,
            null,
            List.of(),
            List.of(new DokumentInfo("555", "tittel", "brevkode", null, null)));
    }


    private static Oppgave opprettOppgave(String expectedId, LocalDate now, String beskrivelse, String enhetsNr, String reservertAv,
                                          YtelseType ytelseType) {
        return Oppgave.builder()
            .medJournalpostId(expectedId)
            .medStatus(Oppgavestatus.AAPNET)
            .medTildeltEnhetsnr(enhetsNr)
            .medAktørId("aktørId")
            .medYtelseType(ytelseType)
            .medBeskrivelse(beskrivelse)
            .medTilordnetRessurs(reservertAv)
            .medAktivDato(now)
            .medFristFerdigstillelse(now)
            .medKilde(Oppgave.Kilde.GOSYS)
            .medOppgaveId(expectedId)
            .build();
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
