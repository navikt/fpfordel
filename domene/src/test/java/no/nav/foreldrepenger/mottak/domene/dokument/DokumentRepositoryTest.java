package no.nav.foreldrepenger.mottak.domene.dokument;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import no.nav.foreldrepenger.fordel.kodeverdi.ArkivFilType;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.mottak.extensions.EntityManagerAwareTest;
import no.nav.foreldrepenger.mottak.extensions.EntityManagerFPFordelAwareExtension;
import no.nav.foreldrepenger.mottak.journal.DokumentArkivTestUtil;
import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseStatus;

@ExtendWith(EntityManagerFPFordelAwareExtension.class)
public class DokumentRepositoryTest extends EntityManagerAwareTest {

    private static final UUID FORSENDELSE_ID = UUID.randomUUID();
    private static final String ARKIV_ID = "1234";

    private DokumentRepository repo;
    static {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Oslo"));
    }

    @BeforeEach
    public void beforeAll() {
        repo = new DokumentRepository(getEntityManager());
    }

    @Test
    public void lagre_og_hente_dokumentMetadata() {
        DokumentMetadata dokumentMetadata = dokumentMetadata(FORSENDELSE_ID);
        repo.lagre(dokumentMetadata);

        Optional<DokumentMetadata> fraRepo = repo.hentUnikDokumentMetadata(FORSENDELSE_ID);
        assertThat(fraRepo)
                .isPresent()
                .hasValue(dokumentMetadata);
    }

    @Test
    public void lagre_og_hente_dokument() {
        Dokument xmlSøknad = dokument(FORSENDELSE_ID, ArkivFilType.XML);
        repo.lagre(xmlSøknad);
        Dokument pdfSøknad = dokument(FORSENDELSE_ID, ArkivFilType.PDFA);
        repo.lagre(pdfSøknad);
        Dokument vedlegg = dokumentAnnet(FORSENDELSE_ID, ArkivFilType.PDFA);
        repo.lagre(vedlegg);

        List<Dokument> dokuments = repo.hentDokumenter(FORSENDELSE_ID);
        assertThat(dokuments)
                .containsExactlyInAnyOrder(xmlSøknad, pdfSøknad, vedlegg);
        assertThat(dokuments.get(2).getBeskrivelse()).isNotNull();
    }

    @Test
    public void hent_unikt_dokument() {
        Dokument xmlSøknad = dokument(FORSENDELSE_ID, ArkivFilType.XML);
        repo.lagre(xmlSøknad);
        Dokument pdfSøknad = dokument(FORSENDELSE_ID, ArkivFilType.PDFA);
        repo.lagre(pdfSøknad);

        Optional<Dokument> dokument = repo.hentUnikDokument(FORSENDELSE_ID, true, ArkivFilType.PDFA);
        assertThat(dokument).isPresent();
        assertThat(dokument.get().erHovedDokument()).isTrue();
        assertThat(dokument.get().getArkivFilType()).isEqualByComparingTo(ArkivFilType.PDFA);
    }

    @Test
    public void hent_eksakt_dokument_metadata() {
        DokumentMetadata metadata = dokumentMetadata(FORSENDELSE_ID);
        repo.lagre(metadata);

        DokumentMetadata resultat = repo.hentEksaktDokumentMetadata(FORSENDELSE_ID);
        assertThat(resultat).isNotNull();
        assertThat(resultat).isEqualTo(metadata);
    }

    @Test
    public void lagre_og_slette_dokument_og_metadato() {
        Dokument xmlSøknad = dokument(FORSENDELSE_ID, ArkivFilType.XML);
        repo.lagre(xmlSøknad);
        Dokument pdfSøknad = dokument(FORSENDELSE_ID, ArkivFilType.PDFA);
        repo.lagre(pdfSøknad);
        DokumentMetadata dokumentMetadata = dokumentMetadata(FORSENDELSE_ID);
        dokumentMetadata.setStatus(ForsendelseStatus.FPSAK);
        repo.lagre(dokumentMetadata);

        Optional<DokumentMetadata> fraRepo = repo.hentUnikDokumentMetadata(FORSENDELSE_ID);
        assertThat(fraRepo).isPresent().hasValue(dokumentMetadata);

        repo.slettForsendelse(FORSENDELSE_ID);

        fraRepo = repo.hentUnikDokumentMetadata(FORSENDELSE_ID);
        assertThat(fraRepo).isNotPresent();

        List<Dokument> dokuments = repo.hentDokumenter(FORSENDELSE_ID);
        assertThat(dokuments).isEmpty();
    }

    @Test
    public void oppdatere_forsendelse_med_arkivId() {
        DokumentMetadata inn = dokumentMetadata(FORSENDELSE_ID);
        repo.lagre(inn);

        DokumentMetadata uendret = repo.hentEksaktDokumentMetadata(FORSENDELSE_ID);
        assertThat(uendret.getArkivId()).isEmpty();

        repo.oppdaterForsendelseMedArkivId(FORSENDELSE_ID, ARKIV_ID, ForsendelseStatus.FPSAK);
        DokumentMetadata endret = repo.hentEksaktDokumentMetadata(FORSENDELSE_ID);
        assertThat(endret.getArkivId()).hasValue(ARKIV_ID);
    }

    private static DokumentMetadata dokumentMetadata(UUID forsendelseId) {
        return DokumentMetadata.builder()
                .setBrukerId("01234567890")
                .setForsendelseId(forsendelseId)
                .setForsendelseMottatt(LocalDateTime.now())
                .build();
    }

    private static Dokument dokument(UUID forsendelseId, ArkivFilType arkivFilType) {
        return DokumentArkivTestUtil.lagDokument(forsendelseId, DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL,
                arkivFilType, true);
    }

    private static Dokument dokumentAnnet(UUID forsendelseId, ArkivFilType arkivFilType) {
        return DokumentArkivTestUtil.lagDokumentBeskrivelse(forsendelseId, DokumentTypeId.ANNET, arkivFilType,
                true, "Farskap");
    }
}