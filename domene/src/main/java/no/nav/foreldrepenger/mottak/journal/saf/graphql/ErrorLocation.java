package no.nav.foreldrepenger.mottak.journal.saf.graphql;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class ErrorLocation {

    @JsonProperty("line")
    private String line;
    @JsonProperty("column")
    private String column;

    @JsonCreator
    public ErrorLocation(@JsonProperty("line") String line,
            @JsonProperty("column") String column) {
        this.line = line;
        this.column = column;
    }

    public String getLine() {
        return line;
    }

    public String getColumn() {
        return column;
    }
}
