package no.nav.foreldrepenger.mottak.journal.dokarkiv;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class AvsenderMottaker {

    @JsonProperty("id")
    private String id;
    @JsonProperty("idType")
    private AvsenderMottakerIdType idType;
    @JsonProperty("navn")
    private String navn;


    @JsonCreator
    public AvsenderMottaker(@JsonProperty("id") String id,
                            @JsonProperty("idType") AvsenderMottakerIdType idType,
                            @JsonProperty("navn") String navn) {
        this.id = id;
        this.idType = idType;
        this.navn = navn;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public AvsenderMottakerIdType getIdType() {
        return idType;
    }

    public String getNavn() {
        return navn;
    }

    @Override
    public String toString() {
        return "AvsenderMottaker{" +
                "idType=" + idType +
                '}';
    }
}
