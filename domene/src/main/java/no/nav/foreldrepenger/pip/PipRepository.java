package no.nav.foreldrepenger.pip;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@Dependent
public class PipRepository {

    private static final String PIP_QUERY = "select brukerId from DokumentMetadata where forsendelseId in (:dokumentforsendelseIder)";

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

        return entityManager.createQuery(PIP_QUERY, String.class)
            .setParameter("dokumentforsendelseIder", dokumentforsendelseIder)
            .getResultStream()
            .collect(Collectors.toUnmodifiableSet());
    }
}
