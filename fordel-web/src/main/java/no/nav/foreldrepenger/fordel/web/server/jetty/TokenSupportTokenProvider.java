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
import no.nav.vedtak.sikkerhet.context.SubjectHandler;

@Alternative
@Dependent
@Priority(100)
public class TokenSupportTokenProvider implements TokenProvider {

    private static final Logger LOG = LoggerFactory.getLogger(TokenSupportTokenProvider.class);

    @Override
    public String getUid() {
        return firstToken("UID")
                .map(JwtToken::getSubject)
                .orElseGet(() -> SubjectHandler.getSubjectHandler().getUid());
    }

    @Override
    public String userToken() {
        return firstToken("USER")
                .map(JwtToken::getTokenAsString)
                .orElseGet(() -> saksbehandlerToken());
    }

    private String saksbehandlerToken() {
        return SubjectHandler.getSubjectHandler().getInternSsoToken();

    }

    @Override
    public String samlToken() {
        return SubjectHandler.getSubjectHandler().getSamlToken().getTokenAsString();

    }

    private Optional<JwtToken> firstToken(String type) {
        try {
            var token = JaxrsTokenValidationContextHolder.getHolder().getTokenValidationContext().getFirstValidToken();
            token.ifPresent(t -> LOG.trace("{} Issuer {}", type, t.getIssuer()));
            return token;
        } catch (Exception e) {
            return Optional.empty();
        }
    }

}
