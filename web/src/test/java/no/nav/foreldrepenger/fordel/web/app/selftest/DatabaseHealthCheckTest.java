package no.nav.foreldrepenger.fordel.web.app.selftest;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import no.nav.foreldrepenger.mottak.extensions.JpaExtension;

@ExtendWith(JpaExtension.class)
class DatabaseHealthCheckTest {

    @Test
    void test_working_query() throws Exception {
        assertThat(new DatabaseHealthCheck().isReady()).isTrue();
    }
}
