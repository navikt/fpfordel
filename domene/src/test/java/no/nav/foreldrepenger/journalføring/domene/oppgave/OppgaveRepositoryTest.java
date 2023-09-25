package no.nav.foreldrepenger.journalføring.domene.oppgave;

import jakarta.persistence.EntityManager;
import jakarta.persistence.FlushModeType;
import jakarta.transaction.TransactionScoped;
import no.nav.foreldrepenger.domene.BrukerId;
import no.nav.foreldrepenger.domene.YtelseType;
import no.nav.foreldrepenger.mottak.extensions.JpaExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalDate;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(JpaExtension.class)
class OppgaveRepositoryTest {

    private static final String JOURNALPOST_ID = "1234567";
    protected static final String ENHET = "1234";
    private OppgaveRepository repo;

    @BeforeEach
    void setUp(EntityManager em) {
        // Støtte for flush i testene - siden vi ikke vil kalle på flush eksplisit i repo.
        em.setFlushMode(FlushModeType.AUTO);
        repo = new OppgaveRepository(em);
    }

    @Test
    void lagreOgHente() {
        var oppgaveEntitet = lagTestOppgave(JOURNALPOST_ID);
        assertThat(repo.hentOppgave(JOURNALPOST_ID)).isNotNull().isEqualTo(oppgaveEntitet);
    }

    @Test
    void lagreToOppgaverMedSammeId() {
        lagTestOppgave(JOURNALPOST_ID);
        assertThrows(Exception.class, () -> lagTestOppgave(JOURNALPOST_ID));
    }

    @Test
    void harÅpenOppgave() {
        lagTestOppgave(JOURNALPOST_ID);
        assertTrue(repo.harÅpenOppgave(JOURNALPOST_ID));
        assertFalse(repo.harÅpenOppgave("ikke_finnes_"));
    }

    @Test
    void harFerdigstilltOppgave() {
        lagTestOppgave(JOURNALPOST_ID, Status.FERDIGSTILT);
        assertFalse(repo.harÅpenOppgave(JOURNALPOST_ID));
    }

    @Test
    void hentOppgave() {
        var oppgaveEntitet = lagTestOppgave(JOURNALPOST_ID);
        var result = repo.hentOppgave(JOURNALPOST_ID);
        assertThat(result).isNotNull().isEqualTo(oppgaveEntitet);
        assertThat(result.getJournalpostId()).isEqualTo(JOURNALPOST_ID);
        assertThat(result.getStatus()).isEqualTo(Status.AAPNET);
        assertThat(result.getYtelseType()).isEqualTo(YtelseType.FP);
    }

    @Test
    void hentAlleÅpneOppgaver() {
        var antall = 12;
        lagreOppgaver(antall, ENHET);
        var oppgaver = repo.hentAlleÅpneOppgaver();
        assertThat(oppgaver).isNotEmpty().hasSize(antall);
    }

    @Test
    @TransactionScoped
    void hentÅpneOppgaverFor() {
        var antall = 6;
        lagreOppgaver(2, "4321");
        lagreOppgaver(antall, ENHET);
        var oppgaver = repo.hentÅpneOppgaverFor(ENHET);
        assertThat(oppgaver).isNotEmpty().hasSize(antall);
        assertThat(repo.hentAlleÅpneOppgaver()).isNotEmpty().hasSize(8).containsAll(oppgaver);
    }

    @Test
    void ferdigstillOppgave() {
        lagTestOppgave(JOURNALPOST_ID, Status.AAPNET);
        var lagret = repo.hentOppgave(JOURNALPOST_ID);
        assertThat(lagret.getStatus()).isEqualTo(Status.AAPNET);

        repo.ferdigstillOppgave(JOURNALPOST_ID);

        var resultat = repo.hentOppgave(JOURNALPOST_ID);
        assertThat(resultat).isNotNull();
        assertThat(resultat.getStatus()).isEqualTo(Status.FERDIGSTILT);
    }

    private void lagreOppgaver(int antall, String enhet) {
        var randomId = new Random().nextInt(10000);
        for (int i = 0; i < antall; i++) {
            repo.lagre(lagTestOppgave(String.valueOf(randomId + i), enhet, Status.AAPNET));
        }
    }

    private OppgaveEntitet lagTestOppgave(String journalpostId, String enhet, Status status) {
        var oppgaveEntitet = OppgaveEntitet.builder()
                .medJournalpostId(journalpostId)
                .medStatus(status)
                .medYtelseType(YtelseType.FP)
                .medFrist(LocalDate.now())
                .medEnhet(enhet)
                .medBrukerId(new BrukerId("1234567890123"))
                .build();
        repo.lagre(oppgaveEntitet);
        return oppgaveEntitet;
    }

    private OppgaveEntitet lagTestOppgave(String journalpostId, Status status) {
        return lagTestOppgave(journalpostId, ENHET, status);
    }

    private OppgaveEntitet lagTestOppgave(String journalpostId) {
        return lagTestOppgave(journalpostId, ENHET, Status.AAPNET);
    }
}
