package no.nav.foreldrepenger.mottak.gsak.api;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.util.Optional;

import no.nav.foreldrepenger.fordel.kodeverk.Fagsystem;
import no.nav.foreldrepenger.fordel.kodeverk.Tema;

public class GsakSak {

    private final String brukerFnr;
    private final String sakId;
    private final Tema tema; // tilsvarer fagområde i GSak
    private final Fagsystem fagsystem;
    private final LocalDate sistEndret;

    public GsakSak(String brukerFnr, String sakId, Tema tema, Fagsystem fagsystem) {
        this(brukerFnr, sakId, tema, fagsystem, null);
    }

    public GsakSak(String brukerFnr, String sakId, Tema tema, Fagsystem fagsystem, LocalDate sistEndret) {
        this.brukerFnr = requireNonNull(brukerFnr, "brukerFnr er påkrevd.");
        this.sakId = requireNonNull(sakId, "sakId er påkrevd.");
        this.tema = requireNonNull(tema, "tema er påkrevd.");
        this.fagsystem = requireNonNull(fagsystem, "fagsystem er påkrevd.");
        this.sistEndret = null;
    }

    public String getBrukerFnr() {
        return brukerFnr;
    }

    public String getSakId() {
        return sakId;
    }

    public Tema getTema() {
        return tema;
    }

    public Fagsystem getFagsystem() {
        return fagsystem;
    }

    public Optional<LocalDate> getSistEndret() {
        return Optional.ofNullable(sistEndret);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[brukerFnr=" + brukerFnr + ", sakId=" + sakId + ", tema=" + tema
                + ", fagsystem=" + fagsystem + ", sistEndret=" + sistEndret + "]";
    }
}
