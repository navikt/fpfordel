package no.nav.foreldrepenger.mottak.infotrygd.rest;

import java.time.LocalDate;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Utbetaling {
    private final int gradering;
    private final LocalDate utbetaltFom;
    private final LocalDate utbetaltTom;

    @JsonCreator
    public Utbetaling(
            @JsonProperty("gradering") int gradering,
            @JsonProperty("utbetaltFom") LocalDate utbetaltFom,
            @JsonProperty("utbetaltTom") LocalDate utbetaltTom) {
        this.gradering = gradering;
        this.utbetaltFom = utbetaltFom;
        this.utbetaltTom = utbetaltTom;
    }

    public int getGradering() {
        return gradering;
    }

    public LocalDate getUtbetaltFom() {
        return utbetaltFom;
    }

    public LocalDate getUtbetaltTom() {
        return utbetaltTom;
    }

    @Override
    public int hashCode() {
        return Objects.hash(gradering, utbetaltFom, utbetaltTom);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Utbetaling)) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        Utbetaling that = (Utbetaling) obj;
        return Objects.equals(that.gradering, this.gradering) &&
                Objects.equals(that.utbetaltFom, this.utbetaltFom) &&
                Objects.equals(that.utbetaltTom, this.utbetaltTom);

    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[gradering=" + gradering + ", utbetaltFom=" + utbetaltFom
                + ", utbetaltTom=" + utbetaltTom + "]";
    }

}
