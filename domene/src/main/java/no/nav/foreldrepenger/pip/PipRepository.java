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

import no.nav.vedtak.felles.jpa.VLPersistenceUnit;

@ApplicationScoped
public class PipRepository {

    private EntityManager entityManager;

    public PipRepository() {
    }

    @Inject
    public PipRepository(@VLPersistenceUnit EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @SuppressWarnings("unchecked")
    public Set<String> hentAktørIdForForsendelser(Set<UUID> dokumentforsendelseIder) {
        Objects.requireNonNull(dokumentforsendelseIder, "dokumentforsendelseIder");

        if (dokumentforsendelseIder.isEmpty()) {
            return Collections.emptySet();
        }

        String sql = "select BRUKER_ID " +
                "from DOKUMENT_METADATA " +
                "where FORSENDELSE_ID in (:dokumentforsendelseIder)";

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("dokumentforsendelseIder", dokumentforsendelseIder);

        return new HashSet<>(query.getResultList());
    }
}
