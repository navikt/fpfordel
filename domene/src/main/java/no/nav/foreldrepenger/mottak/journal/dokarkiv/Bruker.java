package no.nav.foreldrepenger.mottak.journal.dokarkiv;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Bruker {

    @JsonProperty("id")
    private String id;
    @JsonProperty("type")
    private BrukerIdType type;

    @JsonCreator
    public Bruker(@JsonProperty("id") String id, @JsonProperty("type") BrukerIdType type) {
        this.id = id;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public BrukerIdType getType() {
        return type;
    }

    public boolean erAktoerId() {
        return BrukerIdType.AKTOERID.equals(type);
    }
}
