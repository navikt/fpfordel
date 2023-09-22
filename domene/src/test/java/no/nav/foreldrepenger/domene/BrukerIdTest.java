package no.nav.foreldrepenger.domene;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class BrukerIdTest {

    protected static final String GYLDIG_AKTØRID = "0123456789123";
    protected static final String IKKE_GYLDIG_AKTØRID = "ikke_gyldig";

    @Test
    void okBrukerIdTest() {
        var bruker = new BrukerId(GYLDIG_AKTØRID);
        assertThat(bruker.getId()).isEqualTo(GYLDIG_AKTØRID);
    }

    @Test
    void nokBrukerIdTest() {
        var ex = assertThrows(IllegalArgumentException.class, () -> new BrukerId(IKKE_GYLDIG_AKTØRID));
        assertThat(ex.getMessage()).contains("Ugyldig aktørId");
    }

    @Test
    void brukerGyldigTest() {
        assertTrue(BrukerId.erGyldigBrukerId(GYLDIG_AKTØRID));
    }

    @Test
    void ikkeGyldigBrukerTest() {
        assertFalse(BrukerId.erGyldigBrukerId(IKKE_GYLDIG_AKTØRID));
    }

    @Test
    void testMaskering() {
        var toString = new BrukerId(GYLDIG_AKTØRID).toString();
        assertThat(toString)
            .doesNotContain(GYLDIG_AKTØRID)
            .containsSubsequence("******");
    }
}
