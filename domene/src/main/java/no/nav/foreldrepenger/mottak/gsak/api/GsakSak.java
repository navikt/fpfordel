package no.nav.foreldrepenger.mottak.gsak.api;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.util.Optional;

import no.nav.foreldrepenger.fordel.kodeverk.Fagsystem;
import no.nav.foreldrepenger.fordel.kodeverk.Tema;

public class GsakSak {

    private String brukerFnr;
    private String sakId;
    private Tema tema; // tilsvarer fagområde i GSak
    private Fagsystem fagsystem;
    private LocalDate sistEndret;

    public GsakSak(String brukerFnr, String sakId, Tema tema, Fagsystem fagsystem) {
        requireNonNull(brukerFnr, "brukerFnr er påkrevd.");
        requireNonNull(sakId, "sakId er påkrevd.");
        requireNonNull(tema, "tema er påkrevd.");
        requireNonNull(fagsystem, "fagsystem er påkrevd.");

        this.brukerFnr = brukerFnr;
        this.sakId = sakId;
        this.tema = tema;
        this.fagsystem = fagsystem;
    }

    public GsakSak(String brukerFnr, String sakId, Tema tema, Fagsystem fagsystem, LocalDate sistEndret) {
        requireNonNull(brukerFnr, "brukerFnr er påkrevd.");
        requireNonNull(sakId, "sakId er påkrevd.");
        requireNonNull(tema, "tema er påkrevd.");
        requireNonNull(fagsystem, "fagsystem er påkrevd.");

        this.brukerFnr = brukerFnr;
        this.sakId = sakId;
        this.tema = tema;
        this.fagsystem = fagsystem;
        this.sistEndret = sistEndret;
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
}

