package no.nav.foreldrepenger.fordel.web.app.exceptions;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonMappingException;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public class JsonMappingExceptionMapper implements ExceptionMapper<JsonMappingException> {

    private static final Logger log = LoggerFactory.getLogger(JsonMappingExceptionMapper.class);

    @Override
    public Response toResponse(JsonMappingException exception) {
        Feil feil = JsonMappingFeil.FACTORY.jsonMappingFeil(exception);
        feil.log(log);
        return Response
                .status(Response.Status.BAD_REQUEST)
                .entity(new FeilDto(feil.getFeilmelding()))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    interface JsonMappingFeil extends DeklarerteFeil {

        JsonMappingFeil FACTORY = FeilFactory.create(JsonMappingFeil.class);

        @TekniskFeil(feilkode = "FP-252294", feilmelding = "JSON-mapping feil", logLevel = LogLevel.WARN)
        Feil jsonMappingFeil(JsonMappingException cause);
    }

}
