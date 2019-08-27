package no.nav.foreldrepenger.fordel.web.app.selftest.checks;

import no.nav.foreldrepenger.fordel.web.app.selftest.checks.MottakQueueHealthCheck;
import no.nav.foreldrepenger.mottak.queue.MottakAsyncJmsConsumer;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class MottakQueueHealthCheckTest {

    @Test
    public void test_alt() {
        MottakAsyncJmsConsumer mockAsyncJmsConsumer = mock(MottakAsyncJmsConsumer.class);
        MottakQueueHealthCheck check = new MottakQueueHealthCheck(mockAsyncJmsConsumer);

        assertThat(check.getDescriptionSuffix()).isNotNull();

        new MottakQueueHealthCheck(); // som CDI trenger
    }
}
