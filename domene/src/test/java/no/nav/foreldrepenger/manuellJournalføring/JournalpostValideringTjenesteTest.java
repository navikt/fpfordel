package no.nav.foreldrepenger.manuellJournalføring;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.kontrakter.fordel.SaksnummerDto;
import no.nav.foreldrepenger.mottak.journal.ArkivJournalpost;
import no.nav.foreldrepenger.mottak.journal.ArkivTjeneste;
import no.nav.foreldrepenger.mottak.klient.Fagsak;
import no.nav.foreldrepenger.mottak.klient.FagsakJournalFøringDto;
import no.nav.foreldrepenger.typer.AktørId;
import no.nav.foreldrepenger.typer.JournalpostId;
import no.nav.vedtak.exception.FunksjonellException;
import no.nav.vedtak.exception.TekniskException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JournalpostValideringTjenesteTest {

    @Mock
    private ArkivTjeneste arkivTjeneste;
    @Mock
    private Fagsak fagsak;
    @Mock
    private ArkivJournalpost journalpost;

    private static final JournalpostId JOURNALPOST_ID = new JournalpostId(987654L);
    private static final AktørId AKTØR_ID = new AktørId(1234567890123L);
    private JournalpostValideringTjeneste tjeneste;

    @BeforeEach
    void setup() {
        tjeneste = new JournalpostValideringTjeneste(arkivTjeneste, fagsak);
    }

    @Test
    @DisplayName("Exception om oppgitt BehandlingTema er ugyldig.")
    void kast_exception_om_behandlingsteam_ikke_finnes() {
        var exception = assertThrows(TekniskException.class, () -> {
            tjeneste.validerKonsistensMedSak(JOURNALPOST_ID, "ikke_finnes", AKTØR_ID);
        });

        String expectedMessage = "FP-34236:Ugyldig input: Behandlingstema med verdi: ikke_finnes er ugyldig input.";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    @DisplayName("Exception om oppgitt BehandlingTema gjelder ikke en FP ytelse.")
    void kast_exception_om_behandlingsteam_finnes_men_er_ikke_relatert_til_fpsak_tjeneste() {
        String omsOffisiellKode = BehandlingTema.OMS.getOffisiellKode();

        var exception = assertThrows(TekniskException.class, () -> {
            tjeneste.validerKonsistensMedSak(JOURNALPOST_ID, omsOffisiellKode, AKTØR_ID);
        });

        String expectedMessage = "FP-34237:Ugyldig input: Behandlingstema med verdi: "+omsOffisiellKode+" er ugyldig input.";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    @DisplayName("OK om førstegangssøknad dokument gjelder riktig ytelse.")
    void ok_hvis_søknad_fra_selvbetjening_og_behandldingtema_lik_dokument_tema() {

        when(arkivTjeneste.hentArkivJournalpost(anyString())).thenReturn(journalpost);
        when(journalpost.getHovedtype()).thenReturn(DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL);

        tjeneste.validerKonsistensMedSak(JOURNALPOST_ID, BehandlingTema.FORELDREPENGER.getOffisiellKode(), AKTØR_ID);
    }

    @Test
    @DisplayName("Ny sak kan kun opprettes for inntektsmeldinger uten sak eller førstegangssøknader.")
    void funksjonell_exception_hvis_søknad_fra_selvbetjening_og_behandldingtema_ulik_dokument_tema() {

        when(arkivTjeneste.hentArkivJournalpost(anyString())).thenReturn(journalpost);
        when(journalpost.getHovedtype()).thenReturn(DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL);

        var exception = assertThrows(FunksjonellException.class, () -> {
            tjeneste.validerKonsistensMedSak(JOURNALPOST_ID, BehandlingTema.SVANGERSKAPSPENGER.getOffisiellKode(), AKTØR_ID);
        });

        String expectedMessage = "FP-785359:Dokument og valgt ytelsetype i uoverenstemmelse";
        String actualMessage = exception.getMessage();

        String expectedLøsningsforslag = "Velg ytelsetype som samstemmer med dokument";
        String løsningsforslag = exception.getLøsningsforslag();

        assertTrue(actualMessage.contains(expectedMessage));
        assertTrue(løsningsforslag.contains(expectedLøsningsforslag));
    }

    @Test
    @DisplayName("Exception om foreldrepenger inntektsmelding men det opprettes en svangenskapspenger sak. Om det ikke finnes en aktiv sak allerede.")
    void funksjonell_exception_hvis_fp_inntektsmelding_men_valgt_behandling_tema_er_svp() {

        when(arkivTjeneste.hentArkivJournalpost(anyString())).thenReturn(journalpost);
        when(journalpost.getHovedtype()).thenReturn(DokumentTypeId.INNTEKTSMELDING);
        when(journalpost.getStrukturertPayload()).thenReturn("ytelse>FORELDREPENGER<");

        var exception = assertThrows(FunksjonellException.class, () -> {
            tjeneste.validerKonsistensMedSak(JOURNALPOST_ID, BehandlingTema.SVANGERSKAPSPENGER.getOffisiellKode(), AKTØR_ID);
        });

        String expectedMessage = "FP-785359:Dokument og valgt ytelsetype i uoverenstemmelse";
        String actualMessage = exception.getMessage();

        String expectedLøsningsforslag = "Velg ytelsetype som samstemmer med dokument";
        String løsningsforslag = exception.getLøsningsforslag();

        assertTrue(actualMessage.contains(expectedMessage));
        assertTrue(løsningsforslag.contains(expectedLøsningsforslag));
    }

    @Test
    @DisplayName("Exception om sak skal opprettes med det finnes en sak allerede. Gjelder kun foreldrepenger.")
    void teknisk_exception_hvis_fp_inntektsmelding_men_åpen_sak_finnes() {

        when(arkivTjeneste.hentArkivJournalpost(anyString())).thenReturn(journalpost);
        when(journalpost.getHovedtype()).thenReturn(DokumentTypeId.INNTEKTSMELDING);
        when(journalpost.getStrukturertPayload()).thenReturn("YTELSE>FORELDREPENGER<");

        List<FagsakJournalFøringDto> brukersFagsaker = List.of(
                opprettFagsakInfo("FP", "LOP"),
                opprettFagsakInfo("FP", "AVSLU"),
                opprettFagsakInfo("SVP", "LOP"));
        when(fagsak.hentBrukersSaker(eq(AKTØR_ID.getId()))).thenReturn(brukersFagsaker);

        var exception = assertThrows(TekniskException.class, () -> {
            tjeneste.validerKonsistensMedSak(JOURNALPOST_ID, BehandlingTema.FORELDREPENGER.getOffisiellKode(), AKTØR_ID);
        });

        String expectedMessage = "FP-34238:Kan ikke journalføre FP inntektsmelding på en ny sak fordi det finnes en aktiv foreldrepenger sak allerede.";
        String actualMessage = exception.getMessage();


        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    @DisplayName("Exception om svangerskapspenger inntektsmelding men det opprettes en foreldrepenger sak. Om det ikke finnes en aktiv sak allerede.")
    void funksjonell_exception_hvis_svp_inntektsmelding_men_valgt_behandling_tema_er_fp() {

        when(arkivTjeneste.hentArkivJournalpost(anyString())).thenReturn(journalpost);
        when(journalpost.getHovedtype()).thenReturn(DokumentTypeId.INNTEKTSMELDING);

        when(journalpost.getStrukturertPayload()).thenReturn("ytelse>SVANGERSKAPSPENGER<");

        var exception = assertThrows(FunksjonellException.class, () -> {
            tjeneste.validerKonsistensMedSak(JOURNALPOST_ID, BehandlingTema.FORELDREPENGER.getOffisiellKode(), AKTØR_ID);
        });

        String expectedMessage = "FP-785359:Dokument og valgt ytelsetype i uoverenstemmelse";
        String actualMessage = exception.getMessage();

        String expectedLøsningsforslag = "Velg ytelsetype som samstemmer med dokument";
        String løsningsforslag = exception.getLøsningsforslag();

        assertTrue(actualMessage.contains(expectedMessage));
        assertTrue(løsningsforslag.contains(expectedLøsningsforslag));
    }

    private FagsakJournalFøringDto opprettFagsakInfo(String ytelseType, String ytelseStatus) {
        return new FagsakJournalFøringDto(new SaksnummerDto("12345"), ytelseType, LocalDate.now(), LocalDate.now(), ytelseStatus);
    }
}