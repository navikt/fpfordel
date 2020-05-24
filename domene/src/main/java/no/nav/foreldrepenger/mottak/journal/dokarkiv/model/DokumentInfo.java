package no.nav.foreldrepenger.mottak.journal.dokarkiv.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.foreldrepenger.mottak.journal.saf.model.Dokumentvariant;
import no.nav.foreldrepenger.mottak.journal.saf.model.LogiskVedlegg;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class DokumentInfo {

    @JsonProperty("dokumentInfoId")
    private String dokumentInfoId;
    @JsonProperty("tittel")
    private String tittel;
    @JsonProperty("brevkode")
    private String brevkode;
    @JsonProperty("logiskeVedlegg")
    private List<LogiskVedlegg> logiskeVedlegg;
    @JsonProperty("dokumentvarianter")
    private List<Dokumentvariant> dokumentvarianter;

    @JsonCreator
    public DokumentInfo(@JsonProperty("dokumentInfoId") String dokumentInfoId,
                        @JsonProperty("tittel") String tittel,
                        @JsonProperty("brevkode") String brevkode,
                        @JsonProperty("logiskeVedlegg") List<LogiskVedlegg> logiskeVedlegg,
                        @JsonProperty("dokumentvarianter") List<Dokumentvariant> dokumentvarianter) {
        this.dokumentInfoId = dokumentInfoId;
        this.tittel = tittel;
        this.brevkode = brevkode;
        this.logiskeVedlegg = logiskeVedlegg;
        this.dokumentvarianter = dokumentvarianter;
    }

    public String getDokumentInfoId() {
        return dokumentInfoId;
    }

    public String getTittel() {
        return tittel;
    }

    public String getBrevkode() {
        return brevkode;
    }

    public List<LogiskVedlegg> getLogiskeVedlegg() {
        return logiskeVedlegg;
    }

    public List<Dokumentvariant> getDokumentvarianter() {
        return dokumentvarianter;
    }

    @Override
    public String toString() {
        return "DokumentInfo{" +
                "dokumentInfoId='" + dokumentInfoId + '\'' +
                ", tittel='" + tittel + '\'' +
                ", brevkode='" + brevkode + '\'' +
                ", logiskeVedlegg=" + logiskeVedlegg +
                ", dokumentvarianter=" + dokumentvarianter +
                '}';
    }
}
