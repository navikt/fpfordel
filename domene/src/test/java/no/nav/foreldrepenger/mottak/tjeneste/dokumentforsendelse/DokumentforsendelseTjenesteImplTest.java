package no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse;

import static java.util.Arrays.asList;
import static no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
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
import no.nav.foreldrepenger.mottak.person.AktørTjeneste;
import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseIdDto;
import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseStatus;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;
import no.nav.vedtak.felles.testutilities.sikkerhet.StaticSubjectHandler;
import no.nav.vedtak.felles.testutilities.sikkerhet.SubjectHandlerUtils;
import no.nav.vedtak.sikkerhet.context.SubjectHandler;

@ExtendWith(MockitoExtension.class)
public class DokumentforsendelseTjenesteImplTest {

    private static final String BRUKER_ID = "1234L";

    private static final UUID ID = UUID.randomUUID();

    static {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Oslo"));
    }

    @Mock
    private DokumentRepository dokumentRepositoryMock;
    @Mock
    private ProsessTaskRepository prosessTaskRepositoryMock;
    @Mock
    private AktørTjeneste aktørConsumerMock;

    private DokumentforsendelseTjenesteImpl tjeneste;

    private static Class<? extends SubjectHandler> orgSubjectHandler;

    @BeforeAll
    public static void setupClass() {
        String subjectHandlerImplementationClassName = System
                .getProperty("no.nav.modig.core.context.subjectHandlerImplementationClass");
        if (subjectHandlerImplementationClassName != null) {
            orgSubjectHandler = SubjectHandler.getSubjectHandler().getClass();
        }
    }

    @BeforeEach
    public void setUp() {
        tjeneste = new DokumentforsendelseTjenesteImpl(dokumentRepositoryMock,
                prosessTaskRepositoryMock, aktørConsumerMock);

        SubjectHandlerUtils.useSubjectHandler(StaticSubjectHandler.class);
    }

    @AfterAll
    public static void teardown() {
        if (orgSubjectHandler == null) {
            SubjectHandlerUtils.unsetSubjectHandler();
        } else {
            SubjectHandlerUtils.useSubjectHandler(orgSubjectHandler);
        }
    }

    @Test
    public void validerDokumentforsendelse__ettersending_uten_saksnummer_er_feil() {
        UUID forsendelseId = UUID.randomUUID();
        DokumentMetadata metadata = DokumentMetadata.builder()
                .setForsendelseId(forsendelseId)
                .setBrukerId(BRUKER_ID)
                .setForsendelseMottatt(LocalDateTime.now())
                .build();
        when(dokumentRepositoryMock.hentEksaktDokumentMetadata(any())).thenReturn(metadata);
        when(dokumentRepositoryMock.hentDokumenter(any())).thenReturn(asList(createDokument(ArkivFilType.PDFA, false)));
        var e = assertThrows(TekniskException.class, () -> tjeneste.validerDokumentforsendelse(forsendelseId));
        assertTrue(e.getMessage().contains("FP-728553"));
    }

    @Test
    public void validerDokumentforsendelse__ettersending_med_saksnummer_er_OK() {
        UUID forsendelseId = UUID.randomUUID();
        DokumentMetadata metadata = DokumentMetadata.builder()
                .setForsendelseId(forsendelseId)
                .setBrukerId(BRUKER_ID)
                .setSaksnummer("123")
                .setForsendelseMottatt(LocalDateTime.now())
                .build();
        when(dokumentRepositoryMock.hentEksaktDokumentMetadata(any())).thenReturn(metadata);
        when(dokumentRepositoryMock.hentDokumenter(any())).thenReturn(asList(createDokument(ArkivFilType.PDFA, false)));

        tjeneste.validerDokumentforsendelse(forsendelseId);

        verify(prosessTaskRepositoryMock).lagre(any(ProsessTaskData.class));
    }

