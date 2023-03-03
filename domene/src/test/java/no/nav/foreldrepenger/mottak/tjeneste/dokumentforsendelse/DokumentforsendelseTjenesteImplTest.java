package no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse;

import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.foreldrepenger.fordel.kodeverdi.ArkivFilType;
import no.nav.foreldrepenger.mottak.domene.dokument.Dokument;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentMetadata;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentRepository;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.journal.DokumentArkivTestUtil;
import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseIdDto;
import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseStatus;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;

@ExtendWith(MockitoExtension.class)
class DokumentforsendelseTjenesteImplTest {

    private static final String BRUKER_ID = "1234L";

    private static final UUID ID = UUID.randomUUID();

    static {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Oslo"));
    }

    @Mock
    private DokumentRepository dokumentRepositoryMock;
    @Mock
    private ProsessTaskTjeneste prosessTaskTjenesteMock;

    private DokumentforsendelseTjenesteImpl tjeneste;

    private static Dokument createDokument(ArkivFilType arkivFilType, boolean hovedDokument) {
        return DokumentArkivTestUtil.lagDokument(ID, SØKNAD_FORELDREPENGER_FØDSEL, arkivFilType, hovedDokument);
    }

    private static DokumentMetadata byggDokumentMetadata(String arkivId, String saksnummer, ForsendelseStatus status) {
        return new DokumentMetadata.Builder().setArkivId(arkivId)
            .setSaksnummer(saksnummer)
            .setBrukerId("avsender")
            .setStatus(status)
            .setForsendelseId(UUID.randomUUID())
            .setForsendelseMottatt(LocalDateTime.now())
            .build();
    }

    private static ForsendelseIdDto lagForsendelseIdDto() {
        return new ForsendelseIdDto(ID.toString());
    }

    @BeforeEach
    void setUp() {
        tjeneste = new DokumentforsendelseTjenesteImpl(dokumentRepositoryMock, prosessTaskTjenesteMock);
    }

    @Test
    void validerDokumentforsendelse__ettersending_uten_saksnummer_er_feil() {
        UUID forsendelseId = UUID.randomUUID();
        var metadata = DokumentMetadata.builder()
            .setForsendelseId(forsendelseId)
            .setBrukerId(BRUKER_ID)
            .setForsendelseMottatt(LocalDateTime.now())
            .build();
        var dokumentListe = List.of(createDokument(ArkivFilType.PDFA, false));
        var e = assertThrows(TekniskException.class, () -> tjeneste.lagreForsendelseValider(metadata, dokumentListe));
        assertTrue(e.getMessage().contains("FP-728553"));
    }

    @Test
    void validerDokumentforsendelse__ettersending_med_saksnummer_er_OK() {
        UUID forsendelseId = UUID.randomUUID();
        var metadata = DokumentMetadata.builder()
            .setForsendelseId(forsendelseId)
            .setBrukerId(BRUKER_ID)
            .setSaksnummer("123")
            .setForsendelseMottatt(LocalDateTime.now())
            .build();
        var dokumentListe = List.of(createDokument(ArkivFilType.PDFA, false));

        tjeneste.lagreForsendelseValider(metadata, dokumentListe);

        verify(prosessTaskTjenesteMock).lagre(any(ProsessTaskData.class));
    }

    @Test
    void validerDokumentforsendelse__søknad_er_OK() {
        UUID forsendelseId = UUID.randomUUID();
        var metadata = DokumentMetadata.builder()
            .setForsendelseId(forsendelseId)
            .setBrukerId(BRUKER_ID)
            .setSaksnummer("123")
            .setForsendelseMottatt(LocalDateTime.now())
            .build();
        var dokumentListe = List.of(createDokument(ArkivFilType.PDFA, true), createDokument(ArkivFilType.XML, true));


        tjeneste.lagreForsendelseValider(metadata, dokumentListe);

        ArgumentCaptor<ProsessTaskData> prosessTaskCaptor = ArgumentCaptor.forClass(ProsessTaskData.class);
        verify(prosessTaskTjenesteMock).lagre(prosessTaskCaptor.capture());
        var capturedProssessTaskData = prosessTaskCaptor.getValue();
        assertNotNull(capturedProssessTaskData);
        assertThat(capturedProssessTaskData.getPropertyValue(MottakMeldingDataWrapper.FORSENDELSE_ID_KEY)).isEqualTo(forsendelseId.toString());
    }

