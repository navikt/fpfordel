package no.nav.foreldrepenger.mottak.journal.dokarkiv.model;

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
    @JsonProperty("arkivsaksnummer")
    private String arkivsaksnummer;
    @JsonProperty("arkivsaksystem")
    private String arkivsaksystem;

    @JsonCreator
    public Sak(@JsonProperty("fagsakId") String fagsakId,
            @JsonProperty("fagsaksystem") String fagsaksystem,
            @JsonProperty("sakstype") String sakstype,
            @JsonProperty("arkivsaksnummer") String arkivsaksnummer,
            @JsonProperty("arkivsaksystem") String arkivsaksystem) {
        this.sakstype = sakstype;
        this.fagsakId = fagsakId;
        this.fagsaksystem = fagsaksystem;
        this.arkivsaksnummer = arkivsaksnummer;
        this.arkivsaksystem = arkivsaksystem;
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

    public String getArkivsaksnummer() {
        return arkivsaksnummer;
    }

    public String getArkivsaksystem() {
        return arkivsaksystem;
    }

    @Override
    public String toString() {
        return "Sak{" +
                "fagsakId='" + fagsakId + '\'' +
                ", fagsaksystem='" + fagsaksystem + '\'' +
                ", sakstype='" + sakstype + '\'' +
                ", arkivsaksnummer='" + arkivsaksnummer + '\'' +
                ", arkivsaksystem='" + arkivsaksystem + '\'' +
                '}';
    }

}
