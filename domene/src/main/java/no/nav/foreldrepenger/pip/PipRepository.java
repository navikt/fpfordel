package no.nav.foreldrepenger.pip;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import no.nav.foreldrepenger.journalføring.domene.JournalpostId;
import no.nav.foreldrepenger.journalføring.oppgave.lager.AktørId;

@Dependent
public class PipRepository {

    private static final String PIP_QUERY = "select brukerId from DokumentMetadata where forsendelseId in (:dokumentforsendelseIder)";
    protected static final String PIP_OPPGAVE_QUERY = "select brukerId from Oppgave where id in (:journalpostIder) and brukerId is not null";
    protected static final String DOKUMENTFORSENDELSE_IDER = "dokumentforsendelseIder";

    private final EntityManager entityManager;

    @Inject
    public PipRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public Set<String> hentAktørIdForForsendelser(Set<UUID> dokumentforsendelseIder) {
        Objects.requireNonNull(dokumentforsendelseIder, DOKUMENTFORSENDELSE_IDER);

        if (dokumentforsendelseIder.isEmpty()) {
            return Set.of();
        }

        return entityManager.createQuery(PIP_QUERY, String.class)
            .setParameter(DOKUMENTFORSENDELSE_IDER, dokumentforsendelseIder)
            .getResultStream()
            .collect(Collectors.toUnmodifiableSet());
    }

    public Set<String> hentAktørIdForOppgave(Set<JournalpostId> journalpostIder) {
        Objects.requireNonNull(journalpostIder, DOKUMENTFORSENDELSE_IDER);

        if (journalpostIder.isEmpty()) {
            return Set.of();
        }

        return entityManager.createQuery(PIP_OPPGAVE_QUERY, AktørId.class)
            .setParameter("journalpostIder", journalpostIder.stream().map(JournalpostId::getVerdi).toList())
            .getResultStream()
            .map(AktørId::getId)
            .collect(Collectors.toUnmodifiableSet());
    }
}
