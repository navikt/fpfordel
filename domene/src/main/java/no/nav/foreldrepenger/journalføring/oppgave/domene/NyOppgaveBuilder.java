package no.nav.foreldrepenger.journalføring.oppgave.domene;

import no.nav.foreldrepenger.journalføring.domene.JournalpostId;

public class NyOppgaveBuilder {
    private JournalpostId journalpostId;
    private String enhetId;
    private String aktørId;
    private String saksref;
    private String behandlingTema;
    private String beskrivelse;

    NyOppgaveBuilder() {
    }

    public NyOppgaveBuilder medJournalpostId(JournalpostId journalpostId) {
        this.journalpostId = journalpostId;
        return this;
    }

    public NyOppgaveBuilder medEnhetId(String enhetId) {
        this.enhetId = enhetId;
        return this;
    }

    public NyOppgaveBuilder medAktørId(String aktørId) {
        this.aktørId = aktørId;
        return this;
    }

    public NyOppgaveBuilder medSaksref(String saksref) {
        this.saksref = saksref;
        return this;
    }

    public NyOppgaveBuilder medBehandlingTema(String behandlingTema) {
        this.behandlingTema = behandlingTema;
        return this;
    }

    public NyOppgaveBuilder medBeskrivelse(String beskrivelse) {
        this.beskrivelse = beskrivelse;
        return this;
    }

    public NyOppgave build() {
        return new NyOppgave(journalpostId, enhetId, aktørId, saksref, behandlingTema, beskrivelse);
    }
}
