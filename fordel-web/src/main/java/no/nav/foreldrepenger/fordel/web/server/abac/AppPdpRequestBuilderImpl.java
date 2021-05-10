package no.nav.foreldrepenger.fordel.web.server.abac;

import static no.nav.vedtak.sikkerhet.abac.NavAbacCommonAttributter.RESOURCE_FELLES_DOMENE;
import static no.nav.vedtak.sikkerhet.abac.NavAbacCommonAttributter.RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE;
import static no.nav.vedtak.sikkerhet.abac.NavAbacCommonAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.vedtak.sikkerhet.abac.NavAbacCommonAttributter.SUBJECT_LEVEL;
import static no.nav.vedtak.sikkerhet.abac.NavAbacCommonAttributter.SUBJECT_TYPE;
import static no.nav.vedtak.sikkerhet.abac.NavAbacCommonAttributter.XACML10_ACTION_ACTION_ID;
import static no.nav.vedtak.sikkerhet.abac.NavAbacCommonAttributter.XACML10_SUBJECT_ID;

import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Priority;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nimbusds.jwt.SignedJWT;

import no.nav.foreldrepenger.pip.PipRepository;
import no.nav.vedtak.sikkerhet.abac.AbacAttributtSamling;
import no.nav.vedtak.sikkerhet.abac.AbacIdToken.TokenType;
import no.nav.vedtak.sikkerhet.abac.PdpKlient;
import no.nav.vedtak.sikkerhet.abac.PdpRequest;
import no.nav.vedtak.sikkerhet.abac.PdpRequestBuilder;

/**
 * Implementasjon av PDP request for denne applikasjonen.
 */

@Dependent
@Alternative
@Priority(2)
public class AppPdpRequestBuilderImpl implements PdpRequestBuilder {
    public static final String ABAC_DOMAIN = "foreldrepenger";
    private static final Logger LOG = LoggerFactory.getLogger(AppPdpRequestBuilderImpl.class);

    private PipRepository pipRepository;

    @Inject
    public AppPdpRequestBuilderImpl(PipRepository pipRepository) {
        this.pipRepository = pipRepository;
        LOG.info("Konstruert");
    }

    @Override
    public PdpRequest lagPdpRequest(AbacAttributtSamling attributter) {
        PdpRequest pdpRequest = new PdpRequest();
        pdpRequest.put(RESOURCE_FELLES_DOMENE, ABAC_DOMAIN);
        pdpRequest.put(PdpKlient.ENVIRONMENT_AUTH_TOKEN, attributter.getIdToken());
        pdpRequest.put(XACML10_ACTION_ACTION_ID, attributter.getActionType().getEksternKode());
        pdpRequest.put(RESOURCE_FELLES_RESOURCE_TYPE, attributter.getResource());

        // Potensielt noen saksnummer i AbacAttributtSamling - overlater til fpsak å
        // håndheve disse
        Set<String> aktørIder = utledAktørIder(attributter);
        pdpRequest.put(RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE, aktørIder);
        if (attributter.getIdToken().getTokenType().equals(TokenType.TOKENX)) {
            LOG.trace("Legger til ekstra tokenX attributter");
            pdpRequest.put(XACML10_SUBJECT_ID, claim(attributter.getIdToken().getToken(), "sub"));
            pdpRequest.put(SUBJECT_LEVEL, Integer.parseInt(claim(attributter.getIdToken().getToken(), "acr").replace("Level", "")));
            pdpRequest.put(SUBJECT_TYPE, "EksternBruker");
        } else {
            LOG.trace("Token type er {}", attributter.getIdToken().getTokenType());
        }
        LOG.trace("Request er {}", pdpRequest);
        return pdpRequest;
    }

    private Set<String> utledAktørIder(AbacAttributtSamling attributter) {
        Set<String> aktørIder = new HashSet<>(attributter.getVerdier(AppAbacAttributtType.AKTØR_ID));
        aktørIder.addAll(pipRepository
                .hentAktørIdForForsendelser(attributter.getVerdier(AppAbacAttributtType.FORSENDELSE_UUID)));
        return aktørIder;
    }

    private String claim(String token, String claim) {
        try {
            var claims = SignedJWT.parse(token).getJWTClaimsSet();
            return String.class.cast(claims.getClaim(claim));
        } catch (ParseException e) {
            throw new IllegalArgumentException("Fant ikke claim" + claim + " i token", e);
        }
    }
}
