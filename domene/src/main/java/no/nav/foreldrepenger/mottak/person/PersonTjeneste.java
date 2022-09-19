package no.nav.foreldrepenger.mottak.person;

import static no.nav.vedtak.sikkerhet.context.SubjectHandler.getSubjectHandler;

import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nimbusds.jwt.SignedJWT;

import no.nav.foreldrepenger.fordel.StringUtil;
import no.nav.pdl.Adressebeskyttelse;
import no.nav.pdl.AdressebeskyttelseGradering;
import no.nav.pdl.AdressebeskyttelseResponseProjection;
import no.nav.pdl.GeografiskTilknytning;
import no.nav.pdl.GeografiskTilknytningResponseProjection;
import no.nav.pdl.HentGeografiskTilknytningQueryRequest;
import no.nav.pdl.HentPersonQueryRequest;
import no.nav.pdl.Navn;
import no.nav.pdl.NavnResponseProjection;
import no.nav.pdl.PersonResponseProjection;
import no.nav.vedtak.felles.integrasjon.pdl.Pdl;
import no.nav.vedtak.felles.integrasjon.pdl.PdlException;
import no.nav.vedtak.felles.integrasjon.rest.NativeClient;
import no.nav.vedtak.util.LRUCache;

@ApplicationScoped
public class PersonTjeneste implements PersonInformasjon {

    private static final Logger LOG = LoggerFactory.getLogger(PersonTjeneste.class);

    private static final Set<AdressebeskyttelseGradering> STRENG =
            Set.of(AdressebeskyttelseGradering.STRENGT_FORTROLIG, AdressebeskyttelseGradering.STRENGT_FORTROLIG_UTLAND);

    private static final int DEFAULT_CACHE_SIZE = 1000;
    private static final long DEFAULT_CACHE_TIMEOUT = TimeUnit.MILLISECONDS.convert(10, TimeUnit.MINUTES);

    protected final Pdl pdl;

    private LRUCache<String, String> cacheAktørIdTilIdent;
    private LRUCache<String, String> cacheIdentTilAktørId;

    @Inject
    public PersonTjeneste(@NativeClient Pdl pdl) {
        this(pdl, DEFAULT_CACHE_TIMEOUT);
    }

    PersonTjeneste(Pdl pdl, long timeoutMs) {
        this.pdl = pdl;
        this.cacheAktørIdTilIdent = new LRUCache<>(DEFAULT_CACHE_SIZE, timeoutMs);
        this.cacheIdentTilAktørId = new LRUCache<>(DEFAULT_CACHE_SIZE, timeoutMs);
    }

    @Override
    public Optional<String> hentAktørIdForPersonIdent(String fnr) {
        try {
            return Optional.ofNullable(cacheIdentTilAktørId.get(fnr))
                    .or(() -> tilAktørId(fnr));
        } catch (PdlException e) {
            LOG.warn("Kunne ikke hente aktørid fra fnr {} ({} {})", StringUtil.mask(fnr), e, expiresAt(), e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<String> hentPersonIdentForAktørId(String aktørId) {
        try {
            return Optional.ofNullable(cacheAktørIdTilIdent.get(aktørId))
                    .or(() -> tilFnr(aktørId));
        } catch (PdlException e) {
            LOG.warn("Kunne ikke hente fnr fra aktørid {} ({} {})", aktørId, e, expiresAt(), e);
            return Optional.empty();
        }
    }

    @Override
    public String hentNavn(String id) {
        return pdl.hentPerson(personQuery(id),
                new PersonResponseProjection().navn(new NavnResponseProjection().forkortetNavn().fornavn().mellomnavn().etternavn())).getNavn()
                .stream()
                .map(PersonTjeneste::mapNavn)
                .findFirst()
                .orElseThrow();
    }

    @Override
    public String hentGeografiskTilknytning(String id) {
        var query = new HentGeografiskTilknytningQueryRequest();
        query.setIdent(id);
        var pgt = new GeografiskTilknytningResponseProjection().gtType().gtBydel().gtKommune().gtLand();
        return tilknytning(pdl.hentGT(query, pgt));
    }

    @Override
    public boolean harStrengDiskresjonskode(String id) {
        var pp = new PersonResponseProjection()
                .adressebeskyttelse(new AdressebeskyttelseResponseProjection().gradering());
        return pdl.hentPerson(personQuery(id), pp).getAdressebeskyttelse().stream()
                .map(Adressebeskyttelse::getGradering)
                .anyMatch(STRENG::contains);
    }

    private static Date expiresAt() {
        try {
            return SignedJWT.parse(getSubjectHandler().getInternSsoToken()).getJWTClaimsSet().getExpirationTime();
        } catch (Exception e) {
            LOG.trace("Kunne ikke hente expiration dato fra token", e);
            return null;
        }
    }

    private Optional<String> tilAktørId(String fnr) {
        var aktørId = pdl.hentAktørIdForPersonIdent(fnr);
        aktørId.ifPresent(a -> cacheIdentTilAktørId.put(fnr, a));
        return aktørId;
    }

    private Optional<String> tilFnr(String aktørId) {
        var personIdent = pdl.hentPersonIdentForAktørId(aktørId);
        personIdent.ifPresent(pi -> {
            cacheAktørIdTilIdent.put(aktørId, pi);
            cacheIdentTilAktørId.put(pi, aktørId);
        });
        return personIdent;

    }

    private static HentPersonQueryRequest personQuery(String aktørId) {
        var q = new HentPersonQueryRequest();
        q.setIdent(aktørId);
        return q;
    }

    private static String mapNavn(Navn navn) {
        return Optional.ofNullable(navn.getForkortetNavn())
                .orElseGet(() -> navn.getEtternavn() + " " + navn.getFornavn() +
                        Optional.ofNullable(navn.getMellomnavn()).map(n -> " " + n).orElse(""));
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

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [pdl=" + pdl + "]";
    }

}
