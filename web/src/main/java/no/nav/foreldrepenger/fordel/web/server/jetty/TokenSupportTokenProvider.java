package no.nav.foreldrepenger.fordel.web.server.jetty;

import no.nav.security.token.support.core.jwt.JwtToken;
import no.nav.security.token.support.jaxrs.JaxrsTokenValidationContextHolder;
import no.nav.vedtak.sikkerhet.abac.TokenProvider;
import no.nav.vedtak.sikkerhet.context.SubjectHandler;
import no.nav.vedtak.sikkerhet.context.containers.SluttBruker;
import no.nav.vedtak.sikkerhet.kontekst.IdentType;
import no.nav.vedtak.sikkerhet.oidc.config.ConfigProvider;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDConfiguration;
import no.nav.vedtak.sikkerhet.oidc.config.OpenIDProvider;
import no.nav.vedtak.sikkerhet.oidc.token.OpenIDToken;
import no.nav.vedtak.sikkerhet.oidc.token.TokenString;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Alternative;

import java.util.Optional;

@Alternative
@Dependent
@Priority(100)
public class TokenSupportTokenProvider implements TokenProvider {

    private static final Logger LOG = LoggerFactory.getLogger(TokenSupportTokenProvider.class);

    @Override
    public String getUid() {
        return firstToken().map(JwtToken::getSubject).orElseGet(() -> SubjectHandler.getSubjectHandler().getUid());
    }

    @Override
    public SluttBruker getSluttBruker() {
        var provider = openIdToken().provider();
        return new SluttBruker(getUid(), OpenIDProvider.TOKENX.equals(provider) ? IdentType.EksternBruker : IdentType.InternBruker);
    }

    @Override
    public OpenIDToken openIdToken() {
        return firstToken().map(
            j -> new OpenIDToken(ConfigProvider.getOpenIDConfiguration(j.getIssuer()).map(OpenIDConfiguration::type).orElse(OpenIDProvider.TOKENX),
                new TokenString(j.getTokenAsString()))).orElseGet(() -> SubjectHandler.getSubjectHandler().getOpenIDToken());

    }

    private String saksbehandlerToken() {
        return SubjectHandler.getSubjectHandler().getInternSsoToken();

    }

    @Override
    public String samlToken() {
        return SubjectHandler.getSubjectHandler().getSamlToken().getTokenAsString();

    }

    private Optional<JwtToken> firstToken() {
        try {
            return JaxrsTokenValidationContextHolder.getHolder().getTokenValidationContext().getFirstValidToken();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

}
