package no.nav.foreldrepenger.mottak.person;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.fordel.StringUtil;
import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.pdl.FolkeregisteridentifikatorResponseProjection;
import no.nav.pdl.HentPersonQueryRequest;
import no.nav.pdl.KjoennResponseProjection;
import no.nav.pdl.KjoennType;
import no.nav.pdl.NavnResponseProjection;
import no.nav.pdl.PersonResponseProjection;
import no.nav.vedtak.felles.integrasjon.person.FalskIdentitet;
import no.nav.vedtak.felles.integrasjon.person.PdlException;
import no.nav.vedtak.felles.integrasjon.person.PersonMappers;
import no.nav.vedtak.felles.integrasjon.person.Persondata;
import no.nav.vedtak.util.LRUCache;

@ApplicationScoped
public class PersonTjeneste implements PersonInformasjon {

    private static final Logger LOG = LoggerFactory.getLogger(PersonTjeneste.class);

    private static final int DEFAULT_CACHE_SIZE = 1000;
    private static final long DEFAULT_CACHE_TIMEOUT = TimeUnit.MILLISECONDS.convert(10, TimeUnit.MINUTES);

    protected final Persondata pdl;

    private final LRUCache<String, String> cacheAktørIdTilIdent;
    private final LRUCache<String, String> cacheIdentTilAktørId;

    @Inject
    public PersonTjeneste(Persondata pdl) {
        this(pdl, DEFAULT_CACHE_TIMEOUT);
    }

    PersonTjeneste(Persondata pdl, long timeoutMs) {
        this.pdl = pdl;
        this.cacheAktørIdTilIdent = new LRUCache<>(DEFAULT_CACHE_SIZE, timeoutMs);
        this.cacheIdentTilAktørId = new LRUCache<>(DEFAULT_CACHE_SIZE, timeoutMs);
    }

    private static HentPersonQueryRequest personQuery(String aktørId) {
        var q = new HentPersonQueryRequest();
        q.setIdent(aktørId);
        return q;
    }

    @Override
    public Optional<String> hentAktørIdForPersonIdent(String fnr) {
        try {
            return Optional.ofNullable(cacheIdentTilAktørId.get(fnr)).or(() -> tilAktørId(fnr));
        } catch (PdlException e) {
            LOG.warn("Kunne ikke hente aktørid fra fnr {}", StringUtil.mask(fnr), e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<String> hentPersonIdentForAktørId(String aktørId) {
        try {
            return Optional.ofNullable(cacheAktørIdTilIdent.get(aktørId)).or(() -> tilFnr(aktørId));
        } catch (PdlException e) {
            LOG.warn("Kunne ikke hente fnr fra aktørid {}", aktørId, e);
            return Optional.empty();
        }
    }

    @Override
    public boolean erMann(BehandlingTema behandlingTema, String id) {
        var ytelse = utledYtelse(behandlingTema);
        return pdl.hentPerson(ytelse, personQuery(id),
                new PersonResponseProjection().kjoenn(new KjoennResponseProjection().kjoenn()))
            .getKjoenn()
            .stream()
            .anyMatch(k -> KjoennType.MANN.equals(k.getKjoenn()));
    }

    @Override
    public String hentNavn(BehandlingTema behandlingTema, String id) {
        var ytelse = utledYtelse(behandlingTema);
        var person = pdl.hentPerson(ytelse, personQuery(id),
                new PersonResponseProjection().navn(new NavnResponseProjection().fornavn().mellomnavn().etternavn())
                    .folkeregisteridentifikator(new FolkeregisteridentifikatorResponseProjection().identifikasjonsnummer().status()));
        if (PersonMappers.manglerIdentifikator(person)) {
            var falskId = FalskIdentitet.finnFalskIdentitet(id, pdl).orElse(null);
            if (falskId != null) {
                return falskId.navn();
            }
        }
        return PersonMappers.mapNavn(person).orElse("Ukjent Navn");
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

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [pdl=" + pdl + "]";
    }

    private static Persondata.Ytelse utledYtelse(BehandlingTema behandlingTema) {
        if (BehandlingTema.gjelderEngangsstønad(behandlingTema)) {
            return Persondata.Ytelse.ENGANGSSTØNAD;
        } else if (BehandlingTema.gjelderSvangerskapspenger(behandlingTema)) {
            return Persondata.Ytelse.SVANGERSKAPSPENGER;
        } else {
            return Persondata.Ytelse.FORELDREPENGER;
        }
    }

}
