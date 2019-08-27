package no.nav.foreldrepenger.fordel.web.app.selftest;

import com.codahale.metrics.health.HealthCheck;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import no.nav.foreldrepenger.fordel.web.app.selftest.SelftestResultat;
import no.nav.foreldrepenger.fordel.web.app.selftest.SelftestsJsonSerializerModule;
import no.nav.foreldrepenger.fordel.web.app.selftest.checks.ExtHealthCheck;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class SelftestsJsonSerializerModuleTest {

    private ObjectMapper objectMapper;

    @Before
    public void setup() throws IOException {
        objectMapper = (new ObjectMapper()).registerModule(new SelftestsJsonSerializerModule());
    }

    @Test
    public void test() throws IOException {
        SelftestResultat samletResultat = createPopulatedOverallResult();
        ObjectWriter objectWriter = objectMapper.writer();

        String json = objectWriter.writeValueAsString(samletResultat);

        assertThat(json != null).isTrue();
        assertThat(isValidJSON(json)).isTrue();
        assertThat(json.contains("myapp")).isTrue();
        assertThat(json.contains("http://ws.nav.no")).isTrue();
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

    public static boolean isValidJSON(String jsonInString) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            mapper.readTree(jsonInString);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
