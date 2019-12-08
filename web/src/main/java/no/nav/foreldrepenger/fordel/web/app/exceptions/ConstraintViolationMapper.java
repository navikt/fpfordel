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

import no.nav.vedtak.feil.Feil;

public class ConstraintViolationMapper implements ExceptionMapper<ConstraintViolationException> {

    private static final Logger log = LoggerFactory.getLogger(ConstraintViolationMapper.class);

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        Collection<FeltFeilDto> feilene = new ArrayList<>();

        for (var cv : exception.getConstraintViolations()) {
            String feltNavn = getFeltNavn(cv.getPropertyPath());
            feilene.add(new FeltFeilDto(feltNavn, cv.getMessage(), null));
        }
        var feltNavn = feilene.stream().map(felt -> felt.getNavn()).collect(Collectors.toList());

        Feil feil = FeltValideringFeil.FACTORY.feltverdiKanIkkeValideres(feltNavn);
        feil.log(log);
        return Response
                .status(Response.Status.BAD_REQUEST)
                .entity(new FeilDto(feil.getFeilmelding(), feilene))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    private String getFeltNavn(Path propertyPath) {
        return propertyPath instanceof PathImpl ? ((PathImpl) propertyPath).getLeafNode().toString() : null;
    }

}
