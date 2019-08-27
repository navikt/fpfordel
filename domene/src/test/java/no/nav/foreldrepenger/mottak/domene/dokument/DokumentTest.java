package no.nav.foreldrepenger.mottak.domene.dokument;

import static no.nav.foreldrepenger.fordel.kodeverk.DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL;
import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.Charset;
import java.util.Base64;
import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import no.nav.foreldrepenger.fordel.kodeverk.ArkivFilType;

public class DokumentTest {

    private static final UUID FORSENDELSE_ID = UUID.randomUUID();
    private static final String TEST_STRING = "Test";
    private static final byte[] TEST_BYTES = TEST_STRING.getBytes(Charset.forName("UTF-8"));

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

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

        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Utviklerfeil");

        dokument.getKlartekstDokument();
    }

    private Dokument lagDokument(ArkivFilType arkivFilType) {
        return Dokument.builder()
                .setForsendelseId(FORSENDELSE_ID)
                .setDokumentTypeId(SØKNAD_FORELDREPENGER_FØDSEL)
                .setHovedDokument(true)
                .setDokumentInnhold(TEST_BYTES, arkivFilType)
                .build();
    }

}
