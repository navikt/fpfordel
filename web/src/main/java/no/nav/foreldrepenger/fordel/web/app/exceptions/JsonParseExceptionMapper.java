package no.nav.foreldrepenger.fordel.web.app.exceptions;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public class JsonParseExceptionMapper implements ExceptionMapper<JsonParseException> {

    private static final Logger log = LoggerFactory.getLogger(JsonParseExceptionMapper.class);

    @Override
    public Response toResponse(JsonParseException exception) {
        Feil feil = JsonMappingFeil.FACTORY.jsonParseFeil(exception.getMessage(), exception);
        feil.log(log);
        return Response
            .status(Response.Status.BAD_REQUEST)
            .entity(new FeilDto(feil.getFeilmelding()))
            .type(MediaType.APPLICATION_JSON)
            .build();
    }


    interface JsonMappingFeil extends DeklarerteFeil {

        JsonMappingFeil FACTORY = FeilFactory.create(JsonMappingFeil.class);

        @TekniskFeil(feilkode = "FP-299955", feilmelding = "JSON-parsing feil: %s", logLevel = LogLevel.WARN)
        Feil jsonParseFeil(String feilmelding, JsonParseException e);
    }

}
