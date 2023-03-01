package no.nav.foreldrepenger.pip;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import javax.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import no.nav.foreldrepenger.mottak.domene.dokument.DokumentMetadata;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentRepository;
import no.nav.foreldrepenger.mottak.extensions.JpaExtension;

@ExtendWith(JpaExtension.class)
class PipRepositoryTest {

    private PipRepository pipRepository;
    private DokumentRepository dokumentRepository;

    private String brukerId = "Dummy";
    private String brukerId2 = "Dummy 2";
    private UUID forsendelseId = UUID.randomUUID();
    private UUID forsendelseId2 = UUID.randomUUID();

    private static DokumentMetadata dokumentMetadata(String brukerId, UUID forsendelseId) {
        return DokumentMetadata.builder().setBrukerId(brukerId).setForsendelseId(forsendelseId).setForsendelseMottatt(LocalDateTime.now()).build();
    }

    @BeforeEach
    public void before(EntityManager em) {
        pipRepository = new PipRepository(em);
        dokumentRepository = new DokumentRepository(em);
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
        dokumentRepository.lagre(dokumentMetadata(brukerId2, forsendelseId2));

        Set<UUID> dokumentforsendelseIder = Set.of(forsendelseId, forsendelseId2);
        assertThat(pipRepository.hentAktørIdForForsendelser(dokumentforsendelseIder)).containsOnly(brukerId, brukerId2);
    }
}
