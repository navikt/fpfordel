package no.nav.foreldrepenger.mottak.domene.dokument;

import static no.nav.vedtak.felles.jpa.HibernateVerktøy.hentEksaktResultat;
import static no.nav.vedtak.felles.jpa.HibernateVerktøy.hentUniktResultat;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.hibernate.exception.ConstraintViolationException;

import no.nav.foreldrepenger.fordel.kodeverdi.ArkivFilType;
import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseStatus;

@ApplicationScoped
public class DokumentRepository {

    private static final String LOKALT_OPPHAV = "FORDEL";
    private static final String FORSENDELSE_ID = "forsendelseId";
    private static final String HOVED_DOKUMENT = "hovedDokument";
    private static final String ARKIV_FILTYPE = "arkivFilType";
    private EntityManager entityManager;

    DokumentRepository() {
    }

    @Inject
    public DokumentRepository(EntityManager entityManager) {
        this.entityManager = Objects.requireNonNull(entityManager);
    }

    public void lagre(Dokument dokument) {
        entityManager.persist(dokument);
        entityManager.flush();
    }

    public void lagre(DokumentMetadata dokumentMetadata) {
        try {
            entityManager.persist(dokumentMetadata);
            entityManager.flush();
        } catch (PersistenceException e) {
            Throwable cause = e.getCause();
            if ((cause instanceof ConstraintViolationException) &&
                    ((ConstraintViolationException) cause).getConstraintName()
                            .contains(DokumentMetadata.UNIQUE_FORSENDELSE_ID_CONSTRAINT)) {
                throw DokumentFeil.FACTORY.constraintForsendelseId(dokumentMetadata.getForsendelseId()).toException();
            } else {
                throw e;
            }
        }
    }

    public Optional<Dokument> hentUnikDokument(UUID forsendelseId, boolean hovedDokument, ArkivFilType arkivFilType) {
        TypedQuery<Dokument> query = entityManager.createQuery(
                "from Dokument where forsendelseId = :forsendelseId and hovedDokument = :hovedDokument and arkivFilType = :arkivFilType",
                Dokument.class)
                .setParameter(FORSENDELSE_ID, forsendelseId)
                .setParameter(HOVED_DOKUMENT, hovedDokument)
                .setParameter(ARKIV_FILTYPE, arkivFilType);

        List<Dokument> resultatListe = query.getResultList();
        if (resultatListe.size() > 1) {
            throw DokumentFeil.FACTORY.fantIkkeUnikResultat().toException();
        } else if (resultatListe.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(resultatListe.get(0));
    }

    public List<Dokument> hentDokumenter(UUID forsendelseId) {
        TypedQuery<Dokument> query = entityManager.createQuery(
                "from Dokument where forsendelseId = :forsendelseId", Dokument.class)
                .setParameter(FORSENDELSE_ID, forsendelseId);
        return query.getResultList();
    }

    public DokumentMetadata hentEksaktDokumentMetadata(UUID forsendelseId) {
        TypedQuery<DokumentMetadata> query = entityManager.createQuery(
                "from DokumentMetadata where forsendelseId = :forsendelseId", DokumentMetadata.class)
                .setParameter(FORSENDELSE_ID, forsendelseId);
        return hentEksaktResultat(query);
    }

    public Optional<DokumentMetadata> hentUnikDokumentMetadata(UUID forsendelseId) {
        TypedQuery<DokumentMetadata> query = entityManager.createQuery(
                "from DokumentMetadata where forsendelseId = :forsendelseId", DokumentMetadata.class)
                .setParameter(FORSENDELSE_ID, forsendelseId);
        return hentUniktResultat(query);
    }

    public void slettForsendelse(UUID forsendelseId) {
        Query query1 = entityManager.createNativeQuery("delete from DOKUMENT where FORSENDELSE_ID  = :forsendelseId");
        query1.setParameter(FORSENDELSE_ID, forsendelseId); // NOSONAR
        query1.executeUpdate();

        Query query2 = entityManager
                .createNativeQuery("delete from DOKUMENT_METADATA where FORSENDELSE_ID  = :forsendelseId");
        query2.setParameter(FORSENDELSE_ID, forsendelseId); // NOSONAR
        query2.executeUpdate();

        entityManager.flush();
    }

    public void oppdaterForsendelseMedArkivId(UUID forsendelseId, String arkivId, ForsendelseStatus status) {
        oppdaterForsendelseMetadata(forsendelseId, arkivId, null, status);
    }

    public void oppdaterForsendelseMetadata(UUID forsendelseId, String arkivId, String saksnummer,
            ForsendelseStatus status) {
        DokumentMetadata metadata = hentEksaktDokumentMetadata(forsendelseId);
        metadata.setArkivId(arkivId);
        metadata.setSaksnummer(saksnummer);
        metadata.setStatus(status);
        lagre(metadata);
    }

    public boolean erLokalForsendelse(String eksternReferanseId) {
        try {
            UUID forsendelseId = UUID.fromString(eksternReferanseId);
            return hentUnikDokumentMetadata(forsendelseId).isPresent();
        } catch (Exception e) {
            return false;
        }
    }

    public void lagreJournalpostLokal(String journalpostId, String kanal, String tilstand, String referanse) {
        var journalpost = new Journalpost(journalpostId, tilstand, kanal, referanse, LOKALT_OPPHAV);
        entityManager.persist(journalpost);
        entityManager.flush();
    }

    public List<Journalpost> hentJournalposter(String journalpostId) {
        TypedQuery<Journalpost> query = entityManager.createQuery(
                "from Journalpost where journalpostId = :journalpostId", Journalpost.class)
                .setParameter("journalpostId", journalpostId);
        return query.getResultList();
    }
}
