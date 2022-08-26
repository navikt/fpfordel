package no.nav.foreldrepenger.fordel.web.server.abac;

import static no.nav.vedtak.sikkerhet.abac.NavAbacCommonAttributter.ENVIRONMENT_FELLES_OIDC_TOKEN_BODY;
import static no.nav.vedtak.sikkerhet.abac.NavAbacCommonAttributter.ENVIRONMENT_FELLES_PEP_ID;
import static no.nav.vedtak.sikkerhet.abac.NavAbacCommonAttributter.ENVIRONMENT_FELLES_SAML_TOKEN;
import static no.nav.vedtak.sikkerhet.abac.NavAbacCommonAttributter.ENVIRONMENT_FELLES_TOKENX_TOKEN_BODY;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Base64;
import java.util.List;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.interceptor.Interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nimbusds.jwt.SignedJWT;

import no.nav.foreldrepenger.konfig.Environment;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.log.util.LoggerUtils;
import no.nav.vedtak.sikkerhet.abac.AbacIdToken;
import no.nav.vedtak.sikkerhet.abac.AbacResultat;
import no.nav.vedtak.sikkerhet.abac.Decision;
import no.nav.vedtak.sikkerhet.abac.PdpKlient;
import no.nav.vedtak.sikkerhet.abac.PdpRequest;
import no.nav.vedtak.sikkerhet.abac.Tilgangsbeslutning;
import no.nav.vedtak.sikkerhet.pdp.PdpConsumer;
import no.nav.vedtak.sikkerhet.pdp.XacmlRequestBuilderTjeneste;
import no.nav.vedtak.sikkerhet.pdp.xacml.Advice;
import no.nav.vedtak.sikkerhet.pdp.xacml.XacmlAttributeSet;
import no.nav.vedtak.sikkerhet.pdp.xacml.XacmlRequestBuilder;
import no.nav.vedtak.sikkerhet.pdp.xacml.XacmlResponse;
import no.nav.vedtak.sikkerhet.pdp.xacml.XacmlResponseMapper;

@Alternative
@Priority(Interceptor.Priority.APPLICATION + 1)
@ApplicationScoped
public class LokalPdpKlientImpl implements PdpKlient {

    private static final Environment ENV = Environment.current();
    private static final Logger LOG = LoggerFactory.getLogger(LokalPdpKlientImpl.class);
    private XacmlRequestBuilderTjeneste xamlRequestBuilderTjeneste;

    private PdpConsumer pdp;

    public LokalPdpKlientImpl() {
    }

    @Inject
    public LokalPdpKlientImpl(PdpConsumer pdp, XacmlRequestBuilderTjeneste xamlRequestBuilderTjeneste) {
        this.pdp = pdp;
        this.xamlRequestBuilderTjeneste = xamlRequestBuilderTjeneste;
    }

    @Override
    public Tilgangsbeslutning forespørTilgang(PdpRequest req) {
        var builder = xamlRequestBuilderTjeneste.lagXacmlRequestBuilder(req);
        leggPåTokenInformasjon(builder, req);
        var response = pdp.evaluate(builder);
        var hovedresultat = resultatFraResponse(response);
        return new Tilgangsbeslutning(hovedresultat, XacmlResponseMapper.getDecisions(response), req);
    }

    static void leggPåTokenInformasjon(XacmlRequestBuilder builder, PdpRequest req) {
        var attrs = new XacmlAttributeSet();
        attrs.addAttribute(ENVIRONMENT_FELLES_PEP_ID, getPepId());
        var idToken = AbacIdToken.class.cast(req.get(ENVIRONMENT_AUTH_TOKEN));
        switch (idToken.getTokenType()) {
            case OIDC:
                String key = ENVIRONMENT_FELLES_OIDC_TOKEN_BODY;
                LOG.trace("Legger ved token med type oidc på {}", key);
                try {
                    attrs.addAttribute(key, SignedJWT.parse(idToken.getToken()).getPayload().toBase64URL().toString());
                } catch (ParseException e) {
                    throw new IllegalArgumentException("Ukjent token type");
                }
                break;
            case TOKENX:
                String keyX = ENVIRONMENT_FELLES_TOKENX_TOKEN_BODY;
                LOG.trace("Legger ved token med type tokenX på {}", keyX);
                try {
                    attrs.addAttribute(keyX, SignedJWT.parse(idToken.getToken()).getPayload().toBase64URL().toString());
                } catch (ParseException e) {
                    throw new IllegalArgumentException("Ukjent token type");
                }
                break;
            case SAML:
                LOG.trace("Legger på token med type saml");
                attrs.addAttribute(ENVIRONMENT_FELLES_SAML_TOKEN, base64encode(idToken.getToken()));
                break;
        }

        builder.addEnvironmentAttributeSet(attrs);
    }

    private static String base64encode(String samlToken) {
        return Base64.getEncoder().encodeToString(samlToken.getBytes(StandardCharsets.UTF_8));
    }

    private static AbacResultat resultatFraResponse(XacmlResponse response) {
        var decisions = XacmlResponseMapper.getDecisions(response);

        for (var decision : decisions) {
            if (decision == Decision.Indeterminate) {
                throw new TekniskException("F-080281",
                    String.format("Decision %s fra PDP, dette skal aldri skje. Full JSON response: %s", decision, response));
            }
        }

        var biasedDecision = createAggregatedDecision(decisions);
        handlObligation(response);

        if (biasedDecision == Decision.Permit) {
            return AbacResultat.GODKJENT;
        }

        var denyAdvice = XacmlResponseMapper.getAdvice(response);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Deny fra PDP, advice var: {}", LoggerUtils.toStringWithoutLineBreaks(denyAdvice));
        }
        if (denyAdvice.contains(Advice.DENY_KODE_6)) {
            return AbacResultat.AVSLÅTT_KODE_6;
        }
        if (denyAdvice.contains(Advice.DENY_KODE_7)) {
            return AbacResultat.AVSLÅTT_KODE_7;
        }
        if (denyAdvice.contains(Advice.DENY_EGEN_ANSATT)) {
            return AbacResultat.AVSLÅTT_EGEN_ANSATT;
        }
        var ukjentadvice = denyAdvice.toString();
        // TODO (JOL): fjerne etter midlertidig logging ifm overgang
        LOG.info("Deny fra PDP uten kjent årsak, advice var: {}", ukjentadvice);
        return AbacResultat.AVSLÅTT_ANNEN_ÅRSAK;
    }

    private static Decision createAggregatedDecision(List<Decision> decisions) {
        for (var decision : decisions) {
            if (decision != Decision.Permit)
                return Decision.Deny;
        }
        return Decision.Permit;
    }

    private static void handlObligation(XacmlResponse response) {
        var obligations = XacmlResponseMapper.getObligations(response);
        if (!obligations.isEmpty()) {
            throw new TekniskException("F-576027", String.format("Mottok ukjente obligations fra PDP: %s", obligations));
        }
    }

    private static String getPepId() {
        return ENV.getNaisAppName();
    }
}