    @Test
    void korrektAntallOgTyper__ett_hoveddokument_er_feil() {
        assertFalse(tjeneste.korrektAntallOgTyper(Set.of(createDokument(ArkivFilType.XML, true))));
    }

    @Test
    void korrektAntallOgTyper__tre_hoveddokument_er_feil() {
        assertFalse(tjeneste.korrektAntallOgTyper(
            Set.of(createDokument(ArkivFilType.XML, true), createDokument(ArkivFilType.PDFA, true), createDokument(ArkivFilType.XML, true))));
    }

    @Test
    void korrektAntallOgTyper__to_xml_hoveddokument_er_feil() {
        assertFalse(tjeneste.korrektAntallOgTyper(Set.of(createDokument(ArkivFilType.XML, true), createDokument(ArkivFilType.XML, true))));
    }

    @Test
    void korrektAntallOgTyper__to_pdf_hoveddokument_er_feil() {
        assertFalse(tjeneste.korrektAntallOgTyper(Set.of(createDokument(ArkivFilType.PDFA, true), createDokument(ArkivFilType.PDFA, true))));
    }

    @Test
    void korrektAntallOgTyper__ett_xml_og_ett_jpeg_hoveddokument_er_feil() {
        assertFalse(tjeneste.korrektAntallOgTyper(Set.of(createDokument(ArkivFilType.XML, true), createDokument(ArkivFilType.JPEG, true))));
    }

    @Test
    void korrektAntallOgTyper__ett_xml_og_ett_pdf_hoveddokument_er_OK() {
        assertTrue(tjeneste.korrektAntallOgTyper(Set.of(createDokument(ArkivFilType.XML, true), createDokument(ArkivFilType.PDFA, true))));
    }

    @Test
    void dokumentforsendelse_status_pending() {
        ForsendelseIdDto forsendelseIdDto = lagForsendelseIdDto();
        when(dokumentRepositoryMock.hentUnikDokumentMetadata(any(UUID.class))).thenReturn(
            Optional.of(byggDokumentMetadata(null, null, ForsendelseStatus.PENDING)));

        assertThat(tjeneste.finnStatusinformasjon(forsendelseIdDto.forsendelseId()).getForsendelseStatus()).isEqualByComparingTo(
            ForsendelseStatus.PENDING);
    }

    @Test
    void dokumentforsendelse_status_gosys() {
        ForsendelseIdDto forsendelseIdDto = lagForsendelseIdDto();
        when(dokumentRepositoryMock.hentUnikDokumentMetadata(any(UUID.class))).thenReturn(
            Optional.of(byggDokumentMetadata("1", null, ForsendelseStatus.GOSYS)));

        assertThat(tjeneste.finnStatusinformasjon(forsendelseIdDto.forsendelseId()).getForsendelseStatus()).isEqualByComparingTo(
            ForsendelseStatus.GOSYS);
    }

    @Test
    void dokumentforsendelse_status_fpsak() {
        ForsendelseIdDto forsendelseIdDto = lagForsendelseIdDto();
        when(dokumentRepositoryMock.hentUnikDokumentMetadata(any(UUID.class))).thenReturn(
            Optional.of(byggDokumentMetadata("1", "1", ForsendelseStatus.FPSAK)));

        assertThat(tjeneste.finnStatusinformasjon(forsendelseIdDto.forsendelseId()).getForsendelseStatus()).isEqualByComparingTo(
            ForsendelseStatus.FPSAK);
    }

    @Test
    void dokumentforsendelse_med_uuid_ikke_funnet() {
        ForsendelseIdDto forsendelseIdDto = lagForsendelseIdDto();
        when(dokumentRepositoryMock.hentUnikDokumentMetadata(any(UUID.class))).thenReturn(Optional.empty());
        var forsendelseId = forsendelseIdDto.forsendelseId();
        var e = assertThrows(TekniskException.class, () -> tjeneste.finnStatusinformasjon(forsendelseId));
        assertTrue(e.getMessage().contains("FP-295614"));
    }

    @Test
    void dokumentforsendelse_med_ugyldig_uuid() {
        var e = assertThrows(IllegalArgumentException.class, () -> new ForsendelseIdDto("123"));
        assertTrue(e.getMessage().contains("Invalid UUID"));
    }
}
