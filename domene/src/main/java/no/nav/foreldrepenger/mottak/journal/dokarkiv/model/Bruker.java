package no.nav.foreldrepenger.mottak.journal.dokarkiv.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.foreldrepenger.mottak.journal.saf.model.BrukerIdType;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Bruker {

    @JsonProperty("id")
    private String id;
    @JsonProperty("idType")
    private BrukerIdType idType;

    @JsonCreator
    public Bruker(@JsonProperty("id") String id, @JsonProperty("idType") BrukerIdType idType) {
        this.id = id;
        this.idType = idType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public BrukerIdType getIdType() {
        return idType;
    }

    @Override
    public String toString() {
        return "Bruker{" +
                "id='" + id + '\'' +
                ", idType=" + idType +
                '}';
    }
}
