package no.nav.foreldrepenger.mottak.journal.dokarkiv;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.foreldrepenger.mottak.journal.saf.model.AvsenderMottaker;
import no.nav.foreldrepenger.mottak.journal.saf.model.Bruker;
import no.nav.foreldrepenger.mottak.journal.saf.model.Sak;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class OpprettJournalpostRequest {

    @JsonProperty("tittel")
    private String tittel;
    @JsonProperty("journalposttype")
    private String journalposttype;
    @JsonProperty("kanal")
    private String kanal;
    @JsonProperty("tema")
    private String tema;
    @JsonProperty("behandlingstema")
    private String behandlingstema;
    @JsonProperty("journalfoerendeEnhet")
    private String journalfoerendeEnhet;
    @JsonProperty("sak")
    private Sak sak;
    @JsonProperty("bruker")
    private Bruker bruker;
    @JsonProperty("avsenderMottaker")
    private AvsenderMottaker avsenderMottaker;
    @JsonProperty("dokumenter")
    private List<DokumentInfoOpprett> dokumenter;

    @JsonCreator
    public OpprettJournalpostRequest(@JsonProperty("journalposttype") String journalposttype,
                                     @JsonProperty("tittel") String tittel,
                                     @JsonProperty("kanal") String kanal,
                                     @JsonProperty("tema") String tema,
                                     @JsonProperty("behandlingstema") String behandlingstema,
                                     @JsonProperty("journalfoerendeEnhet") String journalfoerendeEnhet,
                                     @JsonProperty("bruker") Bruker bruker,
                                     @JsonProperty("avsenderMottaker") AvsenderMottaker avsenderMottaker,
                                     @JsonProperty("sak") Sak sak,
                                     @JsonProperty("dokumenter") List<DokumentInfoOpprett> dokumenter) {
        this.tittel = tittel;
        this.journalposttype = journalposttype;
        this.kanal = kanal;
        this.tema = tema;
        this.behandlingstema = behandlingstema;
        this.sak = sak;
        this.bruker = bruker;
        this.avsenderMottaker = avsenderMottaker;
        this.journalfoerendeEnhet = journalfoerendeEnhet;
        this.dokumenter = dokumenter;
    }

    public String getTittel() {
        return tittel;
    }

    public String getJournalposttype() {
        return journalposttype;
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

    @Override
    public String toString() {
        return "OpprettJournalpostRequest{" +
                "tittel='" + tittel + '\'' +
                ", journalposttype='" + journalposttype + '\'' +
                ", kanal='" + kanal + '\'' +
                ", tema='" + tema + '\'' +
                ", behandlingstema='" + behandlingstema + '\'' +
                ", journalfoerendeEnhet='" + journalfoerendeEnhet + '\'' +
                ", sak=" + sak +
                ", bruker=" + bruker +
                ", avsenderMottaker=" + avsenderMottaker +
                ", dokumenter=" + dokumenter +
                '}';
    }
}
