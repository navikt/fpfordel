package no.nav.foreldrepenger.fordel.web.app.rest.journalføring;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.Journalstatus;
import no.nav.foreldrepenger.fordel.kodeverdi.Tema;
import no.nav.foreldrepenger.fordel.web.app.exceptions.FeilDto;
import no.nav.foreldrepenger.kontrakter.fordel.JournalpostIdDto;
import no.nav.foreldrepenger.mottak.journal.ArkivJournalpost;
import no.nav.foreldrepenger.mottak.journal.ArkivTjeneste;
import no.nav.foreldrepenger.mottak.journal.saf.Journalpost;
import no.nav.foreldrepenger.mottak.klient.Fagsak;
import no.nav.foreldrepenger.mottak.klient.Los;
import no.nav.foreldrepenger.mottak.klient.TilhørendeEnhetDto;
import no.nav.foreldrepenger.mottak.klient.YtelseTypeDto;
import no.nav.foreldrepenger.mottak.person.PersonInformasjon;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.integrasjon.oppgave.v1.*;
import org.assertj.core.api.ListAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.ws.rs.core.MediaType;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ManuellJournalføringRestTjenesteTest {

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

    private ManuellJournalføringRestTjeneste restTjeneste;

    private final String LIMIT = "50";
    private final SaksbehandlerIdentDto saksbehandlerIdentDto = new SaksbehandlerIdentDto("123456");
    private final TilhørendeEnhetDto tilhørendeEnhetDto = new TilhørendeEnhetDto("9999", "Navn på enhet");

    @BeforeEach
    void setUp() {
        lenient().when(los.hentTilhørendeEnheter(saksbehandlerIdentDto.ident())).thenReturn(List.of(tilhørendeEnhetDto));
        restTjeneste = new ManuellJournalføringRestTjeneste(oppgaver, pdl, arkiv, fagsak, los);
    }

    @Test
    @DisplayName("/oppgaver - ingen oppgaver = tom liste")
    void skal_levere_en_tom_liste_om_ingen_oppgaver_funnet() {

        var oppgaveDtos = restTjeneste.hentÅpneOppgaverForSaksbehandler(saksbehandlerIdentDto);

        assertThat(oppgaveDtos).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("/oppgaver - 1 oppgave = liste med 1 oppgave.")
    void skal_levere_en_liste_med_oppgaver() throws Exception {
        var expectedId = 123L;
        var expectedJournalpostId = "12334";
        var now = LocalDate.now();
        var beskrivelse = "beskrivelse";
        var journalføringOppgaver = List.of(opprettOppgave(expectedId, now, expectedJournalpostId, beskrivelse, BehandlingTema.FORELDREPENGER_ADOPSJON, tilhørendeEnhetDto.enhetsnummer()));

        when(oppgaver.finnÅpneOppgaverForEnhet(Tema.FORELDRE_OG_SVANGERSKAPSPENGER.getOffisiellKode(), List.of(Oppgavetype.JOURNALFØRING.getKode()), tilhørendeEnhetDto.enhetsnummer(), LIMIT)).thenReturn(journalføringOppgaver);

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
        assertThat(oppgave.journalpostHarMangler()).isFalse();
    }

    @Test
    @DisplayName("/oppgaver - 2 oppgaver = liste med 2 oppgaver med ulike enheter.")
    void skal_levere_en_liste_med_oppgaver_på_ulike_enheter() throws Exception {
        var now = LocalDate.now();
        var enhet1 = "111111";
        var enhet2 = "222222";

        when(los.hentTilhørendeEnheter(saksbehandlerIdentDto.ident())).thenReturn(List.of(new TilhørendeEnhetDto(enhet1, "Enhet 1"), new TilhørendeEnhetDto(enhet2, "Enhet 2")));

        var journalføringOppgaveEnhet1 = opprettOppgave(123L, now, "1111", "beskrivelse1", BehandlingTema.FORELDREPENGER_ADOPSJON, enhet1);
        var journalføringOppgaveEnhet2 = opprettOppgave(124L, now, "2222", "beskrivelse2", BehandlingTema.SVANGERSKAPSPENGER, enhet2);

        when(oppgaver.finnÅpneOppgaverForEnhet(Tema.FORELDRE_OG_SVANGERSKAPSPENGER.getOffisiellKode(), List.of(Oppgavetype.JOURNALFØRING.getKode()), enhet1, LIMIT)).thenReturn(List.of(journalføringOppgaveEnhet1));
        when(oppgaver.finnÅpneOppgaverForEnhet(Tema.FORELDRE_OG_SVANGERSKAPSPENGER.getOffisiellKode(), List.of(Oppgavetype.JOURNALFØRING.getKode()), enhet2, LIMIT)).thenReturn(List.of(journalføringOppgaveEnhet2));

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
    void skal_levere_fnr_om_finnes() throws Exception {
        var expectedId = 123L;
        var expectedJournalpostId = "12334";
        var now = LocalDate.now();
        var beskrivelse = "beskrivelse";
        var aktørId = "aktørId";
        var journalføringOppgaver = List.of(opprettOppgave(expectedId, now, expectedJournalpostId, beskrivelse, BehandlingTema.FORELDREPENGER_FØDSEL, tilhørendeEnhetDto.enhetsnummer()));

        when(oppgaver.finnÅpneOppgaverForEnhet(Tema.FORELDRE_OG_SVANGERSKAPSPENGER.getOffisiellKode(), List.of(Oppgavetype.JOURNALFØRING.getKode()), tilhørendeEnhetDto.enhetsnummer(), LIMIT)).thenReturn(journalføringOppgaver);
        var fnr = "12344345678";
        when(pdl.hentPersonIdentForAktørId(aktørId)).thenReturn(Optional.of(fnr));
        var oppgaveDtos = restTjeneste.hentÅpneOppgaverForSaksbehandler(saksbehandlerIdentDto);

        assertThat(oppgaveDtos).isNotNull().hasSize(1);
        var oppgave = oppgaveDtos.get(0);
        assertThat(oppgave.aktørId()).isEqualTo(aktørId);
        assertThat(oppgave.fødselsnummer()).isEqualTo(fnr);
        assertThat(oppgave.ytelseType()).isEqualTo(YtelseTypeDto.FORELDREPENGER);
        assertThat(oppgave.journalpostHarMangler()).isFalse();
    }

    @Test
    @DisplayName("/oppgaver - ytelseType = Ukjent - om ikke FP behandlingTema")
    void skal_ha_ytelse_type_ukjent_om_det_ikke_lar_seg_utlede_fra_behandlingstema() throws Exception {
        var expectedId = 123L;
        var expectedJournalpostId = "12334";
        var now = LocalDate.now();
        var beskrivelse = "beskrivelse";
        var journalføringOppgaver = List.of(opprettOppgave(expectedId, now, expectedJournalpostId, beskrivelse, BehandlingTema.OMS, tilhørendeEnhetDto.enhetsnummer()));

        when(oppgaver.finnÅpneOppgaverForEnhet(Tema.FORELDRE_OG_SVANGERSKAPSPENGER.getOffisiellKode(), List.of(Oppgavetype.JOURNALFØRING.getKode()), tilhørendeEnhetDto.enhetsnummer(), LIMIT)).thenReturn(journalføringOppgaver);
        var oppgaveDtos = restTjeneste.hentÅpneOppgaverForSaksbehandler(saksbehandlerIdentDto);

        assertThat(oppgaveDtos).isNotNull().hasSize(1);
        assertThat(oppgaveDtos.get(0).ytelseType()).isNull();
    }

    @Test
    @DisplayName("/oppgaver - ytelseType = Ukjent - om ikke FP behandlingTema")
    void skal_ha_mangler_om_tittel_er_ukjent() throws Exception {
        var expectedId = 123L;
        var expectedJournalpostId = "12334";
        var now = LocalDate.now();
        var beskrivelse = "Journalføring";
        var journalføringOppgaver = List.of(opprettOppgave(expectedId, now, expectedJournalpostId, beskrivelse, BehandlingTema.OMS, tilhørendeEnhetDto.enhetsnummer()));

        when(oppgaver.finnÅpneOppgaverForEnhet(Tema.FORELDRE_OG_SVANGERSKAPSPENGER.getOffisiellKode(), List.of(Oppgavetype.JOURNALFØRING.getKode()), tilhørendeEnhetDto.enhetsnummer(), LIMIT)).thenReturn(journalføringOppgaver);
        var oppgaveDtos = restTjeneste.hentÅpneOppgaverForSaksbehandler(saksbehandlerIdentDto);

        assertThat(oppgaveDtos).isNotNull().hasSize(1);
        var oppgave = oppgaveDtos.get(0);
        assertThat(oppgave.journalpostHarMangler()).isTrue();
    }

    @Test
    @DisplayName("/oppgaver - ytelseType = Ukjent - om ikke FP behandlingTema")
    void skal_ikke_ha_mangler_om_tittel_er_kjent_og_bruker_ikke_er_null() throws Exception {
        var expectedId = 123L;
        var expectedJournalpostId = "12334";
        var now = LocalDate.now();
        var beskrivelse = "Foreldrepengesøknad";
        var journalføringOppgaver = List.of(opprettOppgave(expectedId, now, expectedJournalpostId, beskrivelse, BehandlingTema.OMS, tilhørendeEnhetDto.enhetsnummer()));

        when(oppgaver.finnÅpneOppgaverForEnhet(Tema.FORELDRE_OG_SVANGERSKAPSPENGER.getOffisiellKode(), List.of(Oppgavetype.JOURNALFØRING.getKode()), tilhørendeEnhetDto.enhetsnummer(), LIMIT)).thenReturn(journalføringOppgaver);
        var oppgaveDtos = restTjeneste.hentÅpneOppgaverForSaksbehandler(saksbehandlerIdentDto);

        assertThat(oppgaveDtos).isNotNull().hasSize(1);
        var oppgave = oppgaveDtos.get(0);
        assertThat(oppgave.journalpostHarMangler()).isFalse();
    }

    @Test
    @DisplayName("/oppgaver - ytelseType = Ukjent - om ikke FP behandlingTema")
    void skal_kutte_beskrivelse_iht_regler() throws Exception {
        var expectedId = 123L;
        var expectedJournalpostId = "12334";
        var now = LocalDate.now();
        var beskrivelse = "--- 17.01.2023 09:44 Duck, Skrue (L568956, 4860) --- printet ut og scannes i bisys --- 17.01.2023 09:37 Duck, Skrue (L568956, 4860) --- Oppgaven er flyttet fra enhet " +
                "4812 til 4860, fra saksbehandler <ingen> til L568956 --- 13.01.2023 08:00 Duck, Donald (B568956, 4812) --- Gjelder farskap --- 12.01.2023 12:30 Dusck, Dolly (R857447, 4806)" +
                " --- Overført rett enhet Oppgaven er flyttet fra enhet 4860 til 4812 Journalføring";
        var journalføringOppgaver = List.of(opprettOppgave(expectedId, now, expectedJournalpostId, beskrivelse, BehandlingTema.OMS, tilhørendeEnhetDto.enhetsnummer()));

        when(oppgaver.finnÅpneOppgaverForEnhet(Tema.FORELDRE_OG_SVANGERSKAPSPENGER.getOffisiellKode(), List.of(Oppgavetype.JOURNALFØRING.getKode()), tilhørendeEnhetDto.enhetsnummer(), LIMIT)).thenReturn(journalføringOppgaver);
        var oppgaveDtos = restTjeneste.hentÅpneOppgaverForSaksbehandler(saksbehandlerIdentDto);

        assertThat(oppgaveDtos).isNotNull().hasSize(1);
        var oppgave = oppgaveDtos.get(0);

        assertThat(oppgave.trimmetBeskrivelse()).isEqualTo( "Journalføring");
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
        assertThat(((FeilDto) entity).feilmelding()).contains(String.format("Dokument ikke funnet for journalpost= %s dokument= %s",
                expectedJournalpostId, expectedDokumentId));
    }

    @Test
    @DisplayName("/oppgave/detaljer - exception om journalpost mangler")
    void skal_kaste_exception_om_journalpost_mangler() {
        var expectedJournalpostId = "12334";

        when(arkiv.hentArkivJournalpost(expectedJournalpostId)).thenReturn(null);

        var ex = assertThrows(TekniskException.class, () -> restTjeneste.hentJournalpostDetaljer(new JournalpostIdDto(expectedJournalpostId)));

        assertThat(ex).isNotNull();
        assertThat(ex.getMessage()).isEqualTo("FORDEL-123:Journapost "+ expectedJournalpostId +" finnes ikke i arkivet.");
    }

    @Test
    @DisplayName("/oppgave/detaljer - returner detaljer.")
    void skal_returnere_detaljer() {
        var expectedJournalpostId = "12334";

        when(arkiv.hentArkivJournalpost(expectedJournalpostId)).thenReturn(
                ArkivJournalpost.getBuilder()
                        .medJournalpostId(expectedJournalpostId)
                        .medTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER)
                        .medTilstand(Journalstatus.MOTTATT)
                        .medJournalpost(new Journalpost(expectedJournalpostId, null, null, null, null, null, null, null, null, null, null, null, null, List.of(), List.of()))
                        .build());

        var ex = restTjeneste.hentJournalpostDetaljer(new JournalpostIdDto(expectedJournalpostId));

        assertThat(ex).isNotNull();
        assertThat(ex.journalpostId()).isEqualTo(expectedJournalpostId);
    }

    @Test
    @DisplayName("/ehnet - returner detaljer.")
    void skal_returnere_ehneter() {
        var expectedIdent = "x452323";

        var enheter = List.of(
                new TilhørendeEnhetDto("9999", "Ni enhet"),
                new TilhørendeEnhetDto("0000", "Null enhet"));

        when(los.hentTilhørendeEnheter(expectedIdent)).thenReturn(enheter);

        var resp = restTjeneste.hentTilhørendeEnhet(new SaksbehandlerIdentDto(expectedIdent));

        assertThat(resp).isNotNull().hasSize(2).isEqualTo(enheter);
    }

    @Test
    @DisplayName("/ehnet - returner exception om empty list.")
    void skal_returnere_exception_om_ingen_enheter_funnet() {
        var expectedIdent = "x452323";

        when(los.hentTilhørendeEnheter(expectedIdent)).thenReturn(List.of());

        var saksbehandlerIdentDto = new SaksbehandlerIdentDto(expectedIdent);

        var ex = assertThrows(IllegalStateException.class, () -> restTjeneste.hentTilhørendeEnhet(saksbehandlerIdentDto));

        assertThat(ex.getMessage()).contains("Det forventes at saksbehandler "+expectedIdent+" har minst en tilførende enhet. Fant ingen.");
    }

    private static Oppgave opprettOppgave(long expectedId, LocalDate now, String expectedJournalpostId, String beskrivelse, BehandlingTema behandlingTema, String enhetsNr) {
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
                beskrivelse);
    }
}