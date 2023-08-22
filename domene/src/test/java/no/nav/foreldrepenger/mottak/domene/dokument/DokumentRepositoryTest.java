package no.nav.foreldrepenger.mottak.domene.dokument;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import no.nav.foreldrepenger.fordel.kodeverdi.ArkivFilType;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.mottak.extensions.JpaExtension;
import no.nav.foreldrepenger.mottak.journal.DokumentArkivTestUtil;
import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseStatus;

@ExtendWith(JpaExtension.class)
class DokumentRepositoryTest {

    private static final UUID FORSENDELSE_ID = UUID.randomUUID();
    private static final String ARKIV_ID = "1234";

    private DokumentRepository repo;

    private static DokumentMetadata dokumentMetadata(UUID forsendelseId) {
        return DokumentMetadata.builder()
            .setBrukerId("01234567890")
            .setForsendelseId(forsendelseId)
            .setForsendelseMottatt(LocalDateTime.now())
            .build();
    }

    private static Dokument dokument(UUID forsendelseId, ArkivFilType arkivFilType) {
        return DokumentArkivTestUtil.lagDokument(forsendelseId, DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL, arkivFilType, true);
    }

    private static Dokument dokumentAnnet(UUID forsendelseId, ArkivFilType arkivFilType) {
        return DokumentArkivTestUtil.lagDokumentBeskrivelse(forsendelseId, DokumentTypeId.ANNET, arkivFilType, true, "Farskap");
    }

    @BeforeEach
    void beforeAll(EntityManager em) {
        repo = new DokumentRepository(em);
    }

    @Test
    void lagre_og_hente_dokumentMetadata() {
        var dokumentMetadata = dokumentMetadata(FORSENDELSE_ID);
        repo.lagre(dokumentMetadata);
        assertThat(repo.hentUnikDokumentMetadata(FORSENDELSE_ID)).isPresent().hasValue(dokumentMetadata);
    }

    @Test
    void lagre_og_hente_dokument() {
        var xmlSøknad = dokument(FORSENDELSE_ID, ArkivFilType.XML);
        repo.lagre(xmlSøknad);
        var pdfSøknad = dokument(FORSENDELSE_ID, ArkivFilType.PDFA);
        repo.lagre(pdfSøknad);
        var vedlegg = dokumentAnnet(FORSENDELSE_ID, ArkivFilType.PDFA);
        repo.lagre(vedlegg);

        var dokuments = repo.hentDokumenter(FORSENDELSE_ID);
        assertThat(dokuments).containsExactlyInAnyOrder(xmlSøknad, pdfSøknad, vedlegg);
        assertThat(dokuments.get(2).getBeskrivelse()).isNotNull();
    }

    @Test
    void hent_unikt_dokument() {
        var xmlSøknad = dokument(FORSENDELSE_ID, ArkivFilType.XML);
        repo.lagre(xmlSøknad);
        var pdfSøknad = dokument(FORSENDELSE_ID, ArkivFilType.PDFA);
        repo.lagre(pdfSøknad);
        var dokument = repo.hentUnikDokument(FORSENDELSE_ID, true, ArkivFilType.PDFA);
        assertThat(dokument).isPresent();
        assertTrue(dokument.get().erHovedDokument());
        assertThat(dokument.get().getArkivFilType()).isEqualByComparingTo(ArkivFilType.PDFA);
    }

    @Test
    void hent_eksakt_dokument_metadata() {
        var metadata = dokumentMetadata(FORSENDELSE_ID);
        repo.lagre(metadata);
        assertThat(repo.hentEksaktDokumentMetadata(FORSENDELSE_ID)).isNotNull().isEqualTo(metadata);
    }

    @Test
    void lagre_og_slette_dokument_og_metadato() {
        repo.lagre(dokument(FORSENDELSE_ID, ArkivFilType.XML));
        repo.lagre(dokument(FORSENDELSE_ID, ArkivFilType.PDFA));
        var dokumentMetadata = dokumentMetadata(FORSENDELSE_ID);
        dokumentMetadata.setStatus(ForsendelseStatus.FPSAK);
        repo.lagre(dokumentMetadata);
        assertThat(repo.hentUnikDokumentMetadata(FORSENDELSE_ID)).isPresent().hasValue(dokumentMetadata);
        repo.slettForsendelse(FORSENDELSE_ID);
        assertThat(repo.hentUnikDokumentMetadata(FORSENDELSE_ID)).isNotPresent();
        assertThat(repo.hentDokumenter(FORSENDELSE_ID)).isEmpty();
    }

    @Test
    void oppdatere_forsendelse_med_arkivId() {
        repo.lagre(dokumentMetadata(FORSENDELSE_ID));
        assertThat(repo.hentEksaktDokumentMetadata(FORSENDELSE_ID).getArkivId()).isEmpty();
        repo.oppdaterForsendelseMedArkivId(FORSENDELSE_ID, ARKIV_ID, ForsendelseStatus.FPSAK);
        assertThat(repo.hentEksaktDokumentMetadata(FORSENDELSE_ID).getArkivId()).hasValue(ARKIV_ID);
    }

    @Test
    void oppdatere_dokument_type() {
        var pdfSøknad = DokumentArkivTestUtil.lagDokument(FORSENDELSE_ID, DokumentTypeId.SØKNAD_SVANGERSKAPSPENGER, ArkivFilType.PDFA, false);
        repo.lagre(pdfSøknad);
        repo.hentDokumenter(FORSENDELSE_ID)
            .stream()
            .filter(d -> DokumentTypeId.SØKNAD_SVANGERSKAPSPENGER.equals(d.getDokumentTypeId()))
            .forEach(d -> {
                d.setDokumentTypeId(DokumentTypeId.ETTERSENDT_SØKNAD_SVANGERSKAPSPENGER_SELVSTENDIG);
                repo.lagre(d);
            });
        var dokumenter = repo.hentDokumenter(FORSENDELSE_ID);
        assertThat(dokumenter).hasSize(1);
        assertFalse(dokumenter.get(0).erHovedDokument());
        assertThat(dokumenter.get(0).getDokumentTypeId()).isEqualByComparingTo(DokumentTypeId.ETTERSENDT_SØKNAD_SVANGERSKAPSPENGER_SELVSTENDIG);
    }
}
