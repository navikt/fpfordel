package no.nav.foreldrepenger.mottak.infotrygd.rest;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AvsluttetSak {
    private final LocalDate iverksatt;
    private final LocalDate stoppdato;
    private final List<Utbetaling> utbetalinger;

    @JsonCreator
    public AvsluttetSak(
            @JsonProperty("iverksatt") LocalDate iverksatt,
            @JsonProperty("stoppdato") LocalDate stoppdato,
            @JsonProperty("utbetalinger") List<Utbetaling> utbetalinger) {
        this.iverksatt = iverksatt;
        this.stoppdato = stoppdato;
        this.utbetalinger = Optional.ofNullable(utbetalinger).orElse(Collections.emptyList());
    }

    public LocalDate getIverksatt() {
        return iverksatt;
    }

    public LocalDate getStoppdato() {
        return stoppdato;
    }

    public List<Utbetaling> getUtbetalinger() {
        return utbetalinger;
    }

    @Override
    public int hashCode() {
        return Objects.hash(stoppdato, utbetalinger, iverksatt);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof AvsluttetSak)) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        AvsluttetSak that = (AvsluttetSak) obj;
        return Objects.equals(that.utbetalinger, this.utbetalinger) &&
                Objects.equals(that.iverksatt, this.iverksatt) &&
                Objects.equals(that.stoppdato, this.stoppdato);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[iverksatt=" + iverksatt + ", stoppdato=" + stoppdato + ", utbetalinger="
                + utbetalinger + "]";
    }

}
