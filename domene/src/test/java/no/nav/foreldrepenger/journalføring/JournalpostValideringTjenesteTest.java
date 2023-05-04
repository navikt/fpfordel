package no.nav.foreldrepenger.journalføring;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import java.time.LocalDate;
import java.util.List;

import no.nav.foreldrepenger.mottak.klient.FagsakStatusDto;
import no.nav.foreldrepenger.mottak.klient.FamilieHendelseTypeDto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.mottak.klient.FagsakYtelseTypeDto;
import no.nav.foreldrepenger.kontrakter.fordel.SaksnummerDto;
import no.nav.foreldrepenger.mottak.journal.ArkivJournalpost;
import no.nav.foreldrepenger.mottak.journal.ArkivTjeneste;
import no.nav.foreldrepenger.mottak.klient.AktørIdDto;
import no.nav.foreldrepenger.mottak.klient.SakInfoDto;
import no.nav.foreldrepenger.mottak.klient.Fagsak;
import no.nav.foreldrepenger.typer.AktørId;
import no.nav.foreldrepenger.typer.JournalpostId;
import no.nav.vedtak.exception.FunksjonellException;
import no.nav.vedtak.exception.TekniskException;

@ExtendWith(MockitoExtension.class)
class JournalpostValideringTjenesteTest {

    private static final JournalpostId JOURNALPOST_ID = new JournalpostId(987654L);
    private static final AktørId AKTØR_ID = new AktørId(1234567890123L);
    @Mock
    private ArkivTjeneste arkivTjeneste;
    @Mock
    private Fagsak fagsak;
    @Mock
    private ArkivJournalpost journalpost;
    private ManuellOpprettSakValidator tjeneste;

    @BeforeEach
    void setup() {
        tjeneste = new ManuellOpprettSakValidator(arkivTjeneste, fagsak);
    }

    @Test
    @DisplayName("Exception om oppgitt YtelseType er null.")
    void kast_exception_om_ytelseType_ikke_finnes() {
        var exception = assertThrows(NullPointerException.class, () -> tjeneste.validerKonsistensMedSak(JOURNALPOST_ID, null, AKTØR_ID, null));

        var expectedMessage = "Ugyldig input: YtelseType kan ikke være null ved opprettelse av en sak";
        var actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }


