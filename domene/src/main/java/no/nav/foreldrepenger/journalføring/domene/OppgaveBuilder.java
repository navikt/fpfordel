package no.nav.foreldrepenger.journalføring.domene;

import java.time.LocalDate;
import java.util.Objects;

import no.nav.foreldrepenger.domene.YtelseType;

public class OppgaveBuilder {
    private String id;
    private String aktoerId;
    private YtelseType ytelseType;
    private String tildeltEnhetsnr;
    private LocalDate fristFerdigstillelse;
    private LocalDate aktivDato;
    private Oppgavestatus status;
    private String beskrivelse;
    private String tilordnetRessurs;

    public OppgaveBuilder medId(String id) {
        this.id = id;
        return this;
    }

    public OppgaveBuilder medAktoerId(String aktoerId) {
        this.aktoerId = aktoerId;
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

    public Oppgave build() {
        validate();
        return new Oppgave(id, aktoerId, ytelseType, tildeltEnhetsnr, fristFerdigstillelse, aktivDato, status, beskrivelse, tilordnetRessurs);
    }

    private void validate() {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(tildeltEnhetsnr, "tildeltEnhetsnr");
        Objects.requireNonNull(aktivDato, "aktivDato");
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(fristFerdigstillelse, "frist");
        Objects.requireNonNull(beskrivelse, "beskrivelse");
    }
}
