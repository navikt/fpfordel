package no.nav.foreldrepenger.fordel.web.server.jetty;

import java.util.Optional;

import javax.annotation.Priority;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Alternative;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.security.token.support.core.jwt.JwtToken;
import no.nav.security.token.support.jaxrs.JaxrsTokenValidationContextHolder;
import no.nav.vedtak.sikkerhet.abac.TokenProvider;
import no.nav.vedtak.sikkerhet.kontekst.IdentType;
import no.nav.vedtak.sikkerhet.kontekst.KontekstHolder;
import no.nav.vedtak.sikkerhet.kontekst.RequestKontekst;
import no.nav.vedtak.sikkerhet.kontekst.WsRequestKontekst;
import no.nav.vedtak.sikkerhet.oidc.config.ConfigProvider;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDConfiguration;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDProvider;
import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;
import no.nav.vedtak.sikkerhet.oidc.token.TokenString;

@Alternative
@Dependent
@Priority(100)
public class TokenSupportTokenProvider implements TokenProvider {

    private static final Logger LOG = LoggerFactory.getLogger(TokenSupportTokenProvider.class);

    @Override
    public String getUid() {
        return firstToken().map(JwtToken::getSubject).orElseGet(() -> KontekstHolder.getKontekst().getUid());
    }

    @Override
    public IdentType getIdentType()  {
        var provider = openIdToken().provider();
        return OpenIDProvider.TOKENX.equals(provider) ? IdentType.EksternBruker : IdentType.InternBruker;
    }

    @Override
    public OpenIDToken openIdToken() {
        return firstToken()
            .map(j -> new OpenIDToken(ConfigProvider.getOpenIDConfiguration(j.getIssuer()).map(OpenIDConfiguration::type).orElse(OpenIDProvider.TOKENX),
                new TokenString(j.getTokenAsString())))
            .orElseGet(() -> KontekstHolder.getKontekst() instanceof RequestKontekst rk ? rk.getToken() : null);
    }

    @Override
    public String samlToken() {
        return KontekstHolder.getKontekst() instanceof WsRequestKontekst wrk ? wrk.getSamlTokenAsString() : null;

    }

    private Optional<JwtToken> firstToken() {
        try {
            return JaxrsTokenValidationContextHolder.getHolder().getTokenValidationContext().getFirstValidToken();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

}
