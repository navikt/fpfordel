package no.nav.foreldrepenger.fordel.web.app.selftest;

import com.codahale.metrics.health.HealthCheck;

import no.nav.foreldrepenger.fordel.web.app.selftest.SelftestResultat;
import no.nav.foreldrepenger.fordel.web.app.selftest.SelftestsHtmlFormatter;
import no.nav.foreldrepenger.fordel.web.app.selftest.checks.ExtHealthCheck;

import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class SelftestsHtmlFormatterTest {

    private SelftestsHtmlFormatter formatter;

    @Before
    public void setup() {
        formatter = new SelftestsHtmlFormatter();
    }

    @Test
    public void skal_formattere_typisk_OverallResult() {
        SelftestResultat samletResultat = createPopulatedOverallResult();
        String html = formatter.format(samletResultat);
        assertThat(html).startsWith("<html");
        assertThat(html).contains("descr1");
        assertThat(html).contains("90ms");
        assertThat(html).contains("http://ws.nav.no");
        assertThat(html).contains("UTF-8");
        assertThat(html).endsWith("</html>");
    }

    private SelftestResultat createPopulatedOverallResult() {
        SelftestResultat samletResultat = new SelftestResultat();

        samletResultat.setApplication("myapp");
        samletResultat.setVersion("1.9");
        samletResultat.setRevision("revvv");
        LocalDateTime now = LocalDateTime.now();
        samletResultat.setTimestamp(now);
        samletResultat.setBuildTime("last-year");

        HealthCheck.ResultBuilder builder1 = HealthCheck.Result.builder();
        builder1.healthy();
        builder1.withDetail(ExtHealthCheck.DETAIL_DESCRIPTION, "descr1");
        builder1.withDetail(ExtHealthCheck.DETAIL_RESPONSE_TIME, "90ms");
        builder1.withDetail(ExtHealthCheck.DETAIL_ENDPOINT, "http://ws.nav.no");
        samletResultat.leggTilResultatForKritiskTjeneste(builder1.build());
        samletResultat.leggTilResultatForKritiskTjeneste(HealthCheck.Result.unhealthy("no2"));
        samletResultat.leggTilResultatForKritiskTjeneste(HealthCheck.Result.unhealthy("no3", "arg"));
        samletResultat.leggTilResultatForKritiskTjeneste(HealthCheck.Result.unhealthy(new Exception("foo")));
        return samletResultat;
    }
}
