package no.nav.foreldrepenger.fordel.konfig;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.hibernate.jpa.QueryHints;

import no.nav.foreldrepenger.fordel.kodeverk.KonfigVerdiGruppe;
import no.nav.vedtak.felles.jpa.HibernateVerktøy;
import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

@ApplicationScoped
public class KonfigVerdiRepositoryImpl implements KonfigVerdiRepository {

    private EntityManager entityManager;

    KonfigVerdiRepositoryImpl() {
        // for CDI proxy
    }

    @Inject
    public KonfigVerdiRepositoryImpl(@VLPersistenceUnit EntityManager entityManager) {
        Objects.requireNonNull(entityManager, "entityManager"); //$NON-NLS-1$
        this.entityManager = entityManager;
    }

    @Override
    public Optional<KonfigVerdiEntitet> finnVerdiFor(KonfigVerdiGruppe gruppe, String kode, LocalDate dato) {
        TypedQuery<KonfigVerdiEntitet> query = entityManager.createQuery(
                "SELECT kv from KonfigVerdi AS kv WHERE kv.konfigVerdiGruppe = :gruppe AND kv.konfigVerdiKode.kode =:kode AND :dato between kv.fomDato AND kv.tomDato", //$NON-NLS-1$
                KonfigVerdiEntitet.class);
        query.setParameter("dato", dato); //$NON-NLS-1$
        query.setParameter("kode", kode); //$NON-NLS-1$
        query.setParameter("gruppe", gruppe); //$NON-NLS-1$
        query.setHint(QueryHints.HINT_READONLY, "true");//$NON-NLS-1$
        return HibernateVerktøy.hentUniktResultat(query);

    }

    @Override
    public List<KonfigVerdiEntitet> finnVerdierFor(KonfigVerdiGruppe gruppe, LocalDate dato) {
        TypedQuery<KonfigVerdiEntitet> query = entityManager.createQuery(
                "SELECT kv from KonfigVerdi AS kv WHERE kv.konfigVerdiGruppe = :gruppe AND :dato between kv.fomDato AND kv.tomDato", //$NON-NLS-1$
                KonfigVerdiEntitet.class);
        query.setParameter("dato", dato); //$NON-NLS-1$
        query.setParameter("gruppe", gruppe); //$NON-NLS-1$
        query.setHint(QueryHints.HINT_READONLY, "true");//$NON-NLS-1$
        return query.getResultList();
    }

    @Override
    public List<KonfigVerdiEntitet> finnAlleVerdier(LocalDate dato) {
        TypedQuery<KonfigVerdiEntitet> query = entityManager.createQuery(
                "SELECT kv from KonfigVerdi AS kv WHERE :dato between kv.fomDato AND kv.tomDato", //$NON-NLS-1$
                KonfigVerdiEntitet.class);
        query.setParameter("dato", dato); //$NON-NLS-1$
        query.setHint(QueryHints.HINT_READONLY, "true");//$NON-NLS-1$
        return query.getResultList();
    }
}
