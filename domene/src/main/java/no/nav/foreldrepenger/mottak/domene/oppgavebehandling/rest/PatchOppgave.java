package no.nav.foreldrepenger.mottak.domene.oppgavebehandling.rest;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class PatchOppgave {

    @JsonProperty("id")
    private Long id;
    @JsonProperty("versjon")
    private Integer versjon;
    @JsonProperty("status")
    private Oppgavestatus status;

    @JsonCreator
    public PatchOppgave(@JsonProperty("id") Long id,
                        @JsonProperty("versjon") Integer versjon,
                        @JsonProperty("status") Oppgavestatus status) {
        this.id = id;
        this.versjon = versjon;
        this.status = status;
    }

    @Override
    public String toString() {
        return "PatchOppgave{" +
                "id=" + id +
                ", versjon=" + versjon +
                ", status=" + status +
                '}';
    }
}
