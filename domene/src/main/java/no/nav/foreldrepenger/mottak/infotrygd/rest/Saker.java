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
    private final List<LøpendeSak> løpendeSaker;
    private final AvsluttedeSaker avsluttedeSaker;
    private final List<IkkeStartetSak> ikkeStartedeSaker;

    @JsonCreator
    public Saker(
            @JsonProperty("info") String info,
            @JsonProperty("saker") List<Sak> saker,
            @JsonProperty("løpendeSaker") @JsonAlias("apneSakerMedLopendeUtbetaling") List<LøpendeSak> løpendeSaker,
            @JsonProperty("avsluttedeSaker") AvsluttedeSaker avsluttedeSaker,
            @JsonProperty("ikkeStartet") List<IkkeStartetSak> ikkeStartet) {
        this.info = info;
        this.saker = Optional.ofNullable(saker).orElse(emptyList());
        this.løpendeSaker = Optional.ofNullable(løpendeSaker).orElse(emptyList());
        this.avsluttedeSaker = avsluttedeSaker;
        this.ikkeStartedeSaker = Optional.ofNullable(ikkeStartet).orElse(emptyList());
    }

    public String getInfo() {
        return info;
    }

    public List<Sak> getSaker() {
        return saker;
    }

    public List<IkkeStartetSak> getIkkeStartedeSaker() {
        return ikkeStartedeSaker;
    }

    public List<LøpendeSak> getLøpendeSaker() {
        return løpendeSaker;
    }

    public AvsluttedeSaker getAvsluttedeSaker() {
        return avsluttedeSaker;
    }

    @Override
    public int hashCode() {
        return Objects.hash(saker, løpendeSaker, avsluttedeSaker, ikkeStartedeSaker, info);
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
                Objects.equals(that.ikkeStartedeSaker, this.ikkeStartedeSaker) &&
                Objects.equals(that.avsluttedeSaker, this.avsluttedeSaker) &&
                Objects.equals(that.info, this.info) &&
                Objects.equals(that.løpendeSaker, this.løpendeSaker);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[info=" + info + ", saker=" + saker + ", løpendeSaker=" + løpendeSaker
                + ", avsluttedeSaker=" + avsluttedeSaker + ", ikkeStartedeSaker=" + ikkeStartedeSaker + "]";
    }

}
