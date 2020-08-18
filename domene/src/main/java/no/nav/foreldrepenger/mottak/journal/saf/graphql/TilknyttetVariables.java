package no.nav.foreldrepenger.mottak.journal.saf.graphql;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class TilknyttetVariables {

    @JsonProperty("dokumentInfoId")
    private String dokumentInfoId;

    @JsonProperty("tilknytning")
    private Tilknytning tilknytning = Tilknytning.GJENBRUK;

    public TilknyttetVariables(String dokumentInfoId) {
        this.dokumentInfoId = dokumentInfoId;
    }

    public String getDokumentInfoId() {
        return dokumentInfoId;
    }

    public Tilknytning getTilknytning() {
        return tilknytning;
    }
}
