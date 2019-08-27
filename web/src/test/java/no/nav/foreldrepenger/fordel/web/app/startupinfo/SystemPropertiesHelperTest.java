package no.nav.foreldrepenger.fordel.web.app.startupinfo;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;

import no.nav.foreldrepenger.fordel.web.app.startupinfo.SystemPropertiesHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;

import static org.assertj.core.api.Assertions.assertThat;

public class SystemPropertiesHelperTest {

    private SystemPropertiesHelper helper; // objektet som testes

    @Before
    public void setup() {
        helper = SystemPropertiesHelper.getInstance();
    }

    @Test
    public void test_sysProps() {
        SortedMap<String, String> sysProps = helper.filteredSortedProperties();

        assertThat(sysProps).isNotNull();
        assertThat(sysProps.get("java.version")).isNotNull();
    }

    @Test
    public void test_envVars() {
        SortedMap<String, String> envVars = helper.filteredSortedEnvVars();

        assertThat(envVars).isNotNull();
        assertThat(envVars.size()).isGreaterThanOrEqualTo(1);
    }

    @Test
    public void skal_filtrere_bort_passord_fra_java_opts() {
        Map<String,String> input = new HashMap<String, String>() {{
            put("JAVA_OPTS", "-Djavax.net.ssl.trustStore=/foo/bar -Djavax.net.ssl.trustStorePassword=passord_i_klartekst  -javaagent:/foo/bar/javaagent.jar  -DapplicationName=dummy -");
        }};

        SystemPropertiesHelper.filter(input);

        Assertions.assertThat(input.get("JAVA_OPTS")).isEqualTo("-Djavax.net.ssl.trustStore=/foo/bar -Djavax.net.ssl.trustStorePassword=*****  -javaagent:/foo/bar/javaagent.jar  -DapplicationName=dummy -");
    }
}