    @Test
    @DisplayName("Exception om oppgitt JournalpostId er null.")
    void kast_exception_om_journalpostId_ikke_finnes() {
        var exception = assertThrows(NullPointerException.class, () -> tjeneste.validerKonsistensMedSak(null, FagsakYtelseTypeDto.SVANGERSKAPSPENGER, AKTØR_ID,
            null));

        var expectedMessage = "Ugyldig input: JournalpostId kan ikke være null ved opprettelse av en sak";
        var actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    @DisplayName("Exception om oppgitt AktørId er null.")
    void kast_exception_om_aktørId_ikke_finnes() {
        var exception = assertThrows(NullPointerException.class, () -> tjeneste.validerKonsistensMedSak(JOURNALPOST_ID, FagsakYtelseTypeDto.ENGANGSTØNAD, null,
            null));

        var expectedMessage = "Ugyldig input: AktørId kan ikke være null ved opprettelse av en sak";
        var actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    @DisplayName("OK om førstegangssøknad dokument gjelder riktig ytelse.")
    void ok_hvis_søknad_fra_selvbetjening_og_behandldingtema_lik_dokument_tema() {

        when(arkivTjeneste.hentArkivJournalpost(anyString())).thenReturn(journalpost);
        when(journalpost.getHovedtype()).thenReturn(DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL);

        tjeneste.validerKonsistensMedSak(JOURNALPOST_ID, FagsakYtelseTypeDto.FORELDREPENGER, AKTØR_ID, null);

        verify(arkivTjeneste, times(1)).hentArkivJournalpost(anyString());
    }

    @Test
    @DisplayName("Ny sak kan kun opprettes for inntektsmeldinger uten sak eller for førstegangssøknader.")
    void funksjonell_exception_hvis_søknad_fra_selvbetjening_og_behandldingtema_ulik_dokument_tema() {

        when(arkivTjeneste.hentArkivJournalpost(anyString())).thenReturn(journalpost);
        when(journalpost.getHovedtype()).thenReturn(DokumentTypeId.SØKNAD_SVANGERSKAPSPENGER);

        var offisiellKode = FagsakYtelseTypeDto.ENGANGSTØNAD;

        var exception = assertThrows(FunksjonellException.class, () -> tjeneste.validerKonsistensMedSak(JOURNALPOST_ID, offisiellKode, AKTØR_ID, null));

        var expectedMessage = "FP-785359:Dokument og valgt ytelsetype i uoverenstemmelse";
        var actualMessage = exception.getMessage();

        var expectedLøsningsforslag = "Velg ytelsetype som samstemmer med dokument";
        var løsningsforslag = exception.getLøsningsforslag();

        assertTrue(actualMessage.contains(expectedMessage));
        assertTrue(løsningsforslag.contains(expectedLøsningsforslag));
    }

    @Test
    @DisplayName("Exception om foreldrepenger inntektsmelding men det opprettes en svangenskapspenger sak. Om det ikke finnes en aktiv sak allerede.")
    void funksjonell_exception_hvis_fp_inntektsmelding_men_valgt_behandling_tema_er_svp() {

        when(arkivTjeneste.hentArkivJournalpost(anyString())).thenReturn(journalpost);
        when(journalpost.getHovedtype()).thenReturn(DokumentTypeId.INNTEKTSMELDING);
        when(journalpost.getStrukturertPayload()).thenReturn("ytelse>FORELDREPENGER<");

        var offisiellKode = FagsakYtelseTypeDto.SVANGERSKAPSPENGER;
        var exception = assertThrows(FunksjonellException.class, () -> tjeneste.validerKonsistensMedSak(JOURNALPOST_ID, offisiellKode, AKTØR_ID, null));

        var expectedMessage = "FP-785359:Dokument og valgt ytelsetype i uoverenstemmelse";
        var actualMessage = exception.getMessage();

        var expectedLøsningsforslag = "Velg ytelsetype som samstemmer med dokument";
        var løsningsforslag = exception.getLøsningsforslag();

        assertTrue(actualMessage.contains(expectedMessage));
        assertTrue(løsningsforslag.contains(expectedLøsningsforslag));
    }

    @Test
    @DisplayName("Exception om sak skal opprettes med det finnes en sak allerede. Gjelder kun foreldrepenger.")
    void teknisk_exception_hvis_fp_inntektsmelding_men_åpen_sak_finnes() {

        when(arkivTjeneste.hentArkivJournalpost(anyString())).thenReturn(journalpost);
        when(journalpost.getHovedtype()).thenReturn(DokumentTypeId.INNTEKTSMELDING);
        when(journalpost.getStrukturertPayload()).thenReturn("YTELSE>FORELDREPENGER<");

        var brukersFagsaker = List.of(opprettSakInfo(FagsakYtelseTypeDto.FORELDREPENGER, FagsakStatusDto.LØPENDE),
            opprettSakInfo(FagsakYtelseTypeDto.FORELDREPENGER, FagsakStatusDto.AVSLUTTET),
            opprettSakInfo(FagsakYtelseTypeDto.SVANGERSKAPSPENGER, FagsakStatusDto.LØPENDE));
        when(fagsak.hentBrukersSaker(new AktørIdDto(AKTØR_ID.getId()))).thenReturn(brukersFagsaker);

        var offisiellKode = FagsakYtelseTypeDto.FORELDREPENGER;
        var exception = assertThrows(TekniskException.class, () -> tjeneste.validerKonsistensMedSak(JOURNALPOST_ID, offisiellKode, AKTØR_ID, null));

        var expectedMessage = "FP-34238:Kan ikke journalføre FP inntektsmelding på en ny sak fordi det finnes en aktiv foreldrepenger sak allerede.";
        var actualMessage = exception.getMessage();


        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    @DisplayName("Exception om svangerskapspenger inntektsmelding men det opprettes en foreldrepenger sak. Om det ikke finnes en aktiv sak allerede.")
    void funksjonell_exception_hvis_svp_inntektsmelding_men_valgt_behandling_tema_er_fp() {

        when(arkivTjeneste.hentArkivJournalpost(anyString())).thenReturn(journalpost);
        when(journalpost.getHovedtype()).thenReturn(DokumentTypeId.INNTEKTSMELDING);

        when(journalpost.getStrukturertPayload()).thenReturn("ytelse>SVANGERSKAPSPENGER<");

        var offisiellKode = FagsakYtelseTypeDto.FORELDREPENGER;
        var exception = assertThrows(FunksjonellException.class, () -> tjeneste.validerKonsistensMedSak(JOURNALPOST_ID, offisiellKode, AKTØR_ID, null));

        var expectedMessage = "FP-785359:Dokument og valgt ytelsetype i uoverenstemmelse";
        var actualMessage = exception.getMessage();

        var expectedLøsningsforslag = "Velg ytelsetype som samstemmer med dokument";
        var løsningsforslag = exception.getLøsningsforslag();

        assertTrue(actualMessage.contains(expectedMessage));
        assertTrue(løsningsforslag.contains(expectedLøsningsforslag));
    }

    @Test
    @DisplayName("Ny sak kan kun opprettes for førstegangssøknader.")
    void funksjonell_exception_hvis_endring_søknad() {

        when(arkivTjeneste.hentArkivJournalpost(anyString())).thenReturn(journalpost);
        when(journalpost.getHovedtype()).thenReturn(DokumentTypeId.FORELDREPENGER_ENDRING_SØKNAD);

        var offisiellKode = FagsakYtelseTypeDto.FORELDREPENGER;

        var exception = assertThrows(FunksjonellException.class, () -> tjeneste.validerKonsistensMedSak(JOURNALPOST_ID, offisiellKode, AKTØR_ID, null));

        var expectedMessage = "FP-785359:Dokument og valgt ytelsetype i uoverenstemmelse";
        var actualMessage = exception.getMessage();

        var expectedLøsningsforslag = "Velg ytelsetype som samstemmer med dokument";
        var løsningsforslag = exception.getLøsningsforslag();

        assertTrue(actualMessage.contains(expectedMessage));
        assertTrue(løsningsforslag.contains(expectedLøsningsforslag));
    }

    @Test
    @DisplayName("OK om ny dokumentTypeId gjelder søknad")
    void ok_hvis_nyDokumentTypeId_og_gjelder_søknad() {

        when(arkivTjeneste.hentArkivJournalpost(anyString())).thenReturn(journalpost);
        when(journalpost.getHovedtype()).thenReturn(DokumentTypeId.ANNET);

        tjeneste.validerKonsistensMedSak(JOURNALPOST_ID, FagsakYtelseTypeDto.SVANGERSKAPSPENGER, AKTØR_ID, DokumentTypeId.SØKNAD_SVANGERSKAPSPENGER);

        verify(arkivTjeneste, times(1)).hentArkivJournalpost(anyString());
    }

    private SakInfoDto opprettSakInfo(FagsakYtelseTypeDto fagSakYtelseTypeDto, FagsakStatusDto ytelseStatus) {
        return new SakInfoDto(new SaksnummerDto("12345"),
            fagSakYtelseTypeDto, LocalDate.now(), ytelseStatus, new SakInfoDto.FamiliehendelseInfoDto(LocalDate.now(), FamilieHendelseTypeDto.FØDSEL), LocalDate.now());
    }
}
