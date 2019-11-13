package no.nav.foreldrepenger.mottak.infotrygd.rest;

import static java.util.Collections.emptyList;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Saker {
    private final String info;
    private final List<Sak> saker;
    private final List<LøpendeSak> åpneSaker;
    private final AvsluttedeSaker avsluttedeSaker;

    @JsonCreator
    public Saker(
            @JsonProperty("info") String info,
            @JsonProperty("saker") List<Sak> saker,
            @JsonProperty("åpneSaker") @JsonAlias("apneSakerMedLopendeUtbetaling") List<LøpendeSak> åpneSaker,
            @JsonProperty("avsluttedeSaker") AvsluttedeSaker avsluttedeSaker) {
        this.info = info;
        this.saker = Optional.ofNullable(saker).orElse(emptyList());
        this.åpneSaker = Optional.ofNullable(åpneSaker).orElse(emptyList());
        this.avsluttedeSaker = avsluttedeSaker;
    }

    public String getInfo() {
        return info;
    }

    public List<Sak> getSaker() {
        return saker;
    }

    public List<LøpendeSak> getÅpneSaker() {
        return åpneSaker;
    }

    public AvsluttedeSaker getAvsluttedeSaker() {
        return avsluttedeSaker;
    }

    @Override
    public int hashCode() {
        return Objects.hash(saker, åpneSaker, avsluttedeSaker, info);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Saker)) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        Saker that = (Saker) obj;
        return Objects.equals(that.saker, this.saker) &&
                Objects.equals(that.avsluttedeSaker, this.avsluttedeSaker) &&
                Objects.equals(that.info, this.info) &&
                Objects.equals(that.åpneSaker, this.åpneSaker);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[info=" + info + ", saker=" + saker + ", åpneSaker=" + åpneSaker
                + ", avsluttedeSaker=" + avsluttedeSaker + "]";
    }

}
