package no.nav.foreldrepenger.mottak.domene.oppgavebehandling.rest;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class FinnOppgaveResponse {

    @JsonProperty("antallTreffTotalt")
    private long antallTreffTotalt;
    @JsonProperty("oppgaver")
    private List<Oppgave> oppgaver;

    @JsonCreator
    public FinnOppgaveResponse(@JsonProperty("antallTreffTotalt") long antallTreffTotalt,
                               @JsonProperty("oppgaver") List<Oppgave> oppgaver) {
        this.antallTreffTotalt = antallTreffTotalt;
        this.oppgaver = oppgaver;
    }

    public long getAntallTreffTotalt() {
        return antallTreffTotalt;
    }

    public List<Oppgave> getOppgaver() {
        return oppgaver;
    }

    @Override
    public String toString() {
        return "FinnOppgaveResponse{" +
                "antallTreffTotalt=" + antallTreffTotalt +
                ", oppgaver=" + oppgaver +
                '}';
    }
}
