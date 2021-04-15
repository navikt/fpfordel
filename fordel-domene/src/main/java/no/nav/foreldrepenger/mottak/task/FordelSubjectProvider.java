package no.nav.foreldrepenger.mottak.task;

import javax.enterprise.context.ApplicationScoped;

import com.nimbusds.jwt.SignedJWT;

import no.nav.vedtak.felles.prosesstask.impl.SubjectProvider;
import no.nav.vedtak.sikkerhet.context.SubjectHandler;

@ApplicationScoped
public class FordelSubjectProvider implements SubjectProvider {

    @Override
    public String getUserIdentity() {
        try {
            return SignedJWT.parse(SubjectHandler.getSubjectHandler().getInternSsoToken()).getJWTClaimsSet().getSubject();
        } catch (Exception e) {
            throw new IllegalArgumentException("Kunne ikke hente brukeridentitet", e);
        }
    }
}
