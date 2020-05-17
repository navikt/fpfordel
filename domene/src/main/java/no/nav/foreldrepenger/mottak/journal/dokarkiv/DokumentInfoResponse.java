package no.nav.foreldrepenger.mottak.journal.dokarkiv;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class DokumentInfoResponse {

    @JsonProperty("dokumentInfoId")
    private String dokumentInfoId;

    @JsonCreator
    public DokumentInfoResponse(@JsonProperty("dokumentInfoId") String dokumentInfoId) {
        this.dokumentInfoId = dokumentInfoId;
    }

    public String getDokumentInfoId() {
        return dokumentInfoId;
    }

    @Override
    public String toString() {
        return "DokumentInfo{" +
                "dokumentInfoId='" + dokumentInfoId + '\'' +
                '}';
    }
}
