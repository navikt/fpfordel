package no.nav.foreldrepenger.fordel.web.app.exceptions;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import no.nav.vedtak.exception.FunksjonellException;
import no.nav.vedtak.exception.ManglerTilgangException;
import no.nav.vedtak.log.mdc.MDCOperations;
import no.nav.vedtak.log.util.LoggerUtils;


@Provider
public class GeneralRestExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeneralRestExceptionMapper.class);

    private static Response serverError(Throwable feil) {
        String feilmelding = getVLExceptionFeilmelding(feil);
        return Response.serverError().entity(new FeilDto(feilmelding, FeilType.GENERELL_FEIL)).type(MediaType.APPLICATION_JSON).build();
    }

    private static Response ikkeTilgang(ManglerTilgangException feil) {
        return Response.status(Response.Status.FORBIDDEN)
            .entity(new FeilDto(feil.getMessage(), FeilType.MANGLER_TILGANG_FEIL))
            .type(MediaType.APPLICATION_JSON)
            .build();
    }

    private static String getVLExceptionFeilmelding(Throwable feil) {
        var callId = MDCOperations.getCallId();
        String feilbeskrivelse = getExceptionMelding(feil);
        if (feil instanceof FunksjonellException f) {
            String løsningsforslag = f.getLøsningsforslag();
            return "Det oppstod en feil: " + avsluttMedPunktum(feilbeskrivelse) + avsluttMedPunktum(løsningsforslag) + ". Referanse-id: " + callId;
        }
        return "Det oppstod en serverfeil: " + avsluttMedPunktum(feilbeskrivelse) + ". Meld til support med referanse-id: " + callId;

    }

    private static String avsluttMedPunktum(String tekst) {
        return tekst + (tekst.endsWith(".") ? " " : ". ");
    }

    private static void loggTilApplikasjonslogg(Throwable cause) {
        var feil = getExceptionMelding(cause);
        if (cause instanceof ManglerTilgangException) {
            LOGGER.info(feil, cause);
        } else {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(String.format("Fikk uventet feil: %s", feil), cause);
            }
        }

        // key for å tracke prosess -- nullstill denne
        MDC.remove("prosess");
    }

    private static String getExceptionMelding(Throwable feil) {
        return getTextForField(feil.getMessage());
    }

    private static String getTextForField(String input) {
        return input != null ? LoggerUtils.removeLineBreaks(input) : "";
    }

    @Override
    public Response toResponse(Throwable cause) {
        loggTilApplikasjonslogg(cause);
        if (cause instanceof WebApplicationException wae && wae.getResponse() != null) {
            return wae.getResponse();
        }
        // TODO re-enable og slett den over etter validering loggTilApplikasjonslogg(cause);
        if (cause instanceof ManglerTilgangException mte) {
            return ikkeTilgang(mte);
        } else {
            return serverError(cause);
        }
    }

}
