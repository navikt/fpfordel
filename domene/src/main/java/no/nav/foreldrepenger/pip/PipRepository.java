package no.nav.foreldrepenger.pip;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@Dependent
public class PipRepository {

    private static final String PIP_QUERY = """
        select BRUKER_ID
        from DOKUMENT_METADATA
        where FORSENDELSE_ID in (:dokumentforsendelseIder)
        """;

    private final EntityManager entityManager;

    @Inject
    public PipRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Set<String> hentAktørIdForForsendelser(Set<UUID> dokumentforsendelseIder) {
        Objects.requireNonNull(dokumentforsendelseIder, "dokumentforsendelseIder");

        if (dokumentforsendelseIder.isEmpty()) {
            return Set.of();
        }

        return new HashSet<>(
            entityManager.createNativeQuery(PIP_QUERY).setParameter("dokumentforsendelseIder", dokumentforsendelseIder).getResultList());
    }
}
