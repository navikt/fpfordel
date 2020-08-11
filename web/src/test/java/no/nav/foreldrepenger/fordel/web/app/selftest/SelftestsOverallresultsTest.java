package no.nav.foreldrepenger.fordel.web.app.selftest;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;

import com.codahale.metrics.health.HealthCheck;

import no.nav.foreldrepenger.fordel.web.app.selftest.SelftestResultat.AggregateResult;

public class SelftestsOverallresultsTest {

    private SelftestResultat samletResultat;

    @Before
    public void setup() {
        samletResultat = new SelftestResultat();
    }

    @Test
    public void test_setters_and_getters() {

        samletResultat.setApplication("myapp");
        samletResultat.setVersion("1.9");
        samletResultat.setRevision("revvv");
        LocalDateTime now = LocalDateTime.now();
        samletResultat.setTimestamp(now);
        samletResultat.setBuildTime("last-year");

        assertThat(samletResultat.getApplication()).isEqualTo("myapp");
        assertThat(samletResultat.getVersion()).isEqualTo("1.9");
        assertThat(samletResultat.getRevision()).isEqualTo("revvv");
        assertThat(samletResultat.getTimestamp()).isEqualTo(now);
        assertThat(samletResultat.getBuildTime()).isEqualTo("last-year");
        assertThat(samletResultat.getAlleResultater()).isEmpty();
    }

    @Test
    public void test_aggregateResult_none() {
        assertThat(samletResultat.getAggregateResult()).isEqualTo(AggregateResult.OK);
    }

    @Test
    public void skal_gi_OK_når_alle_tjenester_rapporterer_å_være_friske() {
        samletResultat.leggTilResultatForKritiskTjeneste(HealthCheck.Result.healthy("no1"));
        samletResultat.leggTilResultatForKritiskTjeneste(HealthCheck.Result.healthy("no2"));

        assertThat(samletResultat.getAggregateResult()).isEqualTo(AggregateResult.OK);
    }

    @Test
    public void skal_gi_ERROR_som_endelig_feilkode_når_kritisk_tjeneste_rapporterer_å_være_i_feiltilstand() {
        samletResultat.leggTilResultatForKritiskTjeneste(HealthCheck.Result.unhealthy("no1"));
        samletResultat.leggTilResultatForKritiskTjeneste(HealthCheck.Result.unhealthy("no2"));
        assertThat(samletResultat.getAggregateResult()).isEqualTo(AggregateResult.ERROR);
    }

    @Test
    public void skal_gi_WARN_som_endelig_feilkode_når_ikke_kritisk_tjeneste_rapporterer_å_være_i_feiltilstand() {
        samletResultat.leggTilResultatForIkkeKritiskTjeneste(HealthCheck.Result.unhealthy("no1"));
        samletResultat.leggTilResultatForKritiskTjeneste(HealthCheck.Result.healthy());
        assertThat(samletResultat.getAggregateResult()).isEqualTo(AggregateResult.WARNING);
    }

    @Test
    public void skal_gi_ERROR_som_endelig_feilkode_når_både_kritisk_og_ikke_kritisk_tjeneste_rapporterer_å_være_i_feiltilstand() {
        samletResultat.leggTilResultatForIkkeKritiskTjeneste(HealthCheck.Result.unhealthy("no1"));
        samletResultat.leggTilResultatForKritiskTjeneste(HealthCheck.Result.unhealthy("adsf"));
        assertThat(samletResultat.getAggregateResult()).isEqualTo(AggregateResult.ERROR);
    }

    @Test
    public void skal_gi_ERROR_når_en_tjeneste_er_frisk_og_en_kritisk_tjeneste_er_i_feiltilstand() {
        samletResultat.leggTilResultatForKritiskTjeneste(HealthCheck.Result.healthy("no1"));
        samletResultat.leggTilResultatForKritiskTjeneste(HealthCheck.Result.unhealthy("no2"));

        assertThat(samletResultat.getAggregateResult()).isEqualTo(AggregateResult.ERROR);
    }
}
