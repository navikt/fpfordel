package no.nav.foreldrepenger.fordel.web.app.exceptions;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonMappingException;

import no.nav.vedtak.exception.TekniskException;

public class JsonMappingExceptionMapper implements ExceptionMapper<JsonMappingException> {

    private static final Logger log = LoggerFactory.getLogger(JsonMappingExceptionMapper.class);

    @Override
    public Response toResponse(JsonMappingException exception) {
        var feil = new TekniskException("FP-252294", "JSON-mapping feil", exception);
        log.warn(feil.getMessage());
        return Response
                .status(Response.Status.BAD_REQUEST)
                .entity(new FeilDto(feil.getMessage()))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

}
