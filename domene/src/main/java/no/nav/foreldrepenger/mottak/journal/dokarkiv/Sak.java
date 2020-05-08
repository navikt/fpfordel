package no.nav.foreldrepenger.mottak.journal.dokarkiv;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Sak {

    @JsonProperty("fagsakId")
    private String fagsakId;
    @JsonProperty("fagsaksystem")
    private String fagsaksystem;
    @JsonProperty("sakstype")
    private String sakstype;

    @JsonCreator
    public Sak(@JsonProperty("fagsakId") String fagsakId,
               @JsonProperty("fagsaksystem") String fagsaksystem,
               @JsonProperty("sakstype") String sakstype) {
        this.sakstype = sakstype;
        this.fagsakId = fagsakId;
        this.fagsaksystem = fagsaksystem;
    }

    public String getFagsakId() {
        return fagsakId;
    }

    public String getFagsaksystem() {
        return fagsaksystem;
    }

    public String getSakstype() {
        return sakstype;
    }

    @Override
    public String toString() {
        return "Sak{" +
                "sakstype='" + sakstype + '\'' +
                ", fagsakId='" + fagsakId + '\'' +
                ", fagsaksystem='" + fagsaksystem + '\'' +
                '}';
    }

}
