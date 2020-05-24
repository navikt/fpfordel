package no.nav.foreldrepenger.mottak.journal.dokarkiv.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class DokumentInfoOppdater {

    @JsonProperty("dokumentInfoId")
    private String dokumentInfoId;
    @JsonProperty("tittel")
    private String tittel;
    @JsonProperty("brevkode")
    private String brevkode;

    @JsonCreator
    public DokumentInfoOppdater(@JsonProperty("dokumentInfoId") String dokumentInfoId,
                                @JsonProperty("tittel") String tittel,
                                @JsonProperty("brevkode") String brevkode) {
        this.dokumentInfoId = dokumentInfoId;
        this.tittel = tittel;
        this.brevkode = brevkode;
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


    @Override
    public String toString() {
        return "DokumentInfo{" +
                "dokumentInfoId='" + dokumentInfoId + '\'' +
                ", tittel='" + tittel + '\'' +
                ", brevkode='" + brevkode + '\'' +
                '}';
    }
}
