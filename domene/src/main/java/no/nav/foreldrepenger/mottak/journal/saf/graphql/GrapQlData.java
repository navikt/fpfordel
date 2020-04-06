package no.nav.foreldrepenger.mottak.journal.saf.graphql;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.foreldrepenger.mottak.journal.saf.model.Journalpost;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class GrapQlData {

    @JsonProperty("journalpost")
    private Journalpost journalpost;

    @JsonCreator
    public GrapQlData(@JsonProperty("journalpost") Journalpost journalpost) {
        this.journalpost = journalpost;
    }

    public Journalpost getJournalpost() {
        return journalpost;
    }
}
