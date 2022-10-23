package no.nav.foreldrepenger.mottak.task.dokumentforsendelse;

import static no.nav.foreldrepenger.mottak.journal.DokumentArkivTestUtil.BRUKER_ID;
import static no.nav.foreldrepenger.mottak.journal.DokumentArkivTestUtil.JOURNALPOST_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import no.nav.foreldrepenger.fordel.kodeverdi.ArkivFilType;
import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverdi.Tema;
import no.nav.foreldrepenger.kontrakter.fordel.FagsakInfomasjonDto;
import no.nav.foreldrepenger.kontrakter.fordel.SaksnummerDto;
import no.nav.foreldrepenger.mottak.domene.dokument.Dokument;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentMetadata;
import no.nav.foreldrepenger.mottak.domene.dokument.DokumentRepository;
import no.nav.foreldrepenger.mottak.domene.oppgavebehandling.OpprettGSakOppgaveTask;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.journal.ArkivTjeneste;
import no.nav.foreldrepenger.mottak.journal.OpprettetJournalpost;
import no.nav.foreldrepenger.mottak.klient.Fagsak;
import no.nav.foreldrepenger.mottak.person.PersonInformasjon;
import no.nav.foreldrepenger.mottak.task.VLKlargjørerTask;
import no.nav.foreldrepenger.mottak.tjeneste.Destinasjon;
import no.nav.foreldrepenger.mottak.tjeneste.DestinasjonsRuter;
import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseStatus;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskTjeneste;
import no.nav.vedtak.felles.prosesstask.api.TaskType;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
class BehandleDokumentforsendelseTaskTest {

    private static final UUID FORSENDELSE_ID = UUID.randomUUID();
    private static final String AKTØR_ID = "9000000000009";
    private static final String PERSON_IDENT = "12345678901";
    private static final String FIL_SØKNAD_ENGST = "engangsstoenad-termin-soeknad.xml";
    private static final String FIL_SØKNAD_FORP = "selvb-soeknad-forp.xml";
    private static final String FIL_SØKNAD_ENDRING = "selvb-soeknad-endring.xml";
    private static final String FIL_SØKNAD_FORP_UTTAK_FØR_KONFIGVERDI = "selvb-soeknad-forp-uttak-før-konfigverdi.xml";

    private static final TaskType KLARGJOR_TASK = TaskType.forProsessTask(VLKlargjørerTask.class);
    private static final TaskType GOSYS_TASK = TaskType.forProsessTask(OpprettGSakOppgaveTask.class);

    @Mock
    private ProsessTaskTjeneste taskTjeneste;
    @Mock
    private PersonInformasjon aktørConsumer;
    @Mock
    private Fagsak fagsakRestKlient;
    @Mock
    private DestinasjonsRuter vurderVLSaker;
    @Mock
    private DokumentRepository dokumentRepository;
    @Mock
    private ArkivTjeneste arkivTjeneste;

    private BehandleDokumentforsendelseTask fordelDokTask;
    private ProsessTaskData ptd;

    @BeforeEach
    void setup() {
        when(aktørConsumer.hentPersonIdentForAktørId(any())).thenReturn(Optional.of(PERSON_IDENT));
        fordelDokTask = new BehandleDokumentforsendelseTask(taskTjeneste, vurderVLSaker, aktørConsumer,
                fagsakRestKlient, arkivTjeneste, dokumentRepository);
        ptd = ProsessTaskData.forProsessTask(BehandleDokumentforsendelseTask.class);
    }

    @Test
    void testAvPrecondition() {
        MottakMeldingDataWrapper innData = new MottakMeldingDataWrapper(ptd);

        var e = assertThrows(TekniskException.class, () -> fordelDokTask.precondition(innData));
        assertTrue(e.getMessage().contains("FP-941984"));
    }

    @Test
    void testAvPostCondition() {
        MottakMeldingDataWrapper utData = new MottakMeldingDataWrapper(ptd);
        var e = assertThrows(TekniskException.class, () -> fordelDokTask.postcondition(utData));
        assertTrue(e.getMessage().contains("FP-638068"));

    }

