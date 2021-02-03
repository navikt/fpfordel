package no.nav.foreldrepenger.mottak.journal.dokarkiv.model;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class OpprettJournalpostRequest {

    private String tittel;
    private JournalpostType journalpostType;
    private String kanal;
    private String tema;
    private String behandlingstema;
    private String journalfoerendeEnhet;
    private LocalDate datoMottatt;
    private String eksternReferanseId;
    private Sak sak;
    private Bruker bruker;
    private AvsenderMottaker avsenderMottaker;
    private List<DokumentInfoOpprett> dokumenter;

    @JsonCreator
    public OpprettJournalpostRequest(@JsonProperty("journalposttype") JournalpostType journalpostType,
            @JsonProperty("tittel") String tittel,
            @JsonProperty("kanal") String kanal,
            @JsonProperty("tema") String tema,
            @JsonProperty("behandlingstema") String behandlingstema,
            @JsonProperty("journalfoerendeEnhet") String journalfoerendeEnhet,
            @JsonProperty("datoMottatt") LocalDate datoMottatt,
            @JsonProperty("eksternReferanseId") String eksternReferanseId,
            @JsonProperty("bruker") Bruker bruker,
            @JsonProperty("avsenderMottaker") AvsenderMottaker avsenderMottaker,
            @JsonProperty("sak") Sak sak,
            @JsonProperty("dokumenter") List<DokumentInfoOpprett> dokumenter) {
        this.tittel = tittel;
        this.journalpostType = journalpostType;
        this.kanal = kanal;
        this.tema = tema;
        this.behandlingstema = behandlingstema;
        this.sak = sak;
        this.bruker = bruker;
        this.avsenderMottaker = avsenderMottaker;
        this.journalfoerendeEnhet = journalfoerendeEnhet;
        this.eksternReferanseId = eksternReferanseId;
        this.datoMottatt = datoMottatt;
        this.dokumenter = dokumenter;
    }

    private OpprettJournalpostRequest() {

    }

    public void setTittel(String tittel) {
        this.tittel = tittel;
    }

    public void setJournalposttype(JournalpostType journalpostType) {
        this.journalpostType = journalpostType;
    }

    public void setKanal(String kanal) {
        this.kanal = kanal;
    }

    public void setTema(String tema) {
        this.tema = tema;
    }

    public void setBehandlingstema(String behandlingstema) {
        this.behandlingstema = behandlingstema;
    }

    public void setJournalfoerendeEnhet(String journalfoerendeEnhet) {
        this.journalfoerendeEnhet = journalfoerendeEnhet;
    }

    public void setDatoMottatt(LocalDate datoMottatt) {
        this.datoMottatt = datoMottatt;
    }

    public void setEksternReferanseId(String eksternReferanseId) {
        this.eksternReferanseId = eksternReferanseId;
    }

    public void setSak(Sak sak) {
        this.sak = sak;
    }

    public void setBruker(Bruker bruker) {
        this.bruker = bruker;
    }

    public void setAvsenderMottaker(AvsenderMottaker avsenderMottaker) {
        this.avsenderMottaker = avsenderMottaker;
    }

    public void setDokumenter(List<DokumentInfoOpprett> dokumenter) {
        this.dokumenter = dokumenter;
    }

    public String getTittel() {
        return tittel;
    }

    public JournalpostType getJournalpostType() {
        return journalpostType;
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

    public String getJournalfoerendeEnhet() {
        return journalfoerendeEnhet;
    }

    public LocalDate getDatoMottatt() {
        return datoMottatt;
    }

    public String getEksternReferanseId() {
        return eksternReferanseId;
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

    public List<DokumentInfoOpprett> getDokumenter() {
        return dokumenter;
    }

    public static OpprettJournalpostRequest nyInngående() {
        var response = new OpprettJournalpostRequest();
        response.setJournalposttype(JournalpostType.INNGAAENDE);
        return response;
    }

    @Override
    public String toString() {
        return "OpprettJournalpostRequest{" +
                "tittel='" + tittel + '\'' +
                ", journalpostType='" + journalpostType + '\'' +
                ", kanal='" + kanal + '\'' +
                ", tema='" + tema + '\'' +
                ", behandlingstema='" + behandlingstema + '\'' +
                ", journalfoerendeEnhet='" + journalfoerendeEnhet + '\'' +
                ", datoMottatt=" + datoMottatt +
                ", eksternReferanseId='" + eksternReferanseId + '\'' +
                ", sak=" + sak +
                ", bruker=" + bruker +
                ", avsenderMottaker=" + avsenderMottaker +
                ", dokumenter=" + dokumenter +
                '}';
    }
}
