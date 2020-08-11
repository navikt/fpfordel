package no.nav.foreldrepenger.fordel.web.app.selftest.checks;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.codahale.metrics.health.HealthCheck;

import no.nav.foreldrepenger.fordel.web.app.selftest.checks.ExtHealthCheck.InternalResult;

public class ExtHealthCheckTest {

    private MyExtHealthCheck check; // objektet vi tester

    private ExtHealthCheck.InternalResult internalResult;

    @Before
    public void setup() {
        check = new MyExtHealthCheck();
        internalResult = null;
    }

    @Test
    public void test_check_healthy() {
        internalResult = new InternalResult();
        internalResult.setOk(true);

        HealthCheck.Result result = check.check();

        assertThat(result.isHealthy()).isTrue();
        assertThat(result.getError()).isNull();
        assertThat(result.getMessage()).isNull();
    }

    @Test
    public void test_check_unhealthyWithMessage() {
        internalResult = new InternalResult();
        internalResult.setOk(false);
        internalResult.setMessage("alltid min feil");
        //internalResult.setException(new RuntimeException(("au")));

        HealthCheck.Result result = check.check();

        assertThat(result.isHealthy()).isFalse();
        assertThat(result.getError()).isNull();
        assertThat(result.getMessage()).isEqualTo("alltid min feil");
    }

    @Test
    public void test_check_unhealthyWithException() {
        internalResult = new InternalResult();
        internalResult.setOk(false);
        internalResult.setException(new RuntimeException(("auda")));

        HealthCheck.Result result = check.check();

        assertThat(result.isHealthy()).isFalse();
        assertThat(result.getError()).isNotNull();
        assertThat(result.getMessage()).isNotNull();
    }

    //-------

    private class MyExtHealthCheck extends ExtHealthCheck {

        @Override
        protected String getDescription() {
            return "my test";
        }

        @Override
        protected String getEndpoint() {
            return "http://my.test";
        }

        @Override
        protected InternalResult performCheck() {
            return internalResult;
        }
    }
}
