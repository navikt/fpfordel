package no.nav.foreldrepenger.fordel.kodeverk;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class KonfigVerdiTypeTest {

    @Test
    public void skal_validere_BOOLEAN_verdi() throws Exception {
        assertThat(KonfigVerdiType.BOOLEAN_TYPE.erGyldigFormat("J")).isTrue();
        assertThat(KonfigVerdiType.BOOLEAN_TYPE.erGyldigFormat("N")).isTrue();

        // uvant, men vi holder oss til norske koder her
        assertThat(KonfigVerdiType.BOOLEAN_TYPE.erGyldigFormat("True")).isFalse();
        assertThat(KonfigVerdiType.BOOLEAN_TYPE.erGyldigFormat("False")).isFalse();

    }

    @Test
    public void skal_validere_INTEGER_verdi() throws Exception {
        assertThat(KonfigVerdiType.INTEGER_TYPE.erGyldigFormat("1")).isTrue();
        assertThat(KonfigVerdiType.INTEGER_TYPE.erGyldigFormat("0")).isTrue();
        assertThat(KonfigVerdiType.INTEGER_TYPE.erGyldigFormat("-1")).isTrue();
        assertThat(KonfigVerdiType.INTEGER_TYPE.erGyldigFormat("-2313131")).isTrue();
        assertThat(KonfigVerdiType.INTEGER_TYPE.erGyldigFormat("+10000")).isTrue();

        assertThat(KonfigVerdiType.INTEGER_TYPE.erGyldigFormat("13333330000")).isFalse();
        assertThat(KonfigVerdiType.INTEGER_TYPE.erGyldigFormat("133adf33330000")).isFalse();
    }

    @Test
    public void skal_validere_STRING_verdi() throws Exception {
        assertThat(KonfigVerdiType.STRING_TYPE.erGyldigFormat("J")).isTrue();
    }

    @Test
    public void skal_validere_PERIOD_verdi() throws Exception {
        assertThat(KonfigVerdiType.PERIOD_TYPE.erGyldigFormat("P10M")).isTrue();
        assertThat(KonfigVerdiType.PERIOD_TYPE.erGyldigFormat("PT10M")).isFalse();

        assertThat(KonfigVerdiType.PERIOD_TYPE.erGyldigFormat("PX10M")).isFalse();
    }

    @Test
    public void skal_validere_DURATION_verdi() throws Exception {
        assertThat(KonfigVerdiType.DURATION_TYPE.erGyldigFormat("P10M")).isFalse();
        assertThat(KonfigVerdiType.DURATION_TYPE.erGyldigFormat("PT10M")).isTrue();
        assertThat(KonfigVerdiType.DURATION_TYPE.erGyldigFormat("P3DT10M")).isTrue();

        assertThat(KonfigVerdiType.DURATION_TYPE.erGyldigFormat("PX10M")).isFalse();
    }

    @Test
    public void skal_validere_URI_verdi() throws Exception {
        assertThat(KonfigVerdiType.URI_TYPE.erGyldigFormat("http://java.sun.com/j2se/1.3/")).isTrue();
        assertThat(KonfigVerdiType.URI_TYPE.erGyldigFormat(":abc.d")).isFalse();

    }
}
