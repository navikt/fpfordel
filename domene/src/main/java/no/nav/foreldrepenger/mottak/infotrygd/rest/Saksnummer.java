package no.nav.foreldrepenger.mottak.infotrygd.rest;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Saksnummer {
    private final String blokk;
    private final int nr;

    @JsonCreator
    public Saksnummer(@JsonProperty("blokk") String blokk, @JsonProperty("nr") int nr) {
        this.blokk = blokk;
        this.nr = nr;
    }

    public String getBlokk() {
        return blokk;
    }

    public int getNr() {
        return nr;
    }

    @Override
    public int hashCode() {
        return Objects.hash(blokk, nr);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Saksnummer)) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        Saksnummer that = (Saksnummer) obj;
        return Objects.equals(that.blokk, this.blokk) &&
                Objects.equals(that.nr, this.nr);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[blokk=" + blokk + ", nr=" + nr + "]";
    }

}
