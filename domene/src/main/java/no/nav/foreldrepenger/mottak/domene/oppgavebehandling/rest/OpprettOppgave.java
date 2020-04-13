package no.nav.foreldrepenger.mottak.domene.oppgavebehandling.rest;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class OpprettOppgave {

    @JsonProperty("tildeltEnhetsnr")
    private String tildeltEnhetsnr;
    @JsonProperty("opprettetAvEnhetsnr")
    private String opprettetAvEnhetsnr;
    @JsonProperty("journalpostId")
    private String journalpostId;
    @JsonProperty("behandlesAvApplikasjon")
    private String behandlesAvApplikasjon;
    @JsonProperty("saksreferanse")
    private String saksreferanse;
    @JsonProperty("aktoerId")
    private String aktoerId;
    @JsonProperty("beskrivelse")
    private String beskrivelse;
    @JsonProperty("temagruppe")
    private String temagruppe;
    @JsonProperty("tema")
    private String tema;
    @JsonProperty("behandlingstema")
    private String behandlingstema;
    @JsonProperty("oppgavetype")
    private String oppgavetype;
    @JsonProperty("behandlingstype")
    private String behandlingstype;
    @JsonProperty("aktivDato")
    private LocalDate aktivDato;
    @JsonProperty("prioritet")
    private Prioritet prioritet;
    @JsonProperty("fristFerdigstillelse")
    private LocalDate fristFerdigstillelse;


    public OpprettOppgave() {
    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public static class Builder {
        OpprettOppgave oppgave;
        Builder() {
            oppgave = new OpprettOppgave();
        }

        public Builder medTildeltEnhetsnr(String tildeltEnhetsnr) {
            this.oppgave.tildeltEnhetsnr = tildeltEnhetsnr;
            return this;
        }

        public Builder medOpprettetAvEnhetsnr(String opprettetAvEnhetsnr) {
            this.oppgave.opprettetAvEnhetsnr = opprettetAvEnhetsnr;
            return this;
        }

        public Builder medJournalpostId(String journalpostId) {
            this.oppgave.journalpostId = journalpostId;
            return this;
        }

        public Builder medBehandlesAvApplikasjon(String behandlesAvApplikasjon) {
            this.oppgave.behandlesAvApplikasjon = behandlesAvApplikasjon;
            return this;
        }

        public Builder medSaksreferanse(String saksreferanse) {
            this.oppgave.saksreferanse = saksreferanse;
            return this;
        }

        public Builder medAktoerId(String aktoerId) {
            this.oppgave.aktoerId = aktoerId;
            return this;
        }

        public Builder medBeskrivelse(String beskrivelse) {
            this.oppgave.beskrivelse = beskrivelse;
            return this;
        }

        public Builder medTemagruppe(String temagruppe) {
            this.oppgave.temagruppe = temagruppe;
            return this;
        }

        public Builder medTema(String tema) {
            this.oppgave.tema = tema;
            return this;
        }

        public Builder medBehandlingstema(String behandlingstema) {
            this.oppgave.behandlingstema = behandlingstema;
            return this;
        }

        public Builder medOppgavetype(String oppgavetype) {
            this.oppgave.oppgavetype = oppgavetype;
            return this;
        }

        public Builder medBehandlingstype(String behandlingstype) {
            this.oppgave.behandlingstype = behandlingstype;
            return this;
        }

        public Builder medAktivDato(LocalDate aktivDato) {
            this.oppgave.aktivDato = aktivDato;
            return this;
        }

        public Builder medFristFerdigstillelse(LocalDate fristFerdigstillelse) {
            this.oppgave.fristFerdigstillelse = fristFerdigstillelse;
            return this;
        }

        public Builder medPrioritet(Prioritet prioritet) {
            this.oppgave.prioritet = prioritet;
            return this;
        }

        public OpprettOppgave build() {
            return this.oppgave;
        }
    }

    @Override
    public String toString() {
        return "OpprettOppgave{" +
                "tildeltEnhetsnr='" + tildeltEnhetsnr + '\'' +
                ", opprettetAvEnhetsnr='" + opprettetAvEnhetsnr + '\'' +
                ", journalpostId='" + journalpostId + '\'' +
                ", behandlesAvApplikasjon='" + behandlesAvApplikasjon + '\'' +
                ", saksreferanse='" + saksreferanse + '\'' +
                ", aktoerId='" + aktoerId + '\'' +
                ", beskrivelse='" + beskrivelse + '\'' +
                ", temagruppe='" + temagruppe + '\'' +
                ", tema='" + tema + '\'' +
                ", behandlingstema='" + behandlingstema + '\'' +
                ", oppgavetype='" + oppgavetype + '\'' +
                ", behandlingstype='" + behandlingstype + '\'' +
                ", aktivDato=" + aktivDato +
                ", prioritet=" + prioritet +
                ", fristFerdigstillelse=" + fristFerdigstillelse +
                '}';
    }
}
