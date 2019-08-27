package no.nav.foreldrepenger.fordel.kodeverk;

import static java.util.stream.Collectors.joining;
import static no.nav.foreldrepenger.fordel.kodeverk.KodeverkFeil.FEILFACTORY;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.DiscriminatorValue;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.jpa.QueryHints;

import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.jpa.VLPersistenceUnit;
import no.nav.vedtak.util.LRUCache;

@ApplicationScoped
public class KodeverkRepositoryImpl implements KodeverkRepository {

    private static final long CACHE_ELEMENT_LIVE_TIME = TimeUnit.MILLISECONDS.convert(1, TimeUnit.MINUTES);
    private LRUCache<String, Kodeliste> kodelisteCache = new LRUCache<>(1000, CACHE_ELEMENT_LIVE_TIME);
    private EntityManager entityManager;

    KodeverkRepositoryImpl() {
        // for CDI proxy
    }

    @Inject
    public KodeverkRepositoryImpl(@VLPersistenceUnit EntityManager entityManager) {
        Objects.requireNonNull(entityManager, "entityManager"); //$NON-NLS-1$
        this.entityManager = entityManager;
    }

    @Override
    public <V extends Kodeliste> Optional<V> finnOptional(Class<V> cls, String kode) {
        CriteriaQuery<V> criteria = createCriteria(cls, Collections.singletonList(kode));
        List<V> list = entityManager.createQuery(criteria)
                .setHint(QueryHints.HINT_READONLY, "true")
                .getResultList();
        if (list.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(list.get(0)); // NOSONAR
        }
    }

    @Override
    public <V extends Kodeliste> V finn(Class<V> cls, String kode) {
        String cacheKey = cls.getName() + kode;
        @SuppressWarnings("unchecked")
        Optional<V> fraCache = Optional.ofNullable((V) kodelisteCache.get(cacheKey));
        return fraCache.orElseGet(() -> {
            V finnEM = finnFraEM(cls, kode);
            kodelisteCache.put(cacheKey, finnEM);
            return finnEM;
        });
    }

    @Override
    public <V extends Kodeliste> V finn(Class<V> cls, V kodelisteKonstant) {
        return finn(cls, kodelisteKonstant.getKode());
    }

    @Override
    public <V extends Kodeliste> V finnForKodeverkEiersKode(Class<V> cls, String offisiellKode) {
        String cacheKey = cls.getName() + offisiellKode;
        @SuppressWarnings("unchecked")
        Optional<V> fraCache = Optional.ofNullable((V) kodelisteCache.get(cacheKey));
        return fraCache.orElseGet(() -> {
            V result = finnForKodeverkEiersKodeFraEM(cls, offisiellKode);
            kodelisteCache.put(cacheKey, result);
            return result;
        });
    }

    @Override
    public <V extends Kodeliste> V finnForKodeverkEiersKode(Class<V> cls, String offisiellKode, V defaultValue) {
        Objects.requireNonNull(defaultValue, "defaultValue kan ikke være null"); //$NON-NLS-1$
        V kodeliste;
        try {
            kodeliste = finnForKodeverkEiersKode(cls, offisiellKode);
        } catch (TekniskException e) { // NOSONAR
            // Vi skal tåle ukjent offisiellKode
            kodeliste = finn(cls, defaultValue);
        }
        return kodeliste;
    }

    @Override
    public <V extends Kodeliste> V finnForKodeverkEiersTermNavn(Class<V> cls, String termNavn, V defaultValue) {
        Objects.requireNonNull(defaultValue, "defaultValue kan ikke være null"); //$NON-NLS-1$
        V kodeliste;
        CriteriaQuery<V> criteria = createCriteria(cls, "navn", Collections.singletonList(termNavn));
        try {
            kodeliste = entityManager.createQuery(criteria)
                    .setHint(QueryHints.HINT_READONLY, "true")
                    .getSingleResult();
        } catch (NoResultException e) { // NOSONAR
            kodeliste = finn(cls, defaultValue);
        }
        return kodeliste;
    }

