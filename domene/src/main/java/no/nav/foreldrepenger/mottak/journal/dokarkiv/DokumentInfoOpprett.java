package no.nav.foreldrepenger.mottak.journal.dokarkiv;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class DokumentInfoOpprett {

    @JsonProperty("tittel")
    private String tittel;
    @JsonProperty("brevkode")
    private String brevkode;
    @JsonProperty("dokumentvarianter")
    private List<Dokumentvariant> dokumentvarianter;

    @JsonCreator
    public DokumentInfoOpprett(@JsonProperty("tittel") String tittel,
                               @JsonProperty("brevkode") String brevkode,
                               @JsonProperty("dokumentvarianter") List<Dokumentvariant> dokumentvarianter) {
        this.tittel = tittel;
        this.brevkode = brevkode;
        this.dokumentvarianter = dokumentvarianter;
    }

    public String getTittel() {
        return tittel;
    }

    public String getBrevkode() {
        return brevkode;
    }

    public List<Dokumentvariant> getDokumentvarianter() {
        return dokumentvarianter;
    }

    @Override
    public String toString() {
        return "DokumentInfoOpprett{" +
                "tittel='" + tittel + '\'' +
                ", brevkode='" + brevkode + '\'' +
                ", dokumentvarianter=" + dokumentvarianter +
                '}';
    }
}
