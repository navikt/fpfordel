package no.nav.foreldrepenger.fordel.web.app.exceptions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import org.hibernate.validator.internal.engine.path.PathImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConstraintViolationMapper implements ExceptionMapper<ConstraintViolationException> {

    private static final Logger LOG = LoggerFactory.getLogger(ConstraintViolationMapper.class);

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        Collection<FeltFeilDto> feilene = new ArrayList<>();

        for (var cv : exception.getConstraintViolations()) {
            String feltNavn = getFeltNavn(cv.getPropertyPath());
            feilene.add(new FeltFeilDto(feltNavn, cv.getMessage(), null));
        }
        var feltNavn = feilene.stream().map(felt -> felt.getNavn()).collect(Collectors.toList());
        var feil = FeltValideringFeil.feltverdiKanIkkeValideres(feltNavn);
        LOG.warn(feil.getMessage());
        return Response
                .status(Response.Status.BAD_REQUEST)
                .entity(new FeilDto(feil.getMessage(), feilene))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    private static String getFeltNavn(Path propertyPath) {
        return propertyPath instanceof PathImpl ? ((PathImpl) propertyPath).getLeafNode().toString() : null;
    }

}