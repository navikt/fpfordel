package no.nav.foreldrepenger.journalføring.oppgave.domene;

import java.time.LocalDate;
import java.util.Objects;

import no.nav.foreldrepenger.journalføring.oppgave.lager.YtelseType;

public class OppgaveBuilder {
    private String oppgaveId;
    private String journalpostId;
    private String aktørId;
    private YtelseType ytelseType;
    private String tildeltEnhetsnr;
    private LocalDate fristFerdigstillelse;
    private LocalDate aktivDato;
    private Oppgavestatus status;
    private String beskrivelse;
    private String tilordnetRessurs;
    private Oppgave.Kilde kilde;

    OppgaveBuilder() {
    }

    public OppgaveBuilder medOppgaveId(String oppgaveId) {
        this.oppgaveId = oppgaveId;
        return this;
    }

    public OppgaveBuilder medJournalpostId(String id) {
        this.journalpostId = id;
        return this;
    }

    public OppgaveBuilder medAktørId(String aktørId) {
        this.aktørId = aktørId;
        return this;
    }

    public OppgaveBuilder medYtelseType(YtelseType ytelseType) {
        this.ytelseType = ytelseType;
        return this;
    }

    public OppgaveBuilder medTildeltEnhetsnr(String tildeltEnhetsnr) {
        this.tildeltEnhetsnr = tildeltEnhetsnr;
        return this;
    }

    public OppgaveBuilder medFristFerdigstillelse(LocalDate fristFerdigstillelse) {
        this.fristFerdigstillelse = fristFerdigstillelse;
        return this;
    }

    public OppgaveBuilder medAktivDato(LocalDate aktivDato) {
        this.aktivDato = aktivDato;
        return this;
    }

    public OppgaveBuilder medStatus(Oppgavestatus status) {
        this.status = status;
        return this;
    }

    public OppgaveBuilder medBeskrivelse(String beskrivelse) {
        this.beskrivelse = beskrivelse;
        return this;
    }

    public OppgaveBuilder medTilordnetRessurs(String tilordnetRessurs) {
        this.tilordnetRessurs = tilordnetRessurs;
        return this;
    }

    public OppgaveBuilder medKilde(Oppgave.Kilde kilde) {
        this.kilde = kilde;
        return this;
    }

    public Oppgave build() {
        validate();
        return new Oppgave(oppgaveId,
            journalpostId,
            aktørId,
            ytelseType,
            tildeltEnhetsnr,
            fristFerdigstillelse,
            aktivDato,
            status,
            beskrivelse,
            tilordnetRessurs,
            kilde);
    }

    private void validate() {
        Objects.requireNonNull(journalpostId, "journalpostId");
        Objects.requireNonNull(tildeltEnhetsnr, "tildeltEnhetsnr");
        Objects.requireNonNull(aktivDato, "aktivDato");
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(fristFerdigstillelse, "frist");
        Objects.requireNonNull(beskrivelse, "beskrivelse");
        Objects.requireNonNull(kilde, "kilde");
        Objects.requireNonNull(oppgaveId, "oppgaveId");
    }
}
