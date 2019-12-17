package no.nav.foreldrepenger.mottak.infotrygd;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

public class InfotrygdSak {
    private final LocalDate iverksatt;
    private final LocalDate registrert;

    public InfotrygdSak(LocalDate iverksatt, LocalDate registrert) { // NOSONAR
        this.iverksatt = iverksatt;
        this.registrert = registrert;
    }

    public Optional<LocalDate> getIverksatt() {
        return Optional.ofNullable(iverksatt);
    }

    public LocalDate getRegistrert() {
        return registrert;
    }

    @Override
    public int hashCode() {
        return Objects.hash(iverksatt);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof InfotrygdSak)) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        InfotrygdSak that = (InfotrygdSak) obj;

        return Objects.equals(that.iverksatt, this.iverksatt);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[iverksatt=" + iverksatt + "]";
    }

}
