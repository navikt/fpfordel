package no.nav.foreldrepenger.mottak.infotrygd.rest;

import java.time.LocalDate;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class IkkeStartetSak {

    private final LocalDate iverksatt;
    private final LocalDate registrert;

    @JsonCreator
    public IkkeStartetSak(@JsonProperty("iverksatt") LocalDate iverksatt,
            @JsonProperty("registrert") LocalDate registrert) {
        this.iverksatt = iverksatt;
        this.registrert = registrert;
    }

    public LocalDate getIverksatt() {
        return iverksatt;
    }

    public LocalDate getRegistrert() {
        return registrert;
    }

    @Override
    public int hashCode() {
        return Objects.hash(iverksatt, registrert);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof IkkeStartetSak)) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        IkkeStartetSak that = (IkkeStartetSak) obj;
        return Objects.equals(that.registrert, this.registrert) &&
                Objects.equals(that.iverksatt, this.iverksatt);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[iverksatt=" + iverksatt + ", registrert=" + registrert + "]";
    }

}
