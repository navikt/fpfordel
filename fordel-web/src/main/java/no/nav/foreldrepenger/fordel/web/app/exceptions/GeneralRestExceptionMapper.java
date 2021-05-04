package no.nav.foreldrepenger.fordel.web.app.exceptions;

import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import no.nav.vedtak.exception.FunksjonellException;
import no.nav.vedtak.exception.ManglerTilgangException;
import no.nav.vedtak.exception.VLException;
import no.nav.vedtak.log.mdc.MDCOperations;
import no.nav.vedtak.log.util.LoggerUtils;

// TODO (tor) Har berre fått denne til å fungera med ApplicationException. Dermed blir denne mapperen heilt
// generell. (Eigen mapper for ConstraintViolationException.)

@Provider
public class GeneralRestExceptionMapper implements ExceptionMapper<WebApplicationException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeneralRestExceptionMapper.class);

    @Override
    public Response toResponse(WebApplicationException e) {
        Throwable cause = e.getCause();

        if (cause instanceof ValideringException) {
            return handleValideringsfeil((ValideringException) cause);
        }

        loggTilApplikasjonslogg(cause);
        String callId = MDCOperations.getCallId();

        if (cause instanceof VLException vl) {
            return handleVLException(vl, callId);
        }
        return handleGenerellFeil(cause, callId);
    }

    private static Response handleValideringsfeil(ValideringException valideringsfeil) {
        List<String> feltNavn = valideringsfeil.getFeltFeil().stream().map(felt -> felt.navn())
                .collect(Collectors.toList());
        return Response
                .status(Status.BAD_REQUEST)
                .entity(new FeilDto(
                        new FunksjonellException("FP-328673", String.format("Det oppstod en valideringsfeil på felt %s", feltNavn),
                                "Kontroller at alle feltverdier er korrekte").getMessage(),
                        valideringsfeil.getFeltFeil()))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    private static Response handleVLException(VLException e, String callId) {
        if (e instanceof ManglerTilgangException) {
            return ikkeTilgang((ManglerTilgangException) e);
        }
        return serverError(callId, e);
    }

    private static Response serverError(String callId, VLException feil) {
        String feilmelding = getVLExceptionFeilmelding(callId, feil);
        return Response.serverError()
                .entity(new FeilDto(feilmelding, FeilType.GENERELL_FEIL))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    private static Response ikkeTilgang(ManglerTilgangException feil) {
        String feilmelding = feil.getMessage();
        FeilType feilType = FeilType.MANGLER_TILGANG_FEIL;
        return Response.status(Response.Status.FORBIDDEN)
                .entity(new FeilDto(feilmelding, feilType))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    private static String getVLExceptionFeilmelding(String callId, VLException feil) {
        String feilbeskrivelse = feil.getMessage();
        if (feil instanceof FunksjonellException f) {
            String løsningsforslag = f.getLøsningsforslag();
            return "Det oppstod en feil: " //$NON-NLS-1$
                    + avsluttMedPunktum(feilbeskrivelse)
                    + avsluttMedPunktum(løsningsforslag)
                    + ". Referanse-id: " + callId;
        } else {
            return "Det oppstod en serverfeil: "
                    + avsluttMedPunktum(feilbeskrivelse)
                    + ". Meld til support med referanse-id: " + callId;
        }
    }

    private static Response handleGenerellFeil(Throwable cause, String callId) {
        String generellFeilmelding = "Det oppstod en serverfeil: " + cause.getMessage()
                + ". Meld til support med referanse-id: " + callId;
        return Response.serverError()
                .entity(new FeilDto(generellFeilmelding, FeilType.GENERELL_FEIL))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    private static String avsluttMedPunktum(String tekst) {
        return tekst + (tekst.endsWith(".") ? " " : ". ");
    }

    private static void loggTilApplikasjonslogg(Throwable cause) {
        if (cause instanceof VLException) {
            LOGGER.warn(cause.getMessage());
        } else {
            String message = cause.getMessage() != null ? LoggerUtils.removeLineBreaks(cause.getMessage()) : "";
            LOGGER.error("Fikk uventet feil:" + message, cause);
        }

        // key for å tracke prosess -- nullstill denne
        MDC.remove("prosess");
    }

}
