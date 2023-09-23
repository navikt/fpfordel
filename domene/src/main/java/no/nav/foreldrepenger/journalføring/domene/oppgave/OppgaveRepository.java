package no.nav.foreldrepenger.journalføring.domene.oppgave;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@ApplicationScoped
public class OppgaveRepository {
    private static final Logger LOG = LoggerFactory.getLogger(OppgaveRepository.class);

    private EntityManager em;

    OppgaveRepository() {
    }

    @Inject
    public OppgaveRepository(EntityManager entityManager) {
        this.em = Objects.requireNonNull(entityManager);
    }

    private void commit(Object entity) {
        em.persist(entity);
        em.flush();
    }

    public String lagre(OppgaveEntitet oppgave) {
        commit(oppgave);
        LOG.info("Oppgaver med id: {} lagret.", oppgave.getJournalpostId());
        return oppgave.getJournalpostId();
    }

    public boolean harÅpenOppgave(String journalpostId) {
        var oppgave = hentOppgave(journalpostId);
        return oppgave != null && oppgave.getStatus().equals(Status.AAPNET);
    }

    public OppgaveEntitet hentOppgave(String journalpostId) {
        return em.find(OppgaveEntitet.class, journalpostId);
    }

    public List<OppgaveEntitet> hentAlleÅpneOppgaver() {
        return em.createQuery("from Oppgave where status = :status", OppgaveEntitet.class)
            .setParameter("status", Status.AAPNET)
            .getResultList();
    }

    public List<OppgaveEntitet> finnÅpneOppgaverFor(String enhet) {
        return em.createQuery("from Oppgave where enhet = :enhet and status = :status", OppgaveEntitet.class)
            .setParameter("enhet", enhet)
            .setParameter("status", Status.AAPNET)
            .getResultList();
    }

    public void ferdigstillOppgave(String journalpostId) {
        var oppgave = hentOppgave(journalpostId);
        if (oppgave != null) {
            oppgave.setStatus(Status.FERDIGSTILT);
            lagre(oppgave);
            LOG.info("Oppgave med Id: {} ferdigstillt.", oppgave.getJournalpostId());
        }
    }
}
