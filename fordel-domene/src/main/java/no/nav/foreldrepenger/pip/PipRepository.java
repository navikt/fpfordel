package no.nav.foreldrepenger.pip;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.persistence.EntityManager;

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

    @SuppressWarnings("unchecked")
    public Set<String> hentAktørIdForForsendelser(Set<UUID> dokumentforsendelseIder) {
        Objects.requireNonNull(dokumentforsendelseIder, "dokumentforsendelseIder");

        if (dokumentforsendelseIder.isEmpty()) {
            return Collections.emptySet();
        }

        return new HashSet<String>(entityManager.createNativeQuery(PIP_QUERY)
                .setParameter("dokumentforsendelseIder", dokumentforsendelseIder).getResultList());
    }
}
