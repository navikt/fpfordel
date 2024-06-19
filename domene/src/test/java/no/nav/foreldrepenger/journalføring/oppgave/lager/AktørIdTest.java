package no.nav.foreldrepenger.journalføring.oppgave.lager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AktørIdTest {

    protected static final String GYLDIG_AKTØRID = "0123456789123";
    protected static final String IKKE_GYLDIG_AKTØRID = "ikke_gyldig";

    @Test
    void okBrukerIdTest() {
        var bruker = new AktørId(GYLDIG_AKTØRID);
        assertThat(bruker.getId()).isEqualTo(GYLDIG_AKTØRID);
    }

    @Test
    void nokBrukerIdTest() {
        var ex = assertThrows(IllegalArgumentException.class, () -> new AktørId(IKKE_GYLDIG_AKTØRID));
        assertThat(ex.getMessage()).contains("Ugyldig aktørId");
    }

    @Test
    void brukerGyldigTest() {
        assertTrue(AktørId.erGyldigBrukerId(GYLDIG_AKTØRID));
    }

    @Test
    void ikkeGyldigBrukerTest() {
        assertFalse(AktørId.erGyldigBrukerId(IKKE_GYLDIG_AKTØRID));
    }

    @Test
    void testMaskering() {
        var toString = new AktørId(GYLDIG_AKTØRID).toString();
        assertThat(toString)
            .doesNotContain(GYLDIG_AKTØRID)
            .containsSubsequence("******");
    }
}
