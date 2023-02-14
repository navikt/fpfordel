package no.nav.foreldrepenger.mottak.domene.dokument;

import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.fordel.kodeverdi.ArkivFilType;

class DokumentTest {

    private static final UUID FORSENDELSE_ID = UUID.randomUUID();
    private static final String TEST_STRING = "Test";
    private static final byte[] TEST_BYTES = TEST_STRING.getBytes(StandardCharsets.UTF_8);

    private static Dokument lagDokument(ArkivFilType arkivFilType) {
        return Dokument.builder()
            .setForsendelseId(FORSENDELSE_ID)
            .setDokumentTypeId(SØKNAD_FORELDREPENGER_FØDSEL)
            .setHovedDokument(true)
            .setDokumentInnhold(TEST_BYTES, arkivFilType)
            .build();
    }

    @Test
    void test_skalBase64EncodeDokumentInnhold() {
        assertThat(lagDokument(ArkivFilType.PDFA).getBase64EncodetDokument()).isEqualTo(Base64.getEncoder().encodeToString(TEST_BYTES));
    }

    @Test
    void test_skalKunneHenteKlartekstAvXML() {
        assertThat(lagDokument(ArkivFilType.XML).getKlartekstDokument()).isEqualTo(TEST_STRING);
    }

    @Test
    void test_skalKasteFeilVedHentingAvKlartekstPåBinærDokument() {
        var dokument = lagDokument(ArkivFilType.PDFA);

        var ex = assertThrows(IllegalStateException.class, dokument::getKlartekstDokument);

        assertTrue(ex.getMessage().contains("Utviklerfeil"));
    }
}