    @Test
    void skalReturnereWrapperMedNesteTaskHentOgVurderVLSak() {
        MottakMeldingDataWrapper inndata = new MottakMeldingDataWrapper(ptd);
        inndata.setForsendelseId(FORSENDELSE_ID);

        when(dokumentRepository.hentUnikDokument(any(UUID.class), anyBoolean(), any())).thenReturn(
                Optional.of(genDokument(DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL, FIL_SØKNAD_ENGST, true)));
        when(dokumentRepository.hentEksaktDokumentMetadata(any(UUID.class))).thenReturn(genMetadata(null, AKTØR_ID));
        when(vurderVLSaker.bestemDestinasjon(any())).thenReturn(Destinasjon.FPSAK_UTEN_SAK);
        when(vurderVLSaker.opprettSak(any())).thenReturn("123");
        when(arkivTjeneste.opprettJournalpost(any(), any(String.class), any())).thenReturn(new OpprettetJournalpost(JOURNALPOST_ID, true));

        MottakMeldingDataWrapper utdata = kjørMedPreOgPostcondition(inndata);
        assertThat(utdata.getProsessTaskData().taskType()).isEqualTo(KLARGJOR_TASK);
        assertThat(utdata.getDokumentTypeId()).hasValue(DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL);
        verify(arkivTjeneste).opprettJournalpost(any(), any(String.class), eq("123"));
    }

    @Test
    void skalReturnereTilJournalføringSakTask() {
        MottakMeldingDataWrapper inndata = new MottakMeldingDataWrapper(ptd);
        inndata.setForsendelseId(FORSENDELSE_ID);

        when(dokumentRepository.hentUnikDokument(any(UUID.class), anyBoolean(), any())).thenReturn(
                Optional.of(genDokument(DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL, FIL_SØKNAD_FORP, true)));
        when(dokumentRepository.hentEksaktDokumentMetadata(any(UUID.class))).thenReturn(genMetadata(null, AKTØR_ID));
        when(fagsakRestKlient.finnFagsakInfomasjon(any(SaksnummerDto.class))).thenReturn(genFagsakInformasjon("ab0047"));
        when(vurderVLSaker.bestemDestinasjon(any())).thenReturn(new Destinasjon(ForsendelseStatus.FPSAK, "123"));
        when(arkivTjeneste.opprettJournalpost(any(), any(String.class), eq("123"))).thenReturn(new OpprettetJournalpost(JOURNALPOST_ID, true));

        MottakMeldingDataWrapper utdata = kjørMedPreOgPostcondition(inndata);
        assertThat(utdata.getProsessTaskData().taskType()).isEqualTo(KLARGJOR_TASK);
        assertThat(utdata.getDokumentTypeId()).hasValue(DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL);
    }

    @Test
    void skalReturnereOpprettGSakOppgaveTaskHvisSøknadDatoForForeldrependerStartDato() {
        MottakMeldingDataWrapper inndata = new MottakMeldingDataWrapper(ptd);
        inndata.setForsendelseId(FORSENDELSE_ID);

        when(dokumentRepository.hentUnikDokument(any(UUID.class), anyBoolean(), any())).thenReturn(Optional.of(
                genDokument(DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL, FIL_SØKNAD_FORP_UTTAK_FØR_KONFIGVERDI, true)));
        when(dokumentRepository.hentEksaktDokumentMetadata(any(UUID.class))).thenReturn(genMetadata(null, AKTØR_ID));
        when(vurderVLSaker.bestemDestinasjon(any())).thenReturn(Destinasjon.GOSYS);
        when(arkivTjeneste.opprettJournalpost(any(), any(UUID.class), any())).thenReturn(new OpprettetJournalpost(JOURNALPOST_ID, false));

        MottakMeldingDataWrapper utdata = kjørMedPreOgPostcondition(inndata);
        assertThat(utdata.getProsessTaskData().taskType()).isEqualTo(GOSYS_TASK);
    }

    @Test
    void skalSendeTilJournalføringHvisSaksnummerFinnesPåMetadataOgIVL() {
        MottakMeldingDataWrapper inndata = new MottakMeldingDataWrapper(ptd);
        inndata.setForsendelseId(FORSENDELSE_ID);
        inndata.setAktørId(AKTØR_ID);

        when(dokumentRepository.hentUnikDokument(any(UUID.class), anyBoolean(), any())).thenReturn(
                Optional.of(genDokument(DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL, FIL_SØKNAD_FORP, true)));
        when(dokumentRepository.hentEksaktDokumentMetadata(any(UUID.class))).thenReturn(genMetadata("123", AKTØR_ID));
        when(fagsakRestKlient.finnFagsakInfomasjon(any(SaksnummerDto.class)))
                .thenReturn(genFagsakInformasjon("ab0047"));
        when(arkivTjeneste.opprettJournalpost(any(), any(String.class), eq("123"))).thenReturn(new OpprettetJournalpost(JOURNALPOST_ID, true));


        MottakMeldingDataWrapper utdata = kjørMedPreOgPostcondition(inndata);
        assertThat(utdata.getProsessTaskData().taskType()).isEqualTo(KLARGJOR_TASK);
    }

