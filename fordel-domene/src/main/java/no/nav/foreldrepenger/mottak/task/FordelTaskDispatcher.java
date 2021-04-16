package no.nav.foreldrepenger.mottak.task;

import static no.nav.vedtak.log.mdc.MDCOperations.getConsumerId;
import static no.nav.vedtak.sikkerhet.context.SubjectHandler.getSubjectHandler;
import static no.nav.vedtak.sikkerhet.domene.SluttBruker.internBruker;

import java.security.Principal;
import java.text.ParseException;
import java.util.Date;
import java.util.Optional;
import java.util.function.Predicate;

import javax.enterprise.context.ApplicationScoped;
import javax.security.auth.Subject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nimbusds.jwt.SignedJWT;

import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.impl.BasicCdiProsessTaskDispatcher;
import no.nav.vedtak.isso.SystemUserIdTokenProvider;
import no.nav.vedtak.sikkerhet.context.ThreadLocalSubjectHandler;
import no.nav.vedtak.sikkerhet.domene.AuthenticationLevelCredential;
import no.nav.vedtak.sikkerhet.domene.ConsumerId;
import no.nav.vedtak.sikkerhet.domene.OidcCredential;

@ApplicationScoped
public class FordelTaskDispatcher extends BasicCdiProsessTaskDispatcher {

    private static final Logger LOG = LoggerFactory.getLogger(FordelTaskDispatcher.class);
    private static final int LEVEL_INTERN_BRUKER = 4;

    @Override
    public void dispatch(ProsessTaskData task) throws Exception {
        var sh = subjectHandler();
        var sub = new Subject();
        var token = SystemUserIdTokenProvider.getSystemUserIdToken().getToken();
        sub.getPublicCredentials().add(new OidcCredential(token));
        sub.getPublicCredentials().add(new AuthenticationLevelCredential(LEVEL_INTERN_BRUKER));
        sub.getPrincipals().add(consumerId());
        sub.getPrincipals().add(internBruker(subject(token)));
        sh.setSubject(sub);
        super.dispatch(task);
    }

    private static boolean isExpired(Subject sub) {
        return Optional.ofNullable(sub)
                .map(s -> s.getPublicCredentials(OidcCredential.class)
                        .stream()
                        .map(OidcCredential::getToken)
                        .filter(isNotExpired())
                        .findFirst()
                        .isEmpty())
                .orElse(true);

    }

    private static String subject(String token) {
        try {
            return SignedJWT.parse(token).getJWTClaimsSet().getSubject();
        } catch (ParseException e) {
            throw new IllegalArgumentException("Kunne ikke parse token", e);
        }
    }

    private static Principal consumerId() {
        return Optional.ofNullable(getConsumerId())
                .map(ConsumerId::new)
                .orElseGet(ConsumerId::new);
    }

    private static ThreadLocalSubjectHandler subjectHandler() {
        var subjectHandler = getSubjectHandler();
        if (subjectHandler instanceof ThreadLocalSubjectHandler h) {
            return h;
        }
        throw new IllegalArgumentException("Krever subject handler av klasse "
                + ThreadLocalSubjectHandler.class + ", men fikk istedet: " + subjectHandler.getClass());
    }

    private static Predicate<String> isNotExpired() {
        return token -> {
            try {
                var exp = SignedJWT.parse(token).getJWTClaimsSet().getExpirationTime();
                LOG.trace("Tokenet utgår {}", exp);
                return exp.after(new Date());
            } catch (ParseException e) {
                throw new IllegalArgumentException("Kunne ikke parse token", e);
            }
        };
    }
}
