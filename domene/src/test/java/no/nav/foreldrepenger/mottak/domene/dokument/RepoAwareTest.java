package no.nav.foreldrepenger.mottak.domene.dokument;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.extension.ExtendWith;

import no.nav.foreldrepenger.mottak.extensions.RepositoryExtension;

@ExtendWith(RepositoryExtension.class)
public class RepoAwareTest {

    private EntityManager entityManager;

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

}
