package no.nav.foreldrepenger.pip;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import no.nav.foreldrepenger.journalføring.domene.JournalpostId;
import no.nav.foreldrepenger.journalføring.oppgave.lager.OppgaveEntitet;
import no.nav.foreldrepenger.journalføring.oppgave.lager.OppgaveRepository;
import no.nav.foreldrepenger.journalføring.oppgave.lager.Status;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentMetadata;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentRepository;
import no.nav.foreldrepenger.mottak.extensions.JpaExtension;

@ExtendWith(JpaExtension.class)
class PipRepositoryTest {

    private PipRepository pipRepository;
    private DokumentRepository dokumentRepository;
    private OppgaveRepository oppgaveRepository;

    private final String brukerId = "1234567890123";
    private final UUID forsendelseId = UUID.randomUUID();
    private final UUID forsendelseId2 = UUID.randomUUID();

    private static DokumentMetadata dokumentMetadata(String brukerId, UUID forsendelseId) {
        return DokumentMetadata.builder().setBrukerId(brukerId).setForsendelseId(forsendelseId).setForsendelseMottatt(LocalDateTime.now()).build();
    }

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
    public void before(EntityManager em) {
        pipRepository = new PipRepository(em);
        dokumentRepository = new DokumentRepository(em);
        oppgaveRepository = new OppgaveRepository(em);
    }

    @Test
    void en_aktørId_for_en_forsendelse() {
        dokumentRepository.lagre(dokumentMetadata(brukerId, forsendelseId));
        assertThat(pipRepository.hentAktørIdForForsendelser(Set.of(forsendelseId))).containsOnly(brukerId);
    }

    @Test
    void en_aktørId_for_to_forsendelser_fra_samme_bruker() {
        dokumentRepository.lagre(dokumentMetadata(brukerId, forsendelseId));
        dokumentRepository.lagre(dokumentMetadata(brukerId, forsendelseId2));

        Set<UUID> dokumentforsendelseIder = Set.of(forsendelseId, forsendelseId2);
        assertThat(pipRepository.hentAktørIdForForsendelser(dokumentforsendelseIder)).containsOnly(brukerId);
    }

    @Test
    void to_aktørIder_for_to_forsendelser_fra_forskjellige_brukere() {
        dokumentRepository.lagre(dokumentMetadata(brukerId, forsendelseId));
        String brukerId2 = "Dummy 2";
        dokumentRepository.lagre(dokumentMetadata(brukerId2, forsendelseId2));

        Set<UUID> dokumentforsendelseIder = Set.of(forsendelseId, forsendelseId2);
        assertThat(pipRepository.hentAktørIdForForsendelser(dokumentforsendelseIder)).containsOnly(brukerId, brukerId2);
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
