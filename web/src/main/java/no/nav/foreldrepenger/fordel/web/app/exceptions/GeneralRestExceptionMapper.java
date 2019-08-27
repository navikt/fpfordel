package no.nav.foreldrepenger.fordel.web.app.exceptions;

import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.spi.ApplicationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import no.nav.vedtak.exception.ManglerTilgangException;
import no.nav.vedtak.exception.VLException;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FunksjonellFeil;
import no.nav.vedtak.felles.jpa.TomtResultatException;
import no.nav.vedtak.log.mdc.MDCOperations;
import no.nav.vedtak.log.util.LoggerUtils;

// TODO (tor) Har berre fått denne til å fungera med ApplicationException. Dermed blir denne mapperen heilt
// generell. (Eigen mapper for ConstraintViolationException.)

@Provider
public class GeneralRestExceptionMapper implements ExceptionMapper<ApplicationException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeneralRestExceptionMapper.class);

    @Override
    public Response toResponse(ApplicationException exception) {
        Throwable cause = exception.getCause();

        if (cause instanceof Valideringsfeil) {
            return handleValideringsfeil((Valideringsfeil) cause);
        } else if (cause instanceof TomtResultatException) {
            return handleTomtResultatFeil((TomtResultatException) cause);
        }

        loggTilApplikasjonslogg(cause);
        String callId = MDCOperations.getCallId();

        if (cause instanceof VLException) {
            return handleVLException((VLException) cause, callId);
        }

        return handleGenerellFeil(cause, callId);
    }

    private Response handleTomtResultatFeil(TomtResultatException tomtResultatException) {
        return Response
            .status(Response.Status.NOT_FOUND)
            .entity(new FeilDto(FeilType.TOMT_RESULTAT_FEIL, tomtResultatException.getMessage()))
            .type(MediaType.APPLICATION_JSON)
            .build();
    }

    private Response handleValideringsfeil(Valideringsfeil valideringsfeil) {
        List<String> feltNavn = valideringsfeil.getFeltFeil().stream().map(felt -> felt.getNavn()).collect(Collectors.toList());
        return Response
            .status(Response.Status.BAD_REQUEST)
            .entity(new FeilDto(
                FeltValideringFeil.FACTORY.feltverdiKanIkkeValideres(feltNavn).getFeilmelding(),
                valideringsfeil.getFeltFeil()))
            .type(MediaType.APPLICATION_JSON)
            .build();
    }

    private Response handleVLException(VLException vlException, String callId) {
        Feil feil = vlException.getFeil();
        if (vlException instanceof ManglerTilgangException) {
            return ikkeTilgang(feil);
        } else {
            return serverError(callId, feil);
        }
    }

    private Response serverError(String callId, Feil feil) {
        String feilmelding = getVLExceptionFeilmelding(callId, feil);
        FeilType feilType = FeilType.GENERELL_FEIL;
        return Response.serverError()
            .entity(new FeilDto(feilType, feilmelding))
            .type(MediaType.APPLICATION_JSON)
            .build();
    }

    private Response ikkeTilgang(Feil feil) {
        String feilmelding = feil.getFeilmelding();
        FeilType feilType = FeilType.MANGLER_TILGANG_FEIL;
        return Response.status(Response.Status.FORBIDDEN)
            .entity(new FeilDto(feilType, feilmelding))
            .type(MediaType.APPLICATION_JSON)
            .build();
    }

    private String getVLExceptionFeilmelding(String callId, Feil feil) {
        String feilbeskrivelse = feil.getKode() + ": " + feil.getFeilmelding(); //$NON-NLS-1$
        if (feil instanceof FunksjonellFeil) {
            String løsningsforslag = ((FunksjonellFeil) feil).getLøsningsforslag();
            return "Det oppstod en feil: " //$NON-NLS-1$
                + avsluttMedPunktum(feilbeskrivelse)
                + avsluttMedPunktum(løsningsforslag)
                + ". Referanse-id: " + callId; //$NON-NLS-1$
        } else {
            return "Det oppstod en serverfeil: " //$NON-NLS-1$
                + avsluttMedPunktum(feilbeskrivelse)
                + ". Meld til support med referanse-id: " + callId; //$NON-NLS-1$
        }
    }

    private Response handleGenerellFeil(Throwable cause, String callId) {
        String generellFeilmelding = "Det oppstod en serverfeil: " + cause.getMessage() + ". Meld til support med referanse-id: " + callId; //$NON-NLS-1$ //$NON-NLS-2$
        return Response.serverError()
            .entity(new FeilDto(FeilType.GENERELL_FEIL, generellFeilmelding))
            .type(MediaType.APPLICATION_JSON)
            .build();
    }

    private String avsluttMedPunktum(String tekst) {
        return tekst + (tekst.endsWith(".") ? " " : ". "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    private void loggTilApplikasjonslogg(Throwable cause) {
        if (cause instanceof VLException) {
            ((VLException) cause).log(LOGGER);
        } else {
            String message = cause.getMessage() != null ? LoggerUtils.removeLineBreaks(cause.getMessage()) : "";
            LOGGER.error("Fikk uventet feil:" + message, cause); //NOSONAR //$NON-NLS-1$
        }

        // key for å tracke prosess -- nullstill denne
        MDC.remove("prosess");  //$NON-NLS-1$
    }

}
