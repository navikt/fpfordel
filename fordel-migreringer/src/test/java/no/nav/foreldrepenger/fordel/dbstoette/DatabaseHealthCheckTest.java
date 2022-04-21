package no.nav.foreldrepenger.fordel.dbstoette;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import no.nav.foreldrepenger.mottak.extensions.JpaExtension;

@ExtendWith(JpaExtension.class)
public class DatabaseHealthCheckTest {


    @Test
    public void test_working_query() throws Exception {
        assertThat(new DatabaseHealthCheck().isReady()).isTrue();
    }


}
