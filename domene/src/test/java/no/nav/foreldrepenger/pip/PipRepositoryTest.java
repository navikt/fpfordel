package no.nav.foreldrepenger.pip;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import jakarta.persistence.EntityManager;
import no.nav.foreldrepenger.journalføring.domene.JournalpostId;
import no.nav.foreldrepenger.journalføring.oppgave.lager.OppgaveEntitet;
import no.nav.foreldrepenger.journalføring.oppgave.lager.OppgaveRepository;
import no.nav.foreldrepenger.journalføring.oppgave.lager.Status;
import no.nav.foreldrepenger.mottak.extensions.JpaExtension;

@ExtendWith(JpaExtension.class)
class PipRepositoryTest {

    private PipRepository pipRepository;
    private OppgaveRepository oppgaveRepository;

    private final String brukerId = "1234567890123";

    private static OppgaveEntitet oppgave(String brukerId, String journalpostId) {
        return OppgaveEntitet.builder()
            .medBrukerId(brukerId)
            .medEnhet("0000")
            .medJournalpostId(journalpostId)
            .medStatus(Status.AAPNET)
            .medFrist(LocalDate.now())
            .build();
    }

    @BeforeEach
    void before(EntityManager em) {
        pipRepository = new PipRepository(em);
        oppgaveRepository = new OppgaveRepository(em);
    }


    @Test
    void en_aktørIder_for_en_oppgave() {
        oppgaveRepository.lagre(oppgave(brukerId, "1234"));
        assertThat(pipRepository.hentAktørIdForOppgave(Set.of(JournalpostId.fra("1234")))).contains(brukerId);
    }

    @Test
    void en_aktørIder_for_en_oppgave_uten_aktør() {
        oppgaveRepository.lagre(oppgave(null, "4321"));
        assertThat(pipRepository.hentAktørIdForOppgave(Set.of(JournalpostId.fra("4321")))).isEmpty();
    }
}
