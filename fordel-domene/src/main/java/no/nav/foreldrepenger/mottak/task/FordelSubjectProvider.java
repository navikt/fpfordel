package no.nav.foreldrepenger.mottak.task;

import static com.nimbusds.jwt.SignedJWT.parse;
import static no.nav.vedtak.isso.SystemUserIdTokenProvider.getSystemUserIdToken;

import javax.enterprise.context.ApplicationScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.felles.prosesstask.impl.SubjectProvider;

@ApplicationScoped
public class FordelSubjectProvider implements SubjectProvider {
    private static final Logger LOG = LoggerFactory.getLogger(FordelSubjectProvider.class);

    @Override
    public String getUserIdentity() {
        try {
            LOG.trace("Henter id fra token");
            var id = parse(getSystemUserIdToken().getToken()).getJWTClaimsSet().getSubject();
            LOG.trace("Id fra token er {}", id);
            return id;
        } catch (Exception e) {
            LOG.warn("Id fra token kunne ikke hentes", e);
            return ("srvfpfordel");
        }
    }
}
