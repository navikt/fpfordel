package no.nav.foreldrepenger.pip;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;

@ApplicationScoped
public class PipRepository {

    private static String PIP_QUERY = """ 
                select BRUKER_ID 
                from DOKUMENT_METADATA 
                where FORSENDELSE_ID in (:dokumentforsendelseIder)
                """;

    private EntityManager entityManager;

    public PipRepository() {
    }

    @Inject
    public PipRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @SuppressWarnings("unchecked")
    public Set<String> hentAktørIdForForsendelser(Set<UUID> dokumentforsendelseIder) {
        Objects.requireNonNull(dokumentforsendelseIder, "dokumentforsendelseIder");

        if (dokumentforsendelseIder.isEmpty()) {
            return Collections.emptySet();
        }

        Query query = entityManager.createNativeQuery(PIP_QUERY);
        query.setParameter("dokumentforsendelseIder", dokumentforsendelseIder);

        return new HashSet<>(query.getResultList());
    }
}
