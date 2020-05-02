package no.nav.foreldrepenger.mottak.gsak;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class SakResponse {

    private Long id;
    private String tema;
    private String applikasjon;
    private LocalDate opprettetTidspunkt;

    @JsonCreator
    public SakResponse(@JsonProperty("id") Long id,
                       @JsonProperty("tema") String tema,
                       @JsonProperty("applikasjon") String applikasjon,
                       @JsonProperty("opprettetTidspunkt") LocalDate opprettetTidspunkt) {
        this.id = id;
        this.tema = tema;
        this.applikasjon = applikasjon;
        this.opprettetTidspunkt = opprettetTidspunkt;
    }

    public Long getId() {
        return id;
    }

    public String getTema() {
        return tema;
    }

    public String getApplikasjon() {
        return applikasjon;
    }

    public LocalDate getOpprettetTidspunkt() {
        return opprettetTidspunkt;
    }

    @Override
    public String toString() {
        return "SakResponse{" +
                "id=" + id +
                ", tema='" + tema + '\'' +
                ", applikasjon='" + applikasjon + '\'' +
                ", opprettetTidspunkt=" + opprettetTidspunkt +
                '}';
    }
}
