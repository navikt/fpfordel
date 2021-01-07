package no.nav.foreldrepenger.mottak.journal.saf.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Journalpost {

    private String journalpostId;
    private String tittel;
    private String journalposttype;
    private String journalstatus;
    private String kanal;
    private String tema;
    private String behandlingstema;
    private Sak sak;
    private Bruker bruker;
    private AvsenderMottaker avsenderMottaker;
    private String journalfoerendeEnhet;
    private LocalDateTime datoOpprettet;
    private String eksternReferanseId;
    private List<DokumentInfo> dokumenter;

    @JsonCreator
    public Journalpost(@JsonProperty("journalpostId") String journalpostId,
            @JsonProperty("journalposttype") String journalposttype,
            @JsonProperty("journalstatus") String journalstatus,
            @JsonProperty("datoOpprettet") LocalDateTime datoOpprettet,
            @JsonProperty("tittel") String tittel,
            @JsonProperty("kanal") String kanal,
            @JsonProperty("tema") String tema,
            @JsonProperty("behandlingstema") String behandlingstema,
            @JsonProperty("journalfoerendeEnhet") String journalfoerendeEnhet,
            @JsonProperty("eksternReferanseId") String eksternReferanseId,
            @JsonProperty("bruker") Bruker bruker,
            @JsonProperty("avsenderMottaker") AvsenderMottaker avsenderMottaker,
            @JsonProperty("sak") Sak sak,
            @JsonProperty("dokumenter") List<DokumentInfo> dokumenter) {
        this.journalpostId = journalpostId;
        this.tittel = tittel;
        this.journalposttype = journalposttype;
        this.journalstatus = journalstatus;
        this.kanal = kanal;
        this.tema = tema;
        this.behandlingstema = behandlingstema;
        this.sak = sak;
        this.bruker = bruker;
        this.avsenderMottaker = avsenderMottaker;
        this.journalfoerendeEnhet = journalfoerendeEnhet;
        this.datoOpprettet = datoOpprettet;
        this.eksternReferanseId = eksternReferanseId;
        this.dokumenter = dokumenter;
    }

    public String getJournalpostId() {
        return journalpostId;
    }

    public String getTittel() {
        return tittel;
    }

    public String getJournalposttype() {
        return journalposttype;
    }

    public String getJournalstatus() {
        return journalstatus;
    }

    public String getKanal() {
        return kanal;
    }

    public String getTema() {
        return tema;
    }

    public String getBehandlingstema() {
        return behandlingstema;
    }

    public Sak getSak() {
        return sak;
    }

    public Bruker getBruker() {
        return bruker;
    }

    public AvsenderMottaker getAvsenderMottaker() {
        return avsenderMottaker;
    }

    public String getJournalfoerendeEnhet() {
        return journalfoerendeEnhet;
    }

    public LocalDateTime getDatoOpprettet() {
        return datoOpprettet;
    }

    public String getEksternReferanseId() {
        return eksternReferanseId;
    }

    public List<DokumentInfo> getDokumenter() {
        return dokumenter;
    }

    public boolean harArkivsaksnummer() {
        if (sak == null) {
            return false;
        }
        return (sak.getArkivsaksnummer() != null) && !sak.getArkivsaksnummer().trim().isEmpty();
    }

    @Override
    public int hashCode() {
        return Objects.hash(avsenderMottaker, behandlingstema, bruker, datoOpprettet, dokumenter, eksternReferanseId, journalfoerendeEnhet,
                journalpostId, journalposttype, journalstatus, kanal, sak, tema, tittel);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Journalpost other = (Journalpost) obj;
        if (avsenderMottaker == null) {
            if (other.avsenderMottaker != null)
                return false;
        } else if (!avsenderMottaker.equals(other.avsenderMottaker))
            return false;
        if (behandlingstema == null) {
            if (other.behandlingstema != null)
                return false;
        } else if (!behandlingstema.equals(other.behandlingstema))
            return false;
        if (bruker == null) {
            if (other.bruker != null)
                return false;
        } else if (!bruker.equals(other.bruker))
            return false;
        if (datoOpprettet == null) {
            if (other.datoOpprettet != null)
                return false;
        } else if (!datoOpprettet.equals(other.datoOpprettet))
            return false;
        if (dokumenter == null) {
            if (other.dokumenter != null)
                return false;
        } else if (!dokumenter.equals(other.dokumenter))
            return false;
        if (eksternReferanseId == null) {
            if (other.eksternReferanseId != null)
                return false;
        } else if (!eksternReferanseId.equals(other.eksternReferanseId))
            return false;
        if (journalfoerendeEnhet == null) {
            if (other.journalfoerendeEnhet != null)
                return false;
        } else if (!journalfoerendeEnhet.equals(other.journalfoerendeEnhet))
            return false;
        if (journalpostId == null) {
            if (other.journalpostId != null)
                return false;
        } else if (!journalpostId.equals(other.journalpostId))
            return false;
        if (journalposttype == null) {
            if (other.journalposttype != null)
                return false;
        } else if (!journalposttype.equals(other.journalposttype))
            return false;
        if (journalstatus == null) {
            if (other.journalstatus != null)
                return false;
        } else if (!journalstatus.equals(other.journalstatus))
            return false;
        if (kanal == null) {
            if (other.kanal != null)
                return false;
        } else if (!kanal.equals(other.kanal))
            return false;
        if (sak == null) {
            if (other.sak != null)
                return false;
        } else if (!sak.equals(other.sak))
            return false;
        if (tema == null) {
            if (other.tema != null)
                return false;
        } else if (!tema.equals(other.tema))
            return false;
        if (tittel == null) {
            if (other.tittel != null)
                return false;
        } else if (!tittel.equals(other.tittel))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [journalpostId=" + journalpostId + ", tittel=" + tittel + ", journalposttype=" + journalposttype
                + ", journalstatus=" + journalstatus + ", kanal=" + kanal + ", tema=" + tema + ", behandlingstema=" + behandlingstema + ", sak=" + sak
                + ", bruker=" + bruker + ", avsenderMottaker=" + avsenderMottaker + ", journalfoerendeEnhet=" + journalfoerendeEnhet
                + ", datoOpprettet=" + datoOpprettet + ", eksternReferanseId=" + eksternReferanseId + ", dokumenter=" + dokumenter + "]";
    }
}