    @Test
    void skalsendEndringssøknadMedSaksnummeriVLTilJournalføring() {
        MottakMeldingDataWrapper inndata = new MottakMeldingDataWrapper(ptd);
        inndata.setForsendelseId(FORSENDELSE_ID);
        inndata.setAktørId(AKTØR_ID);
        inndata.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);

        Dokument dokumentEndring = genDokument(DokumentTypeId.FORELDREPENGER_ENDRING_SØKNAD, FIL_SØKNAD_ENDRING, true);

        when(dokumentRepository.hentUnikDokument(any(UUID.class), anyBoolean(), any()))
                .thenReturn(Optional.of(dokumentEndring));
        when(dokumentRepository.hentEksaktDokumentMetadata(any(UUID.class))).thenReturn(genMetadata("123", AKTØR_ID));
        when(fagsakRestKlient.finnFagsakInfomasjon(any(SaksnummerDto.class)))
                .thenReturn(genFagsakInformasjon("ab0047"));
        when(arkivTjeneste.opprettJournalpost(any(), any(String.class), eq("123"))).thenReturn(new OpprettetJournalpost(JOURNALPOST_ID, true));


        MottakMeldingDataWrapper utdata = kjørMedPreOgPostcondition(inndata);
        assertThat(utdata.getProsessTaskData().taskType()).isEqualTo(KLARGJOR_TASK);
        assertThat(utdata.getDokumentTypeId()).hasValue(DokumentTypeId.FORELDREPENGER_ENDRING_SØKNAD);
    }

    @Test
    void skalsendEndringssøknadUtenSaksnummeriVLTilGosys() {
        MottakMeldingDataWrapper inndata = new MottakMeldingDataWrapper(ptd);
        inndata.setForsendelseId(FORSENDELSE_ID);
        inndata.setAktørId(AKTØR_ID);
        inndata.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);

        Dokument dokumentEndring = genDokument(DokumentTypeId.FORELDREPENGER_ENDRING_SØKNAD, FIL_SØKNAD_ENDRING, true);

        when(dokumentRepository.hentUnikDokument(any(UUID.class), anyBoolean(), any()))
                .thenReturn(Optional.of(dokumentEndring));
        when(dokumentRepository.hentEksaktDokumentMetadata(any(UUID.class))).thenReturn(genMetadata("123", AKTØR_ID));
        when(fagsakRestKlient.finnFagsakInfomasjon(any(SaksnummerDto.class))).thenReturn(Optional.empty());
        when(arkivTjeneste.opprettJournalpost(any(), any(UUID.class), any())).thenReturn(new OpprettetJournalpost(JOURNALPOST_ID, false));

        MottakMeldingDataWrapper utdata = kjørMedPreOgPostcondition(inndata);
        assertThat(utdata.getProsessTaskData().taskType()).isEqualTo(GOSYS_TASK);
        assertThat(utdata.getDokumentTypeId()).hasValue(DokumentTypeId.FORELDREPENGER_ENDRING_SØKNAD);
    }

    @Test
    void skalEttersendtVedleggTilJournalføring() {
        MottakMeldingDataWrapper inndata = new MottakMeldingDataWrapper(ptd);
        inndata.setForsendelseId(FORSENDELSE_ID);
        inndata.setAktørId(AKTØR_ID);
        inndata.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);

        when(dokumentRepository.hentUnikDokument(any(UUID.class), anyBoolean(), any())).thenReturn(Optional.empty());
        when(dokumentRepository.hentEksaktDokumentMetadata(any(UUID.class))).thenReturn(genMetadata("123", AKTØR_ID));
        when(dokumentRepository.hentDokumenter(any(UUID.class))).thenReturn(Collections.singletonList(
                genDokument(DokumentTypeId.DOKUMENTASJON_AV_TERMIN_ELLER_FØDSEL, FIL_SØKNAD_FORP, false)));
        when(fagsakRestKlient.finnFagsakInfomasjon(any(SaksnummerDto.class)))
                .thenReturn(genFagsakInformasjon("ab0047"));
        when(arkivTjeneste.opprettJournalpost(any(), any(String.class), eq("123"))).thenReturn(new OpprettetJournalpost(JOURNALPOST_ID, true));


        MottakMeldingDataWrapper utdata = kjørMedPreOgPostcondition(inndata);
        assertThat(utdata.getProsessTaskData().taskType()).isEqualTo(KLARGJOR_TASK);
        assertThat(utdata.getDokumentTypeId()).hasValue(DokumentTypeId.DOKUMENTASJON_AV_TERMIN_ELLER_FØDSEL);
    }

    @Test
    void skalEttersendtVedleggTilEksernSakTypeAnnet() {
        MottakMeldingDataWrapper inndata = new MottakMeldingDataWrapper(ptd);
        inndata.setForsendelseId(FORSENDELSE_ID);
        //inndata.setAktørId(AKTØR_ID);
        inndata.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);

        when(dokumentRepository.hentUnikDokument(any(UUID.class), anyBoolean(), any())).thenReturn(Optional.empty());
        when(dokumentRepository.hentEksaktDokumentMetadata(any(UUID.class))).thenReturn(genMetadata("123", AKTØR_ID));
        when(dokumentRepository.hentDokumenter(any(UUID.class))).thenReturn(Collections.singletonList(
                genDokument(DokumentTypeId.ANNET, FIL_SØKNAD_FORP, false)));
        when(fagsakRestKlient.finnFagsakInfomasjon(any(SaksnummerDto.class)))
                .thenReturn(Optional.empty());
        when(arkivTjeneste.opprettJournalpost(any(), any(UUID.class), any())).thenReturn(new OpprettetJournalpost(JOURNALPOST_ID, false));


        MottakMeldingDataWrapper utdata = kjørMedPreOgPostcondition(inndata);
        assertThat(utdata.getProsessTaskData().taskType()).isEqualTo(GOSYS_TASK);
        assertThat(utdata.getAktørId()).isEqualTo(Optional.of(AKTØR_ID));
        assertThat(utdata.getDokumentTypeId()).hasValue(DokumentTypeId.ANNET);
    }

    @Test
    void skalSendeTilOpprettOppgaveHvisSaksnummerFinnesPåMetadataMenIkkeIVL() {
        MottakMeldingDataWrapper inndata = new MottakMeldingDataWrapper(ptd);
        inndata.setForsendelseId(FORSENDELSE_ID);
        inndata.setAktørId(AKTØR_ID);

        when(dokumentRepository.hentUnikDokument(any(UUID.class), anyBoolean(), any())).thenReturn(
                Optional.of(genDokument(DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL, FIL_SØKNAD_FORP, true)));
        when(dokumentRepository.hentEksaktDokumentMetadata(any(UUID.class))).thenReturn(genMetadata("123", AKTØR_ID));
        when(fagsakRestKlient.finnFagsakInfomasjon(any(SaksnummerDto.class))).thenReturn(Optional.empty());
        when(arkivTjeneste.opprettJournalpost(any(), any(UUID.class), any())).thenReturn(new OpprettetJournalpost(JOURNALPOST_ID, false));


        MottakMeldingDataWrapper utdata = kjørMedPreOgPostcondition(inndata);
        assertThat(utdata.getProsessTaskData().taskType()).isEqualTo(GOSYS_TASK);
    }

    @Test
    void skalFeileHvisBehandlingstemaIkkeMatcher() {
        MottakMeldingDataWrapper inndata = new MottakMeldingDataWrapper(ptd);
        inndata.setForsendelseId(FORSENDELSE_ID);
        inndata.setAktørId(AKTØR_ID);

        when(dokumentRepository.hentUnikDokument(any(UUID.class), anyBoolean(), any())).thenReturn(
                Optional.of(genDokument(DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL, FIL_SØKNAD_FORP, true)));
        when(dokumentRepository.hentEksaktDokumentMetadata(any(UUID.class))).thenReturn(genMetadata("123", AKTØR_ID));
        when(fagsakRestKlient.finnFagsakInfomasjon(any(SaksnummerDto.class)))
                .thenReturn(genFagsakInformasjon("ab0072"));

        assertThatThrownBy(() -> kjørMedPreOgPostcondition(inndata))
                .isInstanceOf(TekniskException.class)
                .hasMessageContaining("FP-756353");
    }

    @Test
    void test_skalVedJournalføringAvDokumentForsendelseFåJournalTilstandMidlertidigJournalført() {
        var data = new MottakMeldingDataWrapper(ptd);

        when(arkivTjeneste.opprettJournalpost(any(), any(UUID.class), any())).thenReturn(new OpprettetJournalpost(JOURNALPOST_ID, false));
        when(dokumentRepository.hentUnikDokument(any(UUID.class), anyBoolean(), any())).thenReturn(
                Optional.of(genDokument(DokumentTypeId.SØKNAD_FORELDREPENGER_FØDSEL, FIL_SØKNAD_FORP, true)));
        when(dokumentRepository.hentEksaktDokumentMetadata(any(UUID.class))).thenReturn(genMetadata(null, AKTØR_ID));
        when(fagsakRestKlient.finnFagsakInfomasjon(any(SaksnummerDto.class))).thenReturn(genFagsakInformasjon("ab0047"));
        when(vurderVLSaker.bestemDestinasjon(any())).thenReturn(Destinasjon.GOSYS);

        data.setForsendelseId(FORSENDELSE_ID);
        data.setAktørId(BRUKER_ID);
        data.setBehandlingTema(BehandlingTema.FORELDREPENGER_FØDSEL);
        data.setTema(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);

        MottakMeldingDataWrapper utdata = kjørMedPreOgPostcondition(data);
        assertThat(utdata.getProsessTaskData().taskType()).isEqualTo(GOSYS_TASK);

        verify(arkivTjeneste).opprettJournalpost(eq(FORSENDELSE_ID), eq(FORSENDELSE_ID), any());
        verify(dokumentRepository).oppdaterForsendelseMedArkivId(any(UUID.class), any(), eq(ForsendelseStatus.GOSYS));
    }

    @Test
    void validerSjekkAvsenderOppretter() {
        String aktørIdForIdent = "1234567890123";
        String aktørIdForAnnen = "1234567890987";
        String fnrForIdent = "12345678901";

        UUID forsendelseId = UUID.randomUUID();
        var metadata = DokumentMetadata.builder()
                .setForsendelseId(forsendelseId)
                .setBrukerId(aktørIdForIdent)
                .setSaksnummer("123")
                .setForsendelseMottatt(LocalDateTime.now())
                .build();
        metadata.setOpprettetAv(fnrForIdent);

        when(aktørConsumer.hentAktørIdForPersonIdent(fnrForIdent)).thenReturn(Optional.of(aktørIdForIdent));
        assertDoesNotThrow(() -> fordelDokTask.validerBrukerAvsender(metadata));

        when(aktørConsumer.hentAktørIdForPersonIdent(fnrForIdent)).thenReturn(Optional.of(aktørIdForAnnen));
        assertThrows(TekniskException.class, () -> fordelDokTask.validerBrukerAvsender(metadata));

        metadata.setOpprettetAv("VL");
        assertDoesNotThrow(() -> fordelDokTask.validerBrukerAvsender(metadata));
    }

    private static Optional<FagsakInfomasjonDto> genFagsakInformasjon(String behTemaOffisiellKode) {
        return Optional.of(new FagsakInfomasjonDto(AKTØR_ID, behTemaOffisiellKode));
    }

    private MottakMeldingDataWrapper kjørMedPreOgPostcondition(MottakMeldingDataWrapper inndata) {
        MottakMeldingDataWrapper utdata;
        fordelDokTask.precondition(inndata);
        utdata = fordelDokTask.doTask(inndata);
        fordelDokTask.postcondition(utdata);
        return utdata;
    }

    private Dokument genDokument(DokumentTypeId dokumentTypeId, String testXmlFilnavn, boolean hovedDokument) {
        String xml = "";
        try {
            xml = readFile("testsoknader/" + testXmlFilnavn);
        } catch (URISyntaxException e) {
            fail(e.getMessage());
        } catch (IOException e) {
            fail(e.getMessage());
        }

        Dokument dokument = Dokument.builder()
                .setForsendelseId(FORSENDELSE_ID)
                .setDokumentInnhold(xml.getBytes(Charset.forName("UTF-8")), ArkivFilType.XML)
                .setDokumentTypeId(dokumentTypeId)
                .setHovedDokument(hovedDokument)
                .build();
        return dokument;

    }

    private static DokumentMetadata genMetadata(String saksnummer, String aktørId) {
        DokumentMetadata metadata = DokumentMetadata.builder()
                .setForsendelseId(FORSENDELSE_ID)
                .setBrukerId(aktørId)
                .setForsendelseMottatt(LocalDateTime.now())
                .build();
        if (saksnummer != null) {
            metadata.setSaksnummer(saksnummer);
        }
        return metadata;
    }

    protected String readFile(String filename) throws URISyntaxException, IOException {
        Path path = Paths.get(getClass().getClassLoader().getResource(filename).toURI());
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
