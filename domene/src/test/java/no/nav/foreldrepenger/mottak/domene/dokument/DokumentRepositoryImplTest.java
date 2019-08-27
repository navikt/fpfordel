package no.nav.foreldrepenger.mottak.domene.dokument;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import no.nav.foreldrepenger.fordel.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.fordel.kodeverk.ArkivFilType;
import no.nav.foreldrepenger.fordel.kodeverk.DokumentTypeId;
import no.nav.foreldrepenger.mottak.journal.dokumentforsendelse.DokumentforsendelseTestUtil;
import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseStatus;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.testutilities.db.RepositoryRule;

public class DokumentRepositoryImplTest {

    private static final UUID FORSENDELSE_ID = UUID.randomUUID();
    private static final String ARKIV_ID = "1234";
    private static final String SAKSNUMMER = "9876";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    @Rule
    public RepositoryRule repoRule = new UnittestRepositoryRule();

    DokumentRepository repo = new DokumentRepositoryImpl(repoRule.getEntityManager());

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
    public void forsendelseId_skal_være_unik_på_dokumentMetadata() {
        expectedException.expect(TekniskException.class);
        expectedException.expectMessage("FP-324315");

        DokumentMetadata dokumentMetadata = dokumentMetadata(FORSENDELSE_ID);
        repo.lagre(dokumentMetadata);
        repo.lagre(dokumentMetadata(FORSENDELSE_ID));
    }

    @Test
    public void lagre_og_hente_dokument() {
        Dokument xmlSøknad = dokument(FORSENDELSE_ID, ArkivFilType.XML);
        repo.lagre(xmlSøknad);
        Dokument pdfSøknad = dokument(FORSENDELSE_ID, ArkivFilType.PDFA);
        repo.lagre(pdfSøknad);

        List<Dokument> dokuments = repo.hentDokumenter(FORSENDELSE_ID);
        assertThat(dokuments)
                .containsExactlyInAnyOrder(xmlSøknad, pdfSøknad);
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

        repo.oppdaterForseldelseMedArkivId(FORSENDELSE_ID, ARKIV_ID, ForsendelseStatus.FPSAK);
        DokumentMetadata endret = repo.hentEksaktDokumentMetadata(FORSENDELSE_ID);
        assertThat(endret.getArkivId()).hasValue(ARKIV_ID);
    }

    private DokumentMetadata dokumentMetadata(UUID forsendelseId) {
        return DokumentMetadata.builder()
                .setBrukerId("01234567890")
                .setForsendelseId(forsendelseId)
                .setForsendelseMottatt(LocalDateTime.now())
                .build();
    }

    private Dokument dokument(UUID forsendelseId, ArkivFilType arkivFilType) {
        return DokumentforsendelseTestUtil.lagDokument(forsendelseId, DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL, arkivFilType, true);
    }
}