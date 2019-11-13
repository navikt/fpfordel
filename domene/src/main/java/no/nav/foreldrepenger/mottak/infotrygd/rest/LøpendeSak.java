package no.nav.foreldrepenger.mottak.infotrygd.rest;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class LøpendeSak {

    private final LocalDate iverksatt;
    private final List<Utbetaling> utbetalinger;

    @JsonCreator
    public LøpendeSak(@JsonProperty("iverksatt") LocalDate iverksatt,
            @JsonProperty("utbetalinger") List<Utbetaling> utbetalinger) {
        this.iverksatt = iverksatt;
        this.utbetalinger = Optional.ofNullable(utbetalinger).orElse(Collections.emptyList());
    }

    public LocalDate getIverksatt() {
        return iverksatt;
    }

    public List<Utbetaling> getUtbetalinger() {
        return utbetalinger;
    }

    @Override
    public int hashCode() {
        return Objects.hash(iverksatt, utbetalinger);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof LøpendeSak)) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        LøpendeSak that = (LøpendeSak) obj;
        return Objects.equals(that.iverksatt, this.iverksatt) &&
                Objects.equals(that.utbetalinger, this.utbetalinger);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[iverksatt=" + iverksatt + ", utbetalinger=" + utbetalinger + "]";
    }
}
