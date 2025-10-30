package no.nav.foreldrepenger.mottak.domene.dokument;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@ApplicationScoped
public class DokumentRepository {

    private static final String LOKALT_OPPHAV = "FORDEL";
    private EntityManager em;

    @Inject
    public DokumentRepository(EntityManager entityManager) {
        this.em = Objects.requireNonNull(entityManager);
    }

    DokumentRepository() {
    }

    public void lagre(Object entity) {
        em.persist(entity);
        em.flush();
    }

    public void lagreJournalpostLokal(String journalpostId, String kanal, String tilstand, String referanse) {
        var journalpost = new Journalpost(journalpostId, tilstand, kanal, referanse, LOKALT_OPPHAV);
        lagre(journalpost);
    }

    public List<Journalpost> hentJournalposter(String journalpostId) {
        return em.createQuery("from Journalpost where journalpostId = :journalpostId", Journalpost.class)
            .setParameter("journalpostId", journalpostId)
            .getResultList();
    }

    public List<Journalpost> hentAlleJournalposter() {
        return em.createQuery("from Journalpost", Journalpost.class)
            .getResultList();
    }

    public int slettJournalpostLokalEldreEnn(LocalDate dato) {
        return em.createQuery("delete from Journalpost where opprettetTidspunkt < :opprettet").setParameter("opprettet", dato.atStartOfDay()).executeUpdate();
    }
}
