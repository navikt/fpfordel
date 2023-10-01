package no.nav.foreldrepenger.journalføring.oppgave.domene;

import java.util.Optional;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.journalføring.domene.JournalpostId;
import no.nav.foreldrepenger.journalføring.oppgave.lager.BrukerId;

public class NyOppgaveBuilder {
    private JournalpostId journalpostId;
    private String enhetId;
    private BrukerId aktørId;
    private String saksref;
    private BehandlingTema behandlingTema;
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
        this.aktørId = Optional.ofNullable(aktørId).map(BrukerId::new).orElse(null);
        return this;
    }

    public NyOppgaveBuilder medAktørId(BrukerId aktørId) {
        this.aktørId = aktørId;
        return this;
    }

    public NyOppgaveBuilder medSaksref(String saksref) {
        this.saksref = saksref;
        return this;
    }

    public NyOppgaveBuilder medBehandlingTema(BehandlingTema behandlingTema) {
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
