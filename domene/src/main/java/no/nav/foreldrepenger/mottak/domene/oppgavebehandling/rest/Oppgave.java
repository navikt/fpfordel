package no.nav.foreldrepenger.mottak.domene.oppgavebehandling.rest;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Oppgave {

    private Long id;
    private String journalpostId;
    private String behandlesAvApplikasjon;
    private String saksreferanse;
    private String aktoerId;
    private String tema;
    private String behandlingstema;
    private String oppgavetype;
    private String behandlingstype;
    private Integer versjon;
    private LocalDate fristFerdigstillelse;
    private LocalDate aktivDato;
    private Prioritet prioritet;
    private Oppgavestatus status;

    @JsonCreator
    public Oppgave(@JsonProperty("id") Long id,
                   @JsonProperty("journalpostId") String journalpostId,
                   @JsonProperty("behandlesAvApplikasjon") String behandlesAvApplikasjon,
                   @JsonProperty("saksreferanse") String saksreferanse,
                   @JsonProperty("aktoerId") String aktoerId,
                   @JsonProperty("tema") String tema,
                   @JsonProperty("behandlingstema") String behandlingstema,
                   @JsonProperty("oppgavetype") String oppgavetype,
                   @JsonProperty("behandlingstype") String behandlingstype,
                   @JsonProperty("versjon") Integer versjon,
                   @JsonProperty("fristFerdigstillelse") LocalDate fristFerdigstillelse,
                   @JsonProperty("aktivDato") LocalDate aktivDato,
                   @JsonProperty("prioritet") Prioritet prioritet,
                   @JsonProperty("status") Oppgavestatus status) {
        this.id = id;
        this.journalpostId = journalpostId;
        this.behandlesAvApplikasjon = behandlesAvApplikasjon;
        this.saksreferanse = saksreferanse;
        this.aktoerId = aktoerId;
        this.tema = tema;
        this.behandlingstema = behandlingstema;
        this.oppgavetype = oppgavetype;
        this.behandlingstype = behandlingstype;
        this.versjon = versjon;
        this.fristFerdigstillelse = fristFerdigstillelse;
        this.aktivDato = aktivDato;
        this.prioritet = prioritet;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getJournalpostId() {
        return journalpostId;
    }

    public String getBehandlesAvApplikasjon() {
        return behandlesAvApplikasjon;
    }

    public String getSaksreferanse() {
        return saksreferanse;
    }

    public String getAktoerId() {
        return aktoerId;
    }

    public String getTema() {
        return tema;
    }

    public String getBehandlingstema() {
        return behandlingstema;
    }

    public String getOppgavetype() {
        return oppgavetype;
    }

    public String getBehandlingstype() {
        return behandlingstype;
    }

    public Integer getVersjon() {
        return versjon;
    }

    public LocalDate getFristFerdigstillelse() {
        return fristFerdigstillelse;
    }

    public LocalDate getAktivDato() {
        return aktivDato;
    }

    public Prioritet getPrioritet() {
        return prioritet;
    }

    public Oppgavestatus getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "Oppgave{" +
                "id=" + id +
                ", journalpostId='" + journalpostId + '\'' +
                ", behandlesAvApplikasjon='" + behandlesAvApplikasjon + '\'' +
                ", saksreferanse='" + saksreferanse + '\'' +
                ", aktoerId='" + aktoerId + '\'' +
                ", tema='" + tema + '\'' +
                ", behandlingstema='" + behandlingstema + '\'' +
                ", oppgavetype='" + oppgavetype + '\'' +
                ", behandlingstype='" + behandlingstype + '\'' +
                ", versjon=" + versjon +
                ", fristFerdigstillelse=" + fristFerdigstillelse +
                ", aktivDato=" + aktivDato +
                ", prioritet=" + prioritet +
                ", status=" + status +
                '}';
    }
}
