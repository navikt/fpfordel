package no.nav.foreldrepenger.journalføring.oppgave.lager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import jakarta.persistence.EntityManager;
import jakarta.transaction.TransactionScoped;
import no.nav.foreldrepenger.mottak.extensions.JpaExtension;

@ExtendWith(JpaExtension.class)
class OppgaveRepositoryTest {

    private static final String JOURNALPOST_ID = "1234567";
    protected static final String ENHET = "1234";
    private OppgaveRepository repo;

    @BeforeEach
    void setUp(EntityManager em) {
        repo = new OppgaveRepository(em);
    }

    @Test
    void lagreOgHente() {
        var oppgaveEntitet = lagTestOppgave();
        assertThat(repo.hentOppgave(JOURNALPOST_ID)).isNotNull().isEqualTo(oppgaveEntitet);
    }

    @Test
    void lagreToOppgaverMedSammeId() {
        lagTestOppgave();
        assertThrows(Exception.class, this::lagTestOppgave);
    }

    @Test
    void harÅpenOppgave() {
        lagTestOppgave();
        assertTrue(repo.harÅpenOppgave(JOURNALPOST_ID));
        assertFalse(repo.harÅpenOppgave("ikke_finnes_"));
    }

    @Test
    void harFerdigstilltOppgave() {
        lagTestOppgave(Status.FERDIGSTILT);
        assertFalse(repo.harÅpenOppgave(JOURNALPOST_ID));
    }

    @Test
    void hentOppgave() {
        var oppgaveEntitet = lagTestOppgave();
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
        lagreOppgaver(2, "4321");
        var antall = 6;
        lagreOppgaver(antall, ENHET);
        var oppgaver = repo.hentÅpneOppgaverFor(ENHET);
        assertThat(oppgaver).isNotEmpty().hasSize(antall);
        assertThat(repo.hentAlleÅpneOppgaver()).isNotEmpty().hasSize(8).containsAll(oppgaver);
    }

    @Test
    void ferdigstillOppgave() {
        lagTestOppgave(Status.AAPNET);
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
                .medBrukerId(new AktørId("1234567890123"))
                .build();

        repo.lagre(oppgaveEntitet);
        return oppgaveEntitet;
    }

    private void lagTestOppgave(Status status) {
        lagTestOppgave(JOURNALPOST_ID, ENHET, status);
    }

    private OppgaveEntitet lagTestOppgave() {
        return lagTestOppgave(JOURNALPOST_ID, ENHET, Status.AAPNET);
    }
}
