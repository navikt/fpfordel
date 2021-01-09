package no.nav.foreldrepenger.mottak.journal.saf.graphql;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class GraphQlResponse {

    private final GrapQlData data;
    private final List<GraphQlError> errors;

    @JsonCreator
    public GraphQlResponse(@JsonProperty("data") GrapQlData data, @JsonProperty("errors") List<GraphQlError> errors) {
        this.data = data;
        this.errors = errors;
    }

    public GrapQlData getData() {
        return data;
    }

    public List<GraphQlError> getErrors() {
        return errors;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [data=" + data + ", errors=" + errors + "]";
    }
}
