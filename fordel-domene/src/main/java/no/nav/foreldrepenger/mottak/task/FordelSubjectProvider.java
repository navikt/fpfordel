package no.nav.foreldrepenger.mottak.task;

import javax.enterprise.context.ApplicationScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nimbusds.jwt.SignedJWT;

import no.nav.vedtak.felles.prosesstask.impl.SubjectProvider;
import no.nav.vedtak.sikkerhet.context.SubjectHandler;

@ApplicationScoped
public class FordelSubjectProvider implements SubjectProvider {
    private static final Logger LOG = LoggerFactory.getLogger(FordelSubjectProvider.class);

    @Override
    public String getUserIdentity() {
        try {
            LOG.trace("Henter id fra token");
            var id = SignedJWT.parse(SubjectHandler.getSubjectHandler().getInternSsoToken()).getJWTClaimsSet().getSubject();
            LOG.trace("Id fra token er {}", id);
            return id;
        } catch (Exception e) {
            LOG.warn("Id fra token kunne ikke hentes", e);
            return ("FIXME");
            // throw new IllegalArgumentException("Kunne ikke hente brukeridentitet", e);
        }
    }
}
