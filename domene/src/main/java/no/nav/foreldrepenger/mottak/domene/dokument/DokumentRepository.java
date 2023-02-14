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

import no.nav.foreldrepenger.fordel.kodeverdi.ArkivFilType;
import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseStatus;
import no.nav.vedtak.exception.TekniskException;

@ApplicationScoped
public class DokumentRepository {

    private static final String LOKALT_OPPHAV = "FORDEL";
    private static final String FORSENDELSE_ID = "forsendelseId";
    private static final String HOVED_DOKUMENT = "hovedDokument";
    private static final String ARKIV_FILTYPE = "arkivFilType";
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

    public Optional<Dokument> hentUnikDokument(UUID forsendelseId, boolean hovedDokument, ArkivFilType arkivFilType) {
        var resultatListe = em.createQuery(
                "from Dokument where forsendelseId = :forsendelseId and hovedDokument = :hovedDokument and arkivFilType = :arkivFilType", Dokument.class)
            .setParameter(FORSENDELSE_ID, forsendelseId)
            .setParameter(HOVED_DOKUMENT, hovedDokument)
            .setParameter(ARKIV_FILTYPE, arkivFilType)
            .getResultList();
        if (resultatListe.size() > 1) {
            throw new TekniskException("FP-302156", "Spørringen returnerte mer enn eksakt ett resultat");
        }

        if (resultatListe.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(resultatListe.get(0));
    }

    public List<Dokument> hentDokumenter(UUID forsendelseId) {
        return em.createQuery("from Dokument where forsendelseId = :forsendelseId", Dokument.class)
            .setParameter(FORSENDELSE_ID, forsendelseId)
            .getResultList();
    }

    public DokumentMetadata hentEksaktDokumentMetadata(UUID forsendelseId) {
        return hentEksaktResultat(em.createQuery("from DokumentMetadata where forsendelseId = :forsendelseId", DokumentMetadata.class)
            .setParameter(FORSENDELSE_ID, forsendelseId));
    }

    public Optional<DokumentMetadata> hentUnikDokumentMetadata(UUID forsendelseId) {
        return hentUniktResultat(em.createQuery("from DokumentMetadata where forsendelseId = :forsendelseId", DokumentMetadata.class)
            .setParameter(FORSENDELSE_ID, forsendelseId));
    }

    public void slettForsendelse(UUID forsendelseId) {
        em.createNativeQuery("delete from DOKUMENT where FORSENDELSE_ID  = :forsendelseId")
            .setParameter(FORSENDELSE_ID, forsendelseId)
            .executeUpdate();

        em.createNativeQuery("delete from DOKUMENT_METADATA where FORSENDELSE_ID  = :forsendelseId")
            .setParameter(FORSENDELSE_ID, forsendelseId)
            .executeUpdate();

        em.flush();
    }

    public void oppdaterForsendelseMedArkivId(UUID forsendelseId, String arkivId, ForsendelseStatus status) {
        oppdaterForsendelseMetadata(forsendelseId, arkivId, null, status);
    }

    public void oppdaterForsendelseMetadata(UUID forsendelseId, String arkivId, String saksnummer, ForsendelseStatus status) {
        var metadata = hentEksaktDokumentMetadata(forsendelseId);
        metadata.setArkivId(arkivId);
        metadata.setSaksnummer(saksnummer);
        metadata.setStatus(status);
        lagre(metadata);
    }

    public boolean erLokalForsendelse(String eksternReferanseId) {
        try {
            var forsendelseId = UUID.fromString(eksternReferanseId);
            return hentUnikDokumentMetadata(forsendelseId).isPresent();
        } catch (Exception e) {
            return false;
        }
    }

    public void lagreJournalpostLokal(String journalpostId, String kanal, String tilstand, String referanse) {
        var journalpost = new Journalpost(journalpostId, tilstand, kanal, referanse, LOKALT_OPPHAV);
        em.persist(journalpost);
        em.flush();
    }

    public List<Journalpost> hentJournalposter(String journalpostId) {
        return em.createQuery("from Journalpost where journalpostId = :journalpostId", Journalpost.class)
            .setParameter("journalpostId", journalpostId)
            .getResultList();
    }
}
