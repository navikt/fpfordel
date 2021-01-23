package no.nav.foreldrepenger.mottak.person;

import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.function.Predicate.not;
import static no.nav.pdl.AdressebeskyttelseGradering.UGRADERT;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;
import com.google.common.base.Joiner;

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
import no.nav.vedtak.felles.integrasjon.rest.jersey.Jersey;

@ApplicationScoped
public class PersonTjeneste implements PersonInformasjon {

    private static final Logger LOG = LoggerFactory.getLogger(PersonTjeneste.class);

    private static final String SPSF = "SPSF";
    private static final String SPFO = "SPFO";
    private static final int DEFAULT_CACHE_SIZE = 1000;
    private static final long DEFAULT_CACHE_TIMEOUT_HOURS = 2;

    private static final Cache<String, String> IDCACHE = cache(DEFAULT_CACHE_SIZE, DEFAULT_CACHE_TIMEOUT_HOURS, HOURS);
    private static final Cache<String, String> AKTØRCACHE = cache(DEFAULT_CACHE_SIZE, DEFAULT_CACHE_TIMEOUT_HOURS, HOURS);
    private Pdl pdl;

    PersonTjeneste() {
    }

    @Inject
    public PersonTjeneste(@Jersey Pdl pdl) {
        this.pdl = pdl;
    }

    @Override
    public Optional<String> hentAktørIdForPersonIdent(String personIdent) {
        try {
            return Optional.ofNullable(AKTØRCACHE.get(personIdent, fraFnr()));
        } catch (PdlException e) {
            LOG.warn("Kunne ikke hente fnr fra aktørid {}", personIdent, e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<String> hentPersonIdentForAktørId(String aktørId) {
        try {
            return Optional.ofNullable(IDCACHE.get(aktørId, fraAktørid()));
        } catch (PdlException e) {
            LOG.warn("Kunne ikke hente personid fra aktørid {}", aktørId, e);
            return Optional.empty();
        }
    }

    @Override
    public String hentNavn(String aktørId) {

        return pdl.hentPerson(personQuery(aktørId),
                new PersonResponseProjection().navn(new NavnResponseProjection().forkortetNavn().fornavn().mellomnavn().etternavn())).getNavn()
                .stream()
                .map(PersonTjeneste::mapNavn)
                .findFirst()
                .orElseThrow();
    }

    @Override
    public GeoTilknytning hentGeografiskTilknytning(String aktørId) {

        var query = new HentGeografiskTilknytningQueryRequest();
        query.setIdent(aktørId);
        var pgt = new GeografiskTilknytningResponseProjection().gtType().gtBydel().gtKommune().gtLand();
        var pp = new PersonResponseProjection()
                .adressebeskyttelse(new AdressebeskyttelseResponseProjection().gradering());
        var gt = new GeoTilknytning(tilknytning(pdl.hentGT(query, pgt)),
                diskresjonskode(pdl.hentPerson(personQuery(aktørId), pp)));
        if (gt.getTilknytning() == null) {
            LOG.info("FPFORDEL PDL mangler GT for {}", aktørId);
        }
        return gt;

    }

    private Function<? super String, ? extends String> fraFnr() {
        return id -> pdl.hentAktørIdForPersonIdent(id)
                .orElseGet(() -> null);
    }

    private Function<? super String, ? extends String> fraAktørid() {
        return id -> pdl.hentPersonIdentForAktørId(id)
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
        var kode = person.getAdressebeskyttelse().stream()
                .map(Adressebeskyttelse::getGradering)
                .filter(not(UGRADERT::equals))
                .findFirst()
                .orElse(UGRADERT);
        return switch (kode) {
            case STRENGT_FORTROLIG, STRENGT_FORTROLIG_UTLAND -> SPSF;
            case FORTROLIG -> SPFO;
            default -> null;
        };
    }

    private static Cache<String, String> cache(int size, long timeout, TimeUnit unit) {
        return Caffeine.newBuilder()
                .expireAfterWrite(timeout, unit)
                .maximumSize(size)
                .removalListener(new RemovalListener<String, String>() {
                    @Override
                    public void onRemoval(String key, String value, RemovalCause cause) {
                        LOG.info("Fjerner {} for {} grunnet {}", value, key, cause);
                    }
                })
                .build();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [cacheAktørIdTilIdent=" + IDCACHE + ", cacheIdentTilAktørId=" + AKTØRCACHE
                + "]";
    }
}
