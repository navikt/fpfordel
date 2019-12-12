package no.nav.foreldrepenger.pip;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.fordel.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentMetadata;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentRepository;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentRepositoryImpl;

public class PipRepositoryTest {
    @Rule
    public final UnittestRepositoryRule repoRule = new UnittestRepositoryRule();

    private final PipRepository pipRepository = new PipRepository(repoRule.getEntityManager());
    private final DokumentRepository dokumentRepository = new DokumentRepositoryImpl(repoRule.getEntityManager());

    private String brukerId = "Dummy";
    private String brukerId2 = "Dummy 2";
    private UUID forsendelseId = UUID.randomUUID();
    private UUID forsendelseId2 = UUID.randomUUID();

    static {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Oslo"));
    }

    @Test
    public void en_aktørId_for_en_forsendelse() {
        dokumentRepository.lagre(dokumentMetadata(brukerId, forsendelseId));

        assertThat(pipRepository.hentAktørIdForForsendelser(Collections.singleton(forsendelseId)))
                .containsOnly(brukerId);
    }

    @Test
    public void en_aktørId_for_to_forsendelser_fra_samme_bruker() {
        dokumentRepository.lagre(dokumentMetadata(brukerId, forsendelseId));
        dokumentRepository.lagre(dokumentMetadata(brukerId, forsendelseId2));

        Set<UUID> dokumentforsendelseIder = new HashSet<>(Arrays.asList(forsendelseId, forsendelseId2));
        assertThat(pipRepository.hentAktørIdForForsendelser(dokumentforsendelseIder))
                .containsOnly(brukerId);
    }

    @Test
    public void to_aktørIder_for_to_forsendelser_fra_forskjellige_brukere() {
        dokumentRepository.lagre(dokumentMetadata(brukerId, forsendelseId));
        dokumentRepository.lagre(dokumentMetadata(brukerId2, forsendelseId2));

        Set<UUID> dokumentforsendelseIder = new HashSet<>(Arrays.asList(forsendelseId, forsendelseId2));
        assertThat(pipRepository.hentAktørIdForForsendelser(dokumentforsendelseIder))
                .containsOnly(brukerId, brukerId2);
    }

    private DokumentMetadata dokumentMetadata(String brukerId, UUID forsendelseId) {
        return DokumentMetadata.builder()
                .setBrukerId(brukerId)
                .setForsendelseId(forsendelseId)
                .setForsendelseMottatt(LocalDateTime.now())
                .build();
    }
}