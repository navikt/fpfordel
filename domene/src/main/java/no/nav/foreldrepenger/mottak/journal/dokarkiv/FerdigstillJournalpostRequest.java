package no.nav.foreldrepenger.mottak.journal.dokarkiv;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class FerdigstillJournalpostRequest {

    @JsonProperty("journalfoerendeEnhet")
    private String journalfoerendeEnhet;

    @JsonCreator
    public FerdigstillJournalpostRequest(@JsonProperty("journalfoerendeEnhet") String journalfoerendeEnhet) {
        this.journalfoerendeEnhet = journalfoerendeEnhet;
    }

    public String getJournalfoerendeEnhet() {
        return journalfoerendeEnhet;
    }

    @Override
    public String toString() {
        return "FerdigstillJournalpostRequest{" +
                ", journalfoerendeEnhet='" + journalfoerendeEnhet + '\'' +
                '}';
    }
}
