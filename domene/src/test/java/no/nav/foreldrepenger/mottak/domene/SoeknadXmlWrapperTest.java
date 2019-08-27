package no.nav.foreldrepenger.mottak.domene;

import org.junit.Test;

import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.OpplysningerOmBarn;
import no.nav.vedtak.exception.TekniskException;

public class SoeknadXmlWrapperTest {

    @Test(expected = TekniskException.class)
    public void skal_kaste_exception_ved_ukjent_soeknadsskjema() throws Exception {
        MottattStrukturertDokument.toXmlWrapper(new OpplysningerOmBarn());
    }
}