    @Test
    public void validerDokumentforsendelse__søknad_er_OK() {
        UUID forsendelseId = UUID.randomUUID();
        DokumentMetadata metadata = DokumentMetadata.builder()
                .setForsendelseId(forsendelseId)
                .setBrukerId(BRUKER_ID)
                .setSaksnummer("123")
                .setForsendelseMottatt(LocalDateTime.now())
                .build();
        when(dokumentRepositoryMock.hentEksaktDokumentMetadata(any())).thenReturn(metadata);
        when(dokumentRepositoryMock.hentDokumenter(any()))
                .thenReturn(asList(createDokument(ArkivFilType.PDFA, true), createDokument(ArkivFilType.XML, true)));

        tjeneste.validerDokumentforsendelse(forsendelseId);

        ArgumentCaptor<ProsessTaskData> prosessTaskCaptor = ArgumentCaptor.forClass(ProsessTaskData.class);
        verify(prosessTaskRepositoryMock).lagre(prosessTaskCaptor.capture());
        ProsessTaskData capturedProssessTaskData = prosessTaskCaptor.getValue();
        assertThat(capturedProssessTaskData).isNotNull();
        assertThat(capturedProssessTaskData.getPropertyValue(MottakMeldingDataWrapper.FORSENDELSE_ID_KEY))
                .isEqualTo(forsendelseId.toString());
        assertThat(capturedProssessTaskData.getPropertyValue(MottakMeldingDataWrapper.AVSENDER_ID_KEY)).isNull();
    }

    @Test
    public void validerDokumentforsendelse__skal_sette_avsender_id_fra_subjecthandler_når_forskjellig_fra_bruker_id() {
        String aktørIdForIdent = "123";
        when(aktørConsumerMock.hentAktørIdForPersonIdent("StaticSubjectHandlerUserId"))
                .thenReturn(Optional.of(aktørIdForIdent));
        UUID forsendelseId = UUID.randomUUID();
        DokumentMetadata metadata = DokumentMetadata.builder()
                .setForsendelseId(forsendelseId)
                .setBrukerId("1234")
                .setSaksnummer("123")
                .setForsendelseMottatt(LocalDateTime.now())
                .build();
        when(dokumentRepositoryMock.hentEksaktDokumentMetadata(any())).thenReturn(metadata);
        when(dokumentRepositoryMock.hentDokumenter(any()))
                .thenReturn(asList(createDokument(ArkivFilType.PDFA, true), createDokument(ArkivFilType.XML, true)));

        tjeneste.validerDokumentforsendelse(forsendelseId);

        ArgumentCaptor<ProsessTaskData> prosessTaskCaptor = ArgumentCaptor.forClass(ProsessTaskData.class);
        verify(prosessTaskRepositoryMock).lagre(prosessTaskCaptor.capture());
        ProsessTaskData capturedProssessTaskData = prosessTaskCaptor.getValue();
        assertThat(capturedProssessTaskData.getPropertyValue(MottakMeldingDataWrapper.AVSENDER_ID_KEY))
                .isEqualTo(aktørIdForIdent);

    }

    @Test
    public void korrektAntallOgTyper__ett_hoveddokument_er_feil() {
        Set<Dokument> hoveddokumenter = new HashSet<>() {
            {
                add(createDokument(ArkivFilType.XML, true));
            }
        };
        assertThat(tjeneste.korrektAntallOgTyper(hoveddokumenter)).isFalse();
    }

    @Test
    public void korrektAntallOgTyper__tre_hoveddokument_er_feil() {
        Set<Dokument> hoveddokumenter = new HashSet<>() {
            {
                add(createDokument(ArkivFilType.XML, true));
                add(createDokument(ArkivFilType.PDFA, true));
                add(createDokument(ArkivFilType.XML, true));
            }
        };
        assertThat(tjeneste.korrektAntallOgTyper(hoveddokumenter)).isFalse();
    }

    @Test
    public void korrektAntallOgTyper__to_xml_hoveddokument_er_feil() {
        Set<Dokument> hoveddokumenter = new HashSet<>() {
            {
                add(createDokument(ArkivFilType.XML, true));
                add(createDokument(ArkivFilType.XML, true));
            }
        };
        assertThat(tjeneste.korrektAntallOgTyper(hoveddokumenter)).isFalse();
    }

