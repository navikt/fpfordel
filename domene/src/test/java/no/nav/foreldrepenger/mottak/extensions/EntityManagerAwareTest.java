package no.nav.foreldrepenger.mottak.extensions;

import javax.persistence.EntityManager;

public abstract class EntityManagerAwareTest {

    private EntityManager entityManager;

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

}
