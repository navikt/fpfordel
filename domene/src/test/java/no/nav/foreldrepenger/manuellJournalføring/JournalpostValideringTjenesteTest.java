package no.nav.foreldrepenger.manuellJournalføring;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.mottak.journal.ArkivDokument;
import no.nav.foreldrepenger.mottak.journal.ArkivJournalpost;
import no.nav.foreldrepenger.mottak.journal.ArkivTjeneste;
import no.nav.foreldrepenger.mottak.klient.Fagsak;
import no.nav.foreldrepenger.typer.AktørId;
import no.nav.foreldrepenger.typer.JournalpostId;
import no.nav.vedtak.exception.FunksjonellException;
import no.nav.vedtak.exception.TekniskException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JournalpostValideringTjenesteTest {

    @Mock
    private ArkivTjeneste arkivTjeneste;
    @Mock
    private Fagsak fagsak;
    @Mock
    private ArkivJournalpost journalpost;

    private JournalpostId journalpostId;
    private AktørId aktørId;
    private JournalpostValideringTjeneste tjeneste;

    @BeforeEach
    void setup() {
        tjeneste = new JournalpostValideringTjeneste(arkivTjeneste, fagsak);

        journalpostId = new JournalpostId(987654L);
        aktørId = new AktørId(1234567890123L);
    }

    @Test
    @DisplayName("Exception om oppgitt BehandlingTema er ugyldig.")
    void kast_exception_om_behandlingsteam_ikke_finnes() {
        var exception = assertThrows(TekniskException.class, () -> {
            tjeneste.validerKonsistensMedSak(journalpostId, "ikke_finnes", aktørId);
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
            tjeneste.validerKonsistensMedSak(journalpostId, omsOffisiellKode, aktørId);
        });

        String expectedMessage = "FP-34237:Ugyldig input: Behandlingstema med verdi: "+omsOffisiellKode+" er ugyldig input.";
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    @DisplayName("OK om førstegangssøknad dokument gjelder riktig ytelse.")
    void ok_hvis_søknad_fra_selvbetjening_og_behandldingtema_lik_dokument_tema() {

        when(arkivTjeneste.hentJournalpostForSak(anyString())).thenReturn(Optional.of(journalpost));
        when(journalpost.getHovedDokument()).thenReturn(opprettMockDokument(DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL));

        tjeneste.validerKonsistensMedSak(journalpostId, BehandlingTema.FORELDREPENGER.getOffisiellKode(), aktørId);
    }

    @Test
    @DisplayName("Ny sak kan kun opprettes for inntektsmeldinger uten sak eller førstegangssøknader.")
    void funksjonell_exception_hvis_søknad_fra_selvbetjening_og_behandldingtema_ulik_dokument_tema() {

        when(arkivTjeneste.hentJournalpostForSak(anyString())).thenReturn(Optional.of(journalpost));
        when(journalpost.getHovedDokument()).thenReturn(opprettMockDokument(DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL));

        var exception = assertThrows(FunksjonellException.class, () -> {
            tjeneste.validerKonsistensMedSak(journalpostId, BehandlingTema.SVANGERSKAPSPENGER.getOffisiellKode(), aktørId);
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

        when(arkivTjeneste.hentJournalpostForSak(anyString())).thenReturn(Optional.of(journalpost));
        when(journalpost.getHovedDokument()).thenReturn(opprettMockDokument(DokumentTypeId.INNTEKTSMELDING));

        when(arkivTjeneste.hentStrukturertDokument(eq(journalpostId.getVerdi()), anyString())).thenReturn("ytelse>FORELDREPENGER<");

        var exception = assertThrows(FunksjonellException.class, () -> {
            tjeneste.validerKonsistensMedSak(journalpostId, BehandlingTema.SVANGERSKAPSPENGER.getOffisiellKode(), aktørId);
        });

        String expectedMessage = "FP-785359:Dokument og valgt ytelsetype i uoverenstemmelse";
        String actualMessage = exception.getMessage();

        String expectedLøsningsforslag = "Velg ytelsetype som samstemmer med dokument";
        String løsningsforslag = exception.getLøsningsforslag();

        assertTrue(actualMessage.contains(expectedMessage));
        assertTrue(løsningsforslag.contains(expectedLøsningsforslag));
    }

    @Test
    @Disabled
    @DisplayName("Exception om sak skal opprettes med det finnes en sak allerede. Gjelder kun foreldrepenger.")
    void teknisk_exception_hvis_fp_inntektsmelding_men_åpen_sak_finnes() {

        when(arkivTjeneste.hentJournalpostForSak(anyString())).thenReturn(Optional.of(journalpost));
        when(journalpost.getHovedDokument()).thenReturn(opprettMockDokument(DokumentTypeId.INNTEKTSMELDING));

        when(arkivTjeneste.hentStrukturertDokument(eq(journalpostId.getVerdi()), anyString())).thenReturn("YTELSE>FORELDREPENGER<");
        // TODO Sladek: Utkommenter da herAktivSak er på plass
        //when(fagsak.harAktivSak(eq(aktørId.getId()), any(BehandlingTema.class))).thenReturn(true);

        var exception = assertThrows(TekniskException.class, () -> {
            tjeneste.validerKonsistensMedSak(journalpostId, BehandlingTema.FORELDREPENGER.getOffisiellKode(), aktørId);
        });

        String expectedMessage = "FP-34238:Kan ikke journalføre FP inntektsmelding på en ny sak fordi det finnes en aktiv foreldrepenger sak allerede.";
        String actualMessage = exception.getMessage();


        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    @DisplayName("Exception om svangerskapspenger inntektsmelding men det opprettes en foreldrepenger sak. Om det ikke finnes en aktiv sak allerede.")
    void funksjonell_exception_hvis_svp_inntektsmelding_men_valgt_behandling_tema_er_fp() {

        when(arkivTjeneste.hentJournalpostForSak(anyString())).thenReturn(Optional.of(journalpost));
        when(journalpost.getHovedDokument()).thenReturn(opprettMockDokument(DokumentTypeId.INNTEKTSMELDING));

        when(arkivTjeneste.hentStrukturertDokument(eq(journalpostId.getVerdi()), anyString())).thenReturn("ytelse>SVANGERSKAPSPENGER<");

        var exception = assertThrows(FunksjonellException.class, () -> {
            tjeneste.validerKonsistensMedSak(journalpostId, BehandlingTema.FORELDREPENGER.getOffisiellKode(), aktørId);
        });

        String expectedMessage = "FP-785359:Dokument og valgt ytelsetype i uoverenstemmelse";
        String actualMessage = exception.getMessage();

        String expectedLøsningsforslag = "Velg ytelsetype som samstemmer med dokument";
        String løsningsforslag = exception.getLøsningsforslag();

        assertTrue(actualMessage.contains(expectedMessage));
        assertTrue(løsningsforslag.contains(expectedLøsningsforslag));
    }

    private ArkivDokument opprettMockDokument(DokumentTypeId dokumentTypeId) {
        return ArkivDokument.Builder.ny()
                .medDokumentTypeId(dokumentTypeId)
                .medDokumentId("123")
                .build();
    }
}