    @Test
    public void korrektAntallOgTyper__to_pdf_hoveddokument_er_feil() {
        Set<Dokument> hoveddokumenter = new HashSet<>() {
            {
                add(createDokument(ArkivFilType.PDFA, true));
                add(createDokument(ArkivFilType.PDFA, true));
            }
        };
        assertThat(tjeneste.korrektAntallOgTyper(hoveddokumenter)).isFalse();
    }

    @Test
    public void korrektAntallOgTyper__ett_xml_og_ett_jpeg_hoveddokument_er_feil() {
        Set<Dokument> hoveddokumenter = new HashSet<>() {
            {
                add(createDokument(ArkivFilType.XML, true));
                add(createDokument(ArkivFilType.JPEG, true));
            }
        };
        assertThat(tjeneste.korrektAntallOgTyper(hoveddokumenter)).isFalse();
    }

    @Test
    public void korrektAntallOgTyper__ett_xml_og_ett_pdf_hoveddokument_er_OK() {
        Set<Dokument> hoveddokumenter = new HashSet<>() {
            {
                add(createDokument(ArkivFilType.XML, true));
                add(createDokument(ArkivFilType.PDFA, true));
            }
        };
        assertThat(tjeneste.korrektAntallOgTyper(hoveddokumenter)).isTrue();
    }

    @Test
    public void dokumentforsendelse_status_pending() {
        ForsendelseIdDto forsendelseIdDto = lagForsendelseIdDto();
        when(dokumentRepositoryMock.hentUnikDokumentMetadata(any(UUID.class)))
                .thenReturn(Optional.of(byggDokumentMetadata(null, null, ForsendelseStatus.PENDING)));

        assertThat(tjeneste.finnStatusinformasjon(forsendelseIdDto.getForsendelseId()).getForsendelseStatus())
                .isEqualByComparingTo(ForsendelseStatus.PENDING);
    }

    @Test
    public void dokumentforsendelse_status_gosys() {
        ForsendelseIdDto forsendelseIdDto = lagForsendelseIdDto();
        when(dokumentRepositoryMock.hentUnikDokumentMetadata(any(UUID.class)))
                .thenReturn(Optional.of(byggDokumentMetadata("1", null, ForsendelseStatus.GOSYS)));

        assertThat(tjeneste.finnStatusinformasjon(forsendelseIdDto.getForsendelseId()).getForsendelseStatus())
                .isEqualByComparingTo(ForsendelseStatus.GOSYS);
    }

    @Test
    public void dokumentforsendelse_status_fpsak() {
        ForsendelseIdDto forsendelseIdDto = lagForsendelseIdDto();
        when(dokumentRepositoryMock.hentUnikDokumentMetadata(any(UUID.class)))
                .thenReturn(Optional.of(byggDokumentMetadata("1", "1", ForsendelseStatus.FPSAK)));

        assertThat(tjeneste.finnStatusinformasjon(forsendelseIdDto.getForsendelseId()).getForsendelseStatus())
                .isEqualByComparingTo(ForsendelseStatus.FPSAK);
    }

    @Test
    public void dokumentforsendelse_med_uuid_ikke_funnet() {
        ForsendelseIdDto forsendelseIdDto = lagForsendelseIdDto();
        when(dokumentRepositoryMock.hentUnikDokumentMetadata(any(UUID.class))).thenReturn(Optional.empty());
        var e = assertThrows(TekniskException.class, () -> tjeneste.finnStatusinformasjon(forsendelseIdDto.getForsendelseId()));
        assertTrue(e.getMessage().contains("FP-295614"));
    }

    @Test
    public void dokumentforsendelse_med_ugyldig_uuid() {
        var e = assertThrows(IllegalArgumentException.class, () -> new ForsendelseIdDto("123"));
        assertTrue(e.getMessage().contains("Invalid UUID"));
    }

    private static Dokument createDokument(ArkivFilType arkivFilType, boolean hovedDokument) {
        return DokumentArkivTestUtil.lagDokument(ID, SØKNAD_FORELDREPENGER_FØDSEL, arkivFilType, hovedDokument);
    }

    private static DokumentMetadata byggDokumentMetadata(String arkivId, String saksnummer, ForsendelseStatus status) {
        return new DokumentMetadata.Builder()
                .setArkivId(arkivId)
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
}