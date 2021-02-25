package no.nav.foreldrepenger.mottak.infotrygd;

import java.time.LocalDate;
import java.util.Optional;

public record InfotrygdSak(LocalDate iverksatt, LocalDate registrert) {
    public Optional<LocalDate> getIverksatt() {
        return Optional.ofNullable(iverksatt);
    }
}
