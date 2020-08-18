package no.nav.foreldrepenger.mottak.journal.saf.graphql;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import no.nav.foreldrepenger.mottak.journal.saf.model.Journalpost;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class GrapQlTilknyttetData {

    @JsonProperty("tilknyttedeJournalposter")
    private List<Journalpost> tilknyttedeJournalposter;

    @JsonCreator
    public GrapQlTilknyttetData(@JsonProperty("tilknyttedeJournalposter") List<Journalpost> tilknyttedeJournalposter) {
        this.tilknyttedeJournalposter = tilknyttedeJournalposter;
    }

    public List<Journalpost> getJournalposter() {
        return tilknyttedeJournalposter;
    }
}
