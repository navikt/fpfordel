package no.nav.foreldrepenger.fordel.web.app.startupinfo;

import static com.google.common.collect.Maps.fromProperties;
import static no.nav.vedtak.konfig.StandardPropertySource.ENV_PROPERTIES;
import static no.nav.vedtak.konfig.StandardPropertySource.SYSTEM_PROPERTIES;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import no.nav.vedtak.util.env.Environment;

public class EnvironmentTest {

    private static final Environment ENV = Environment.current();

    @Test
    public void test_sysProps() {
        var sysProps = fromProperties(ENV.getProperties(SYSTEM_PROPERTIES).getVerdier());
        assertThat(sysProps).isNotNull();
        assertThat(sysProps.get("java.version")).isNotNull();
    }

    @Test
    public void test_envVars() {
        var envVars = fromProperties(ENV.getProperties(ENV_PROPERTIES).getVerdier());
        assertThat(envVars).isNotNull();
        assertThat(envVars.size()).isGreaterThanOrEqualTo(1);
    }

}
