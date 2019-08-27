package no.nav.foreldrepenger.fordel.web.app.selftest.checks;

import org.junit.Before;
import org.junit.Test;

import no.nav.foreldrepenger.fordel.web.app.selftest.checks.ExtHealthCheck;
import no.nav.foreldrepenger.fordel.web.app.selftest.checks.WebServiceHealthCheck;

import javax.xml.ws.WebServiceException;

import static org.assertj.core.api.Assertions.assertThat;

public class WebServiceHealthCheckTest {

    private MyWebServiceHealthCheck healthCheck; // objekter vi tester

    private static final String EXCEPTION_MSG = "SOAP is bad";

    @Before
    public void setup() {
        healthCheck = new MyWebServiceHealthCheck();
    }

    @Test
    public void test_performCheck_ok() {
        ExtHealthCheck.InternalResult res = healthCheck.performCheck();

        assertThat(res.isOk()).isTrue();
    }

    @Test
    public void test_performCheck_exception1() {
        healthCheck.setShouldThrowWebServiceException(true);

        ExtHealthCheck.InternalResult res = healthCheck.performCheck();

        assertThat(res.isOk()).isFalse();
        assertThat(res.getMessage()).isNotNull();
        assertThat(res.getException()).isNull();
    }

    @Test
    public void test_performCheck_exception2() {
        healthCheck.setShouldThrowOtherException(true);

        ExtHealthCheck.InternalResult res = healthCheck.performCheck();

        assertThat(res.isOk()).isFalse();
        assertThat(res.getMessage()).isNull();
        assertThat(res.getException()).isNotNull();
    }

    //-----------

    private static class MyWebServiceHealthCheck extends WebServiceHealthCheck {

        private boolean shouldThrowWebServiceException = false;
        private boolean shouldThrowOtherException = false;

        public void setShouldThrowWebServiceException(boolean shouldThrowWebServiceException) {
            this.shouldThrowWebServiceException = shouldThrowWebServiceException;
        }

        public void setShouldThrowOtherException(boolean shouldThrowOtherException) {
            this.shouldThrowOtherException = shouldThrowOtherException;
        }

        @Override
        protected void performWebServiceSelftest() {
            if (shouldThrowWebServiceException) {
                throw new WebServiceException(EXCEPTION_MSG);
            }
            if (shouldThrowOtherException) {
                throw new RuntimeException(EXCEPTION_MSG);
            }
        }

        @Override
        protected String getDescription() {
            return "my test";
        }

        @Override
        protected String getEndpoint() {
            return "http://mytest";
        }
    }
}
