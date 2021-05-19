package no.nav.foreldrepenger.mottak.person;

import static java.util.function.Predicate.not;
import static no.nav.pdl.AdressebeskyttelseGradering.UGRADERT;
import static no.nav.vedtak.sikkerhet.context.SubjectHandler.getSubjectHandler;
import static no.nav.vedtak.util.env.ConfidentialMarkerFilter.CONFIDENTIAL;

import java.time.Duration;
import java.util.Date;
import java.util.Optional;
import java.util.function.Function;

import javax.enterprise.context.Dependent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;
import com.google.common.base.Joiner;
import com.nimbusds.jwt.SignedJWT;

import no.nav.foreldrepenger.fordel.StringUtil;
import no.nav.pdl.Adressebeskyttelse;
import no.nav.pdl.AdressebeskyttelseResponseProjection;
import no.nav.pdl.GeografiskTilknytning;
import no.nav.pdl.GeografiskTilknytningResponseProjection;
import no.nav.pdl.HentGeografiskTilknytningQueryRequest;
import no.nav.pdl.HentPersonQueryRequest;
import no.nav.pdl.Navn;
import no.nav.pdl.NavnResponseProjection;
import no.nav.pdl.Person;
import no.nav.pdl.PersonResponseProjection;
import no.nav.vedtak.felles.integrasjon.pdl.Pdl;
import no.nav.vedtak.felles.integrasjon.pdl.PdlException;

@Dependent
public class AbstractPersonTjeneste implements PersonInformasjon {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractPersonTjeneste.class);

    protected static final String SPSF = "SPSF";
    protected static final String SPFO = "SPFO";
    protected static final int DEFAULT_CACHE_SIZE = 1000;
    protected static final Duration DEFAULT_CACHE_DURATION = Duration.ofMinutes(55);

    protected final LoadingCache<String, String> tilFnr;
    protected final LoadingCache<String, String> tilAktør;
    protected final Pdl pdl;

    AbstractPersonTjeneste(Pdl pdl, LoadingCache<String, String> tilFnr, LoadingCache<String, String> tilAktør) {
        this.pdl = pdl;
        this.tilFnr = tilFnr;
        this.tilAktør = tilAktør;
    }

    @Override
    public Optional<String> hentAktørIdForPersonIdent(String fnr) {
        try {
            LOG.trace(CONFIDENTIAL, "Henter for {}", fnr);
            return Optional.ofNullable(tilAktør.get(fnr));
        } catch (PdlException e) {
            LOG.warn("Kunne ikke hente aktørid fra fnr {} ({} {})", StringUtil.mask(fnr), e, expiresAt(), e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<String> hentPersonIdentForAktørId(String aktørId) {
        try {
            LOG.trace(CONFIDENTIAL, "Henter for {}", aktørId);
            return Optional.ofNullable(tilFnr.get(aktørId));
        } catch (PdlException e) {
            LOG.warn("Kunne ikke hente fnr fra aktørid {} ({} {})", aktørId, e, expiresAt(), e);
            return Optional.empty();
        }
    }

    @Override
    public String hentNavn(String id) {
        LOG.trace(CONFIDENTIAL, "Henter navn for {}", id);
        return pdl.hentPerson(personQuery(id),
                new PersonResponseProjection().navn(new NavnResponseProjection().forkortetNavn().fornavn().mellomnavn().etternavn())).getNavn()
                .stream()
                .map(AbstractPersonTjeneste::mapNavn)
                .findFirst()
                .orElseThrow();
    }

    @Override
    public GeoTilknytning hentGeografiskTilknytning(String id) {
        LOG.trace(CONFIDENTIAL, "Henter geo-tilknytning for {}", id);
        var query = new HentGeografiskTilknytningQueryRequest();
        query.setIdent(id);
        var pgt = new GeografiskTilknytningResponseProjection().gtType().gtBydel().gtKommune().gtLand();
        var pp = new PersonResponseProjection()
                .adressebeskyttelse(new AdressebeskyttelseResponseProjection().gradering());
        return new GeoTilknytning(tilknytning(pdl.hentGT(query, pgt)),
                diskresjonskode(pdl.hentPerson(personQuery(id), pp)));
    }

    private static Date expiresAt() {
        try {
            return SignedJWT.parse(getSubjectHandler().getInternSsoToken()).getJWTClaimsSet().getExpirationTime();
        } catch (Exception e) {
            LOG.trace("Kunne ikke hente expiration dato fra token", e);
            return null;
        }
    }

    protected static Function<? super String, ? extends String> tilAktørId(Pdl pdl) {
        return fnr -> pdl.hentAktørIdForPersonIdent(fnr)
                .orElseGet(() -> null);
    }

    protected static Function<? super String, ? extends String> tilFnr(Pdl pdl) {
        return aktørId -> pdl.hentPersonIdentForAktørId(aktørId)
                .orElseGet(() -> null);

    }

    private static HentPersonQueryRequest personQuery(String aktørId) {
        var q = new HentPersonQueryRequest();
        q.setIdent(aktørId);
        return q;
    }

    private static String mapNavn(Navn navn) {
        return Optional.ofNullable(navn.getForkortetNavn())
                .orElseGet(() -> Joiner.on(' ')
                        .skipNulls()
                        .join(navn.getEtternavn(), navn.getFornavn(), navn.getMellomnavn()));
    }

    private String tilknytning(GeografiskTilknytning res) {
        if (res == null || res.getGtType() == null)
            return null;
        return switch (res.getGtType()) {
            case BYDEL -> res.getGtBydel();
            case KOMMUNE -> res.getGtKommune();
            case UTLAND -> res.getGtLand();
            default -> null;
        };
    }

    private String diskresjonskode(Person person) {
        return switch (person.getAdressebeskyttelse().stream()
                .map(Adressebeskyttelse::getGradering)
                .filter(not(UGRADERT::equals))
                .findFirst()
                .orElse(UGRADERT)) {
            case STRENGT_FORTROLIG, STRENGT_FORTROLIG_UTLAND -> SPSF;
            case FORTROLIG -> SPFO;
            default -> null;
        };
    }

    protected static LoadingCache<String, String> cache(Function<? super String, ? extends String> loader) {
        return Caffeine.newBuilder()
                .expireAfterWrite(DEFAULT_CACHE_DURATION)
                .maximumSize(DEFAULT_CACHE_SIZE)
                .removalListener(new RemovalListener<String, String>() {
                    @Override
                    public void onRemoval(String key, String value, RemovalCause cause) {
                        LOG.trace("Fjerner {} for {} grunnet {}", value, key, cause);
                    }
                })
                .build(loader::apply);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [pdl=" + pdl + "]";
    }

}
