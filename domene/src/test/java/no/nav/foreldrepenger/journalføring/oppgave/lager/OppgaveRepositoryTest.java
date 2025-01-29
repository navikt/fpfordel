package no.nav.foreldrepenger.journalføring.oppgave.lager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import jakarta.persistence.EntityManager;
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
    void lagreOgHenteUtenYtelseType() {
        var oppgaveEntitet = lagTestOppgave(JOURNALPOST_ID, Status.AAPNET, null);
        var actual = repo.hentOppgave(JOURNALPOST_ID);
        assertThat(actual).isNotNull().isEqualTo(oppgaveEntitet);
        assertNull(actual.getYtelseType());
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
        lagreOppgaver(antall, Status.AAPNET);
        var oppgaver = repo.hentAlleÅpneOppgaver();
        assertThat(oppgaver).isNotEmpty().hasSize(antall);
    }

    @Test
    void hentOppgaverFlyttetTilGosys() {
        var antall = 12;
        var forventedeOppgaver = lagreOppgaver(antall, Status.GOSYS);
        var forventedeJournalpostIder = forventedeOppgaver.stream()
                .map(OppgaveEntitet::getJournalpostId)
                .toList();

        var oppgaver = repo.hentOppgaverFlyttetTilGosys(forventedeJournalpostIder);

        assertThat(oppgaver)
            .isNotEmpty()
            .hasSize(antall);
    }

    @Test
    void flyttOppgaveTilGosys() {
        lagTestOppgave(Status.AAPNET);
        var lagret = repo.hentOppgave(JOURNALPOST_ID);
        assertThat(lagret.getStatus()).isEqualTo(Status.AAPNET);

        repo.flyttOppgaveTilGosys(JOURNALPOST_ID);

        var resultat = repo.hentOppgave(JOURNALPOST_ID);
        assertThat(resultat).isNotNull();
        assertThat(resultat.getStatus()).isEqualTo(Status.GOSYS);
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

    private List<OppgaveEntitet> lagreOppgaver(int antall, Status status) {
        List<OppgaveEntitet> oppgaver = new ArrayList<>();
        var randomId = new Random().nextInt(10000);
        for (int i = 0; i < antall; i++) {
            var oppgave = lagTestOppgave(String.valueOf(randomId + i), status, YtelseType.FP);
            repo.lagre(oppgave);
            oppgaver.add(oppgave);
        }
        return oppgaver;
    }

    private OppgaveEntitet lagTestOppgave(String journalpostId, Status status, YtelseType ytelseType) {
        var oppgaveEntitet = OppgaveEntitet.builder()
                .medJournalpostId(journalpostId)
                .medStatus(status)
                .medYtelseType(ytelseType)
                .medFrist(LocalDate.now())
                .medEnhet(ENHET)
                .medBrukerId(new AktørId("1234567890123"))
                .build();

        repo.lagre(oppgaveEntitet);
        return oppgaveEntitet;
    }

    private void lagTestOppgave(Status status) {
        lagTestOppgave(JOURNALPOST_ID, status, YtelseType.FP);
    }

    private OppgaveEntitet lagTestOppgave() {
        return lagTestOppgave(JOURNALPOST_ID, Status.AAPNET, YtelseType.FP);
    }
}
