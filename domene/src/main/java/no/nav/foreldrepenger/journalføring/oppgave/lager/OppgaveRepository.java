package no.nav.foreldrepenger.journalføring.oppgave.lager;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

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
        LOG.info("Oppgave med journalpostId: {} lagret.", oppgave.getJournalpostId());
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

    public List<OppgaveEntitet> hentOppgaverFlyttetTilGosys() {
        return em.createQuery("from Oppgave where status = :status",
                              OppgaveEntitet.class)
                .setParameter("status", Status.GOSYS)
                .getResultList();
    }

    public void flyttOppgaveTilGosys(String journalpostId) {
        var oppgave = hentOppgave(journalpostId);
        if (oppgave != null) {
            oppgave.setStatus(Status.GOSYS);
            lagre(oppgave);
            LOG.info("Oppgave med Id: {} flyttet til gosys.", oppgave.getJournalpostId());
        }
    }

    public void ferdigstillOppgave(String journalpostId) {
        var oppgave = hentOppgave(journalpostId);
        if (oppgave != null) {
            oppgave.setStatus(Status.FERDIGSTILT);
            lagre(oppgave);
            LOG.info("Oppgave med Id: {} ferdigstillt.", oppgave.getJournalpostId());
        }
    }

    public void feilregistrerOppgave(String journalpostId) {
        var oppgave = hentOppgave(journalpostId);
        if (oppgave != null) {
            oppgave.setStatus(Status.FEILREGISTRERT);
            lagre(oppgave);
            LOG.info("Oppgave med Id: {} feilregistrert.", oppgave.getJournalpostId());
        }
    }

    public void fjernFeilopprettetOppgave(String journalpostId) {
        em.createQuery("delete from Oppgave where journalpostId = :journalpostId").setParameter("journalpostId", journalpostId).executeUpdate();
        em.flush();
    }
}
