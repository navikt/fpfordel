package no.nav.foreldrepenger.pip;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import no.nav.foreldrepenger.journalføring.domene.JournalpostId;
import no.nav.foreldrepenger.journalføring.oppgave.lager.AktørId;

@Dependent
public class PipRepository {

    private static final String PIP_OPPGAVE_QUERY = "select brukerId from Oppgave where id in (:journalpostIder) and brukerId is not null";
    private static final String JOURNALPOST_IDER = "journalpostIder";

    private final EntityManager entityManager;

    @Inject
    public PipRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Set<String> hentAktørIdForOppgave(Set<JournalpostId> journalpostIder) {
        Objects.requireNonNull(journalpostIder, JOURNALPOST_IDER);

        if (journalpostIder.isEmpty()) {
            return Set.of();
        }

        return entityManager.createQuery(PIP_OPPGAVE_QUERY, AktørId.class)
            .setParameter(JOURNALPOST_IDER, journalpostIder.stream().map(JournalpostId::getVerdi).toList())
            .getResultStream()
            .map(AktørId::getId)
            .collect(Collectors.toUnmodifiableSet());
    }
}
