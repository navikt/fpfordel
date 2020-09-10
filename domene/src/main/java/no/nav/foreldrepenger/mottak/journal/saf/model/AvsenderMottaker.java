package no.nav.foreldrepenger.mottak.journal.saf.model;

import java.util.Optional;

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
    @JsonProperty("type")
    private String type;
    @JsonProperty("navn")
    private String navn;

    @JsonCreator
    public AvsenderMottaker(@JsonProperty("id") String id,
            @JsonProperty("type") String type,
            @JsonProperty("navn") String navn) {
        this.id = id;
        this.type = type;
        this.navn = navn;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getNavn() {
        return navn;
    }

    public Optional<String> getIdHvisFNR() {
        return "FNR".equalsIgnoreCase(type) ? Optional.ofNullable(id) : Optional.empty();
    }

    @Override
    public String toString() {
        return "AvsenderMottaker{" +
                "id='" + id + '\'' +
                ", idType=" + type +
                ", navn='" + navn + '\'' +
                '}';
    }
}
