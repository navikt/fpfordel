package no.nav.foreldrepenger.mottak.journal.saf.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class LogiskVedlegg {

    @JsonProperty("tittel")
    private String tittel;

    @JsonCreator
    public LogiskVedlegg(@JsonProperty("filnavn") String tittel) {
        this.tittel = tittel;
    }

    public String getTittel() {
        return tittel;
    }

    @Override
    public String toString() {
        return "LogiskVedlegg{" +
                "tittel='" + tittel + '\'' +
                '}';
    }
}
