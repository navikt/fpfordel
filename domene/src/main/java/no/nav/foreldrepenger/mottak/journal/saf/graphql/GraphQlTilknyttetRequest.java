package no.nav.foreldrepenger.mottak.journal.saf.graphql;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE, setterVisibility = JsonAutoDetect.Visibility.NONE, fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class GraphQlTilknyttetRequest {

    @JsonProperty("query")
    private String query;
    @JsonProperty("variables")
    private TilknyttetVariables variables;

    @JsonCreator
    public GraphQlTilknyttetRequest(@JsonProperty("query") String query, @JsonProperty("variables") TilknyttetVariables variables) {
        this.query = query;
        this.variables = variables;
    }

    public String getQuery() {
        return query;
    }

    public TilknyttetVariables getVariables() {
        return variables;
    }

}
