package no.nav.foreldrepenger.fordel.web.app.selftest;

import com.codahale.metrics.health.HealthCheck;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SelftestResultat {

    public enum AggregateResult {
        OK(0), ERROR(1), WARNING(2);

        private int intValue;

        AggregateResult(int intValue) {
            this.intValue = intValue;
        }

        int getIntValue() {
            return intValue;
        }
    }

    private String application;
    private String version;
    private String revision;
    private LocalDateTime timestamp;
    private String buildTime;
    private List<HealthCheck.Result> kritiskeResultater = new ArrayList<>();
    private List<HealthCheck.Result> ikkeKritiskeResultater = new ArrayList<>();

    public void leggTilResultatForKritiskTjeneste(HealthCheck.Result resultat) {
        kritiskeResultater.add(resultat);
    }

    public void leggTilResultatForIkkeKritiskTjeneste(HealthCheck.Result resultat) {
        ikkeKritiskeResultater.add(resultat);
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getRevision() {
        return revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    public String getBuildTime() {
        return buildTime;
    }

    public void setBuildTime(String buildTime) {
        this.buildTime = buildTime;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public AggregateResult getAggregateResult() {
        for (HealthCheck.Result result : kritiskeResultater) {
            if (!result.isHealthy()) {
                return AggregateResult.ERROR;
            }
        }
        for (HealthCheck.Result result : ikkeKritiskeResultater) {
            if (!result.isHealthy()) {
                return AggregateResult.WARNING;
            }
        }
        return AggregateResult.OK;
    }

    public List<HealthCheck.Result> getIkkeKritiskeResultater() {
        return ikkeKritiskeResultater;
    }

    public List<HealthCheck.Result> getKritiskeResultater() {
        return kritiskeResultater;
    }

    public List<HealthCheck.Result> getAlleResultater() {
        List<HealthCheck.Result> alle = new ArrayList<>();
        alle.addAll(kritiskeResultater);
        alle.addAll(ikkeKritiskeResultater);
        return alle;
    }
}