    @Override
    public <V extends Kodeliste> List<V> finnForKodeverkEiersKoder(Class<V> cls, String... offisiellKoder) {
        List<String> koderIkkeICache = new ArrayList<>();
        List<V> result = new ArrayList<>();
        //Traverserer alle koder vi skal finne kodeverk for. Kodene som finnes i cache hentes derfra. De som ikke finnes i cache hentes fra DB.
        Arrays.stream(offisiellKoder).forEach(kode -> {
            //For hver kode
            @SuppressWarnings("unchecked")
            V kodeliste = (V) kodelisteCache.get(kode);
            if (kodeliste == null) {
                koderIkkeICache.add(kode);
            } else {
                result.add(kodeliste);
            }
        });
        result.addAll(finnForKodeverkEiersKoderFraEm(cls, koderIkkeICache));
        return result;
    }

    @Override
    public <V extends Kodeliste> List<V> finnListe(Class<V> cls, List<String> koder) {
        List<String> koderIkkeICache = new ArrayList<>();
        List<V> result = new ArrayList<>();
        koder.stream().forEach(kode -> {
            //For hver kode
            @SuppressWarnings("unchecked")
            V kodeliste = (V) kodelisteCache.get(kode);
            if (kodeliste == null) {
                koderIkkeICache.add(kode);
            } else {
                result.add(kodeliste);
            }
        });
        result.addAll(finnListeFraEm(cls, koderIkkeICache));
        return result;
    }

    @Override
    public <V extends Kodeliste> List<V> hentAlle(Class<V> cls) {
        CriteriaQuery<V> criteria = entityManager.getCriteriaBuilder().createQuery(cls);
        criteria.select(criteria.from(cls));
        return entityManager.createQuery(criteria)
                .setHint(QueryHints.HINT_READONLY, "true")
                .getResultList();
    }

    private <V extends Kodeliste> List<V> finnListeFraEm(Class<V> cls, List<String> koder) {
        CriteriaQuery<V> criteria = createCriteria(cls, koder);
        return entityManager.createQuery(criteria)
                .setHint(QueryHints.HINT_READONLY, "true")
                .getResultList();
    }

    private <V extends Kodeliste> List<V> finnForKodeverkEiersKoderFraEm(Class<V> cls, List<String> offisiellKoder) {
        try {
            CriteriaQuery<V> criteria = createCriteria(cls, "offisiellKode", offisiellKoder);
            return entityManager.createQuery(criteria)
                    .setHint(QueryHints.HINT_READONLY, "true")
                    .getResultList();
        } catch (NoResultException e) {
            String koder = offisiellKoder.stream().collect(joining(","));
            throw FEILFACTORY.kanIkkeFinneKodeverkForKoder(cls.getSimpleName(), koder, e).toException();
        }
    }

    private <V extends Kodeliste> V finnFraEM(Class<V> cls, String kode) {
        CriteriaQuery<V> criteria = createCriteria(cls, Collections.singletonList(kode));
        try {
            return entityManager.createQuery(criteria)
                    .setHint(QueryHints.HINT_READONLY, "true")
                    .getSingleResult();
        } catch (NoResultException e) {
            throw FEILFACTORY.kanIkkeFinneKodeverk(cls.getSimpleName(), kode, e).toException();
        }
    }

    private <V extends Kodeliste> V finnForKodeverkEiersKodeFraEM(Class<V> cls, String offisiellKode) {
        CriteriaQuery<V> criteria = createCriteria(cls, "offisiellKode", Collections.singletonList(offisiellKode));
        try {
            return entityManager.createQuery(criteria)
                    .setHint(QueryHints.HINT_READONLY, "true")
                    .getSingleResult();
        } catch (NoResultException e) {
            throw FEILFACTORY.kanIkkeFinneKodeverkForOffisiellKode(cls.getSimpleName(), offisiellKode, e).toException();
        }
    }

    protected <V extends Kodeliste> CriteriaQuery<V> createCriteria(Class<V> cls, List<String> koder) {
        return createCriteria(cls, "kode", koder);
    }

    protected <V extends Kodeliste> CriteriaQuery<V> createCriteria(Class<V> cls, String felt, List<String> koder) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<V> criteria = builder.createQuery(cls);

        DiscriminatorValue discVal = cls.getDeclaredAnnotation(DiscriminatorValue.class);
        Objects.requireNonNull(discVal, "Mangler @DiscriminatorValue i klasse:" + cls); //$NON-NLS-1$
        String kodeverk = discVal.value();
        Root<V> from = criteria.from(cls);
        criteria.where(builder.and(
                builder.equal(from.get("kodeverk"), kodeverk), //$NON-NLS-1$
                from.get(felt).in(koder))); // $NON-NLS-1$
        return criteria;
    }

}
