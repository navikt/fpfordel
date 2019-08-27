package no.nav.foreldrepenger.fordel.web.app.util;

import org.junit.Test;

import no.nav.foreldrepenger.fordel.web.app.util.FoedselsnummerUtil;

import static org.assertj.core.api.Assertions.assertThat;

public class FoedselsnummerUtilTest {

    @Test
    public void gyldigFoedselsnummer(){
        String foedselsnummer = "07077048167";
        boolean gyldig = FoedselsnummerUtil.gyldigFoedselsnummer(foedselsnummer);
        assertThat(gyldig).isEqualTo(true);
    }

    @Test
    public void ugyldigFoedselsnummer() {
        String foedselsnummer = "07077048199";
        boolean gyldig = FoedselsnummerUtil.gyldigFoedselsnummer(foedselsnummer);
        assertThat(gyldig).isEqualTo(false);

        foedselsnummer = "9999999999";
        gyldig = FoedselsnummerUtil.gyldigFoedselsnummer(foedselsnummer);
        assertThat(gyldig).isEqualTo(false);
    }
}
