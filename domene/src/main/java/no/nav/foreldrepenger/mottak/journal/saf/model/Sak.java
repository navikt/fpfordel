package no.nav.foreldrepenger.mottak.journal.saf.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Sak {

    @JsonProperty("arkivsaksnummer")
    private String arkivsaksnummer;
    @JsonProperty("fagsakId")
    private String fagsakId;
    @JsonProperty("fagsaksystem")
    private String fagsaksystem;

    @JsonCreator
    public Sak(@JsonProperty("arkivsaksnummer") String arkivsaksnummer,
            @JsonProperty("fagsakId") String fagsakId,
            @JsonProperty("fagsaksystem") String fagsaksystem) {
        this.arkivsaksnummer = arkivsaksnummer;
        this.fagsakId = fagsakId;
        this.fagsaksystem = fagsaksystem;
    }

    public String getFagsakId() {
        return fagsakId;
    }

    public String getFagsaksystem() {
        return fagsaksystem;
    }

    public String getArkivsaksnummer() {
        return arkivsaksnummer;
    }

    @Override
    public String toString() {
        return "Sak{" +
                "arkivsaksnummer='" + arkivsaksnummer + '\'' +
                ", fagsakId='" + fagsakId + '\'' +
                ", fagsaksystem='" + fagsaksystem + '\'' +
                '}';
    }

}
