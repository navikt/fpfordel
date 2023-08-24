package no.nav.foreldrepenger.fordel.web.app.exceptions;

import java.util.ArrayList;
import java.util.Collection;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;

import org.hibernate.validator.internal.engine.path.PathImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.exception.FunksjonellException;

public class ConstraintViolationMapper implements ExceptionMapper<ConstraintViolationException> {

    private static final Logger LOG = LoggerFactory.getLogger(ConstraintViolationMapper.class);

    private static String getFeltNavn(Path propertyPath) {
        return propertyPath instanceof PathImpl path ? path.getLeafNode().toString() : null;
    }

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        Collection<FeltFeilDto> feilene = new ArrayList<>();

        for (var cv : exception.getConstraintViolations()) {
            String feltNavn = getFeltNavn(cv.getPropertyPath());
            feilene.add(new FeltFeilDto(feltNavn, cv.getMessage(), null));
        }
        var feltNavn = feilene.stream().map(FeltFeilDto::navn).toList();
        var feil = new FunksjonellException("FP-328673", String.format("Det oppstod en valideringsfeil på felt %s", feltNavn),
            "Kontroller at alle feltverdier er korrekte");
        LOG.warn(feil.getMessage());
        return Response.status(Response.Status.BAD_REQUEST).entity(new FeilDto(feil.getMessage(), feilene)).type(MediaType.APPLICATION_JSON).build();
    }

}
