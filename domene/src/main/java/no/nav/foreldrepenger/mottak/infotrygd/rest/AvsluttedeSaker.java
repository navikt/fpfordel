package no.nav.foreldrepenger.mottak.infotrygd.rest;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AvsluttedeSaker {
    private final LocalDate fraOgMed;
    private final List<AvsluttetSak> saker;

    @JsonCreator
    public AvsluttedeSaker(
            @JsonProperty("fraOgMed") LocalDate fraOgMed,
            @JsonProperty("saker") List<AvsluttetSak> saker) {
        this.fraOgMed = fraOgMed;
        this.saker = Optional.ofNullable(saker).orElse(Collections.emptyList());
    }

    public LocalDate getFraOgMed() {
        return fraOgMed;
    }

    public List<AvsluttetSak> getSaker() {
        return saker;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fraOgMed, saker);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof AvsluttedeSaker)) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        AvsluttedeSaker that = (AvsluttedeSaker) obj;
        return Objects.equals(that.fraOgMed, this.fraOgMed) &&
                Objects.equals(that.saker, this.saker);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[fraOgMed=" + fraOgMed + ", saker=" + saker + "]";
    }

}
