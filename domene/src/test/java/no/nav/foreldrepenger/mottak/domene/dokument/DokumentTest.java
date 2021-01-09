package no.nav.foreldrepenger.mottak.domene.dokument;

import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.Charset;
import java.util.Base64;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.fordel.kodeverdi.ArkivFilType;

public class DokumentTest {

    private static final UUID FORSENDELSE_ID = UUID.randomUUID();
    private static final String TEST_STRING = "Test";
    private static final byte[] TEST_BYTES = TEST_STRING.getBytes(Charset.forName("UTF-8"));

    @Test
    public void test_skalBase64EncodeDokumentInnhold() {
        String forventet = Base64.getEncoder().encodeToString(TEST_BYTES);
        Dokument dokument = lagDokument(ArkivFilType.PDFA);
        assertThat(dokument.getBase64EncodetDokument()).isEqualTo(forventet);
    }

    @Test
    public void test_skalKunneHenteKlartekstAvXML() {
        Dokument dokument = lagDokument(ArkivFilType.XML);
        assertThat(dokument.getKlartekstDokument()).isEqualTo(TEST_STRING);
    }

    @Test
    public void test_skalKasteFeilVedHentingAvKlartekstPåBinærDokument() {
        Dokument dokument = lagDokument(ArkivFilType.PDFA);
        var e = assertThrows(IllegalStateException.class, () -> dokument.getKlartekstDokument());
        assertTrue(e.getMessage().contains("Utviklerfeil"));
    }

    private static Dokument lagDokument(ArkivFilType arkivFilType) {
        return Dokument.builder()
                .setForsendelseId(FORSENDELSE_ID)
                .setDokumentTypeId(SØKNAD_FORELDREPENGER_FØDSEL)
                .setHovedDokument(true)
                .setDokumentInnhold(TEST_BYTES, arkivFilType)
                .build();
    }
}
