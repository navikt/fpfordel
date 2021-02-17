package no.nav.foreldrepenger.mottak.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentKategori;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.kontrakter.fordel.OpprettSakDto;
import no.nav.foreldrepenger.kontrakter.fordel.SaksnummerDto;
import no.nav.foreldrepenger.mottak.domene.MottattStrukturertDokument;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.klient.FagsakTjeneste;
import no.nav.foreldrepenger.mottak.klient.VurderFagsystemResultat;
import no.nav.foreldrepenger.mottak.person.PersonInformasjon;
import no.nav.foreldrepenger.mottak.task.xml.MeldingXmlParser;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
class OpprettSakTaskTest {

    public static final String FNR = "99999999999";
    public static final String AKTØR_ID = "9000000000009";

    @Mock
    private ProsessTaskRepository prosessTaskRepositoryMock;
    @Mock
    private FagsakTjeneste fagsakRestKlient;
    @Mock
    private PersonInformasjon aktørConsumer;

    private OpprettSakTask task;

    @BeforeEach
    void setUp() {
        when(aktørConsumer.hentAktørIdForPersonIdent(FNR)).thenReturn(Optional.of(AKTØR_ID));
        var vurderFagsystemRespons = new VurderFagsystemResultat();
        vurderFagsystemRespons.setBehandlesIVedtaksløsningen(true);
        when(fagsakRestKlient.vurderFagsystem(any())).thenReturn(vurderFagsystemRespons);
        task = new OpprettSakTask(prosessTaskRepositoryMock, fagsakRestKlient);
    }

    @Test
    void test_doTask_fødsel_strukturert() throws Exception {

        var prosessTaskData = new ProsessTaskData(OpprettSakTask.TASKNAME);
        prosessTaskData.setSekvens("1");

        String filename = "testsoknader/engangsstoenad-termin-soeknad.xml";
        Path path = Paths.get(getClass().getClassLoader().getResource(filename).toURI());
        String xml = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);

        var ptData = new MottakMeldingDataWrapper(prosessTaskData);
        ptData.setArkivId("123");
        ptData.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_FØDSEL);
        ptData.setDokumentKategori(DokumentKategori.SØKNAD);
        ptData.setAktørId(AKTØR_ID);

        ptData.setDokumentTypeId(DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL);
        final MottattStrukturertDokument<?> soeknadDTO = MeldingXmlParser.unmarshallXml(xml);
        soeknadDTO.kopierTilMottakWrapper(ptData, aktørConsumer::hentAktørIdForPersonIdent);

        String saksnummer = "789";
        var saksnummerDto = new SaksnummerDto(saksnummer);
        when(fagsakRestKlient.opprettSak(any(OpprettSakDto.class))).thenReturn(saksnummerDto);

        MottakMeldingDataWrapper result = doTaskWithPrecondition(ptData);
        assertThat(result.getProsessTaskData().getTaskType()).isEqualTo(TilJournalføringTask.TASKNAME);
        assertThat(result.getSaksnummer()).isPresent()
                .contains(saksnummer);
    }

    @Test
    void test_doTask_fødsel_ustrukturert() {
        var prosessTaskData = new ProsessTaskData(OpprettSakTask.TASKNAME);
        prosessTaskData.setSekvens("1");

        var ptData = new MottakMeldingDataWrapper(prosessTaskData);
        ptData.setArkivId("123");
        ptData.setAktørId(AKTØR_ID);

        ptData.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_FØDSEL);
        ptData.setDokumentKategori(DokumentKategori.SØKNAD);
        ptData.setDokumentTypeId(DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL);

        String saksnummer = "789";
        SaksnummerDto saksnummerDto = new SaksnummerDto(saksnummer);
        when(fagsakRestKlient.opprettSak(any(OpprettSakDto.class))).thenReturn(saksnummerDto);

        var result = doTaskWithPrecondition(ptData);
        assertThat(result.getProsessTaskData().getTaskType()).isEqualTo(TilJournalføringTask.TASKNAME);
        assertThat(result.getSaksnummer()).isPresent()
                .contains(saksnummer);
    }

    private MottakMeldingDataWrapper doTaskWithPrecondition(MottakMeldingDataWrapper ptData) {
        task.precondition(ptData);
        return task.doTask(ptData);
    }

    @Test
    void test_doTask_anke_klage() {
        ProsessTaskData innData = new ProsessTaskData(OpprettSakTask.TASKNAME);
        innData.setSekvens("1");

        var ptData = new MottakMeldingDataWrapper(innData);

        ptData.setArkivId("123");
        ptData.setAktørId(AKTØR_ID);
        ptData.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_FØDSEL);
        ptData.setDokumentTypeId(DokumentTypeId.KLAGE_DOKUMENT);
        ptData.setDokumentKategori(DokumentKategori.KLAGE_ELLER_ANKE);

        var wrapper = doTaskWithPrecondition(ptData);
        assertThat(MidlJournalføringTask.TASKNAME).isEqualTo(wrapper.getProsessTaskData().getTaskType());
    }

    @Test
    void test_doTask_uten_dokumentkategori() {
        var innData = new ProsessTaskData(OpprettSakTask.TASKNAME);
        innData.setSekvens("1");

        var ptData = new MottakMeldingDataWrapper(innData);

        ptData.setAktørId(AKTØR_ID);
        ptData.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_FØDSEL);
        ptData.setDokumentTypeId(DokumentTypeId.KLAGE_DOKUMENT);
        var e = assertThrows(TekniskException.class, () -> doTaskWithPrecondition(ptData));
        assertTrue(e.getMessage().contains("FP-941984"));
    }

    @Test
    void test_validerDatagrunnlag_skal_feile_ved_manglende_personId() {
        var meldingDataWrapper = new MottakMeldingDataWrapper(new ProsessTaskData(OpprettSakTask.TASKNAME));
        assertThrows(TekniskException.class, () -> task.precondition(meldingDataWrapper));
    }

    @Test
    void test_validerDatagrunnlag_skal_feile_ved_manglende_behandlingstema() {
        var meldingDataWrapper = new MottakMeldingDataWrapper(new ProsessTaskData(OpprettSakTask.TASKNAME));
        meldingDataWrapper.setAktørId("123");
        assertThrows(TekniskException.class, () -> task.precondition(meldingDataWrapper));
    }

    @Test
    void test_validerDatagrunnlag_uten_feil() {
        var prosessTaskData = new ProsessTaskData(OpprettSakTask.TASKNAME);
        var data = new MottakMeldingDataWrapper(prosessTaskData);

        data.setArkivId("123");
        data.setAktørId(AKTØR_ID);
        data.setAktørId(AKTØR_ID);
        data.setDokumentKategori(DokumentKategori.SØKNAD);
        data.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_FØDSEL);
        data.setDokumentTypeId(DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL);
        task.precondition(data);
    }

    @Test
    // https://jira.adeo.no/browse/PFP-1730
    void skalIkkeOppretteNySakHvisDetFinnesEksisterende() {
        String saksnr = "GjenspeilDinSjel";
        var vurderFagsystemRespons = new VurderFagsystemResultat();
        vurderFagsystemRespons.setSaksnummer(saksnr);
        vurderFagsystemRespons.setBehandlesIVedtaksløsningen(true);
        when(fagsakRestKlient.vurderFagsystem(any())).thenReturn(vurderFagsystemRespons);

        var prosessTaskData = new ProsessTaskData(OpprettSakTask.TASKNAME);
        prosessTaskData.setSekvens("1");
        var ptData = new MottakMeldingDataWrapper(prosessTaskData);
        ptData.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_FØDSEL);
        ptData.setDokumentKategori(DokumentKategori.SØKNAD);
        ptData.setAktørId("1");
        ptData.setDokumentTypeId(DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL);

        var res = task.doTask(ptData);
        verify(fagsakRestKlient, never()).opprettSak(any());
        assertEquals(saksnr, res.getSaksnummer().get());
    }

    @Test
    void test_doTask_svangerskapspenger_søknad() throws Exception {

        var prosessTaskData = new ProsessTaskData(OpprettSakTask.TASKNAME);
        prosessTaskData.setSekvens("1");

        String filename = "testsoknader/svangerskapspenger.xml";
        Path path = Paths.get(getClass().getClassLoader().getResource(filename).toURI());
        String xml = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);

        var ptData = new MottakMeldingDataWrapper(prosessTaskData);
        ptData.setArkivId("123");
        ptData.setAktørId("9000000000009");
        ptData.setBehandlingTema(BehandlingTema.SVANGERSKAPSPENGER);
        ptData.setDokumentKategori(DokumentKategori.SØKNAD);

        ptData.setDokumentTypeId(DokumentTypeId.SØKNAD_SVANGERSKAPSPENGER);
        var<?> soeknadDTO = MeldingXmlParser.unmarshallXml(xml);
        soeknadDTO.kopierTilMottakWrapper(ptData, aktørConsumer::hentAktørIdForPersonIdent);

        String saksnummer = "789";
        var saksnummerDto = new SaksnummerDto(saksnummer);
        when(fagsakRestKlient.opprettSak(any(OpprettSakDto.class))).thenReturn(saksnummerDto);

        var result = doTaskWithPrecondition(ptData);
        assertThat(result.getProsessTaskData().getTaskType()).isEqualTo(TilJournalføringTask.TASKNAME);
        assertThat(result.getSaksnummer()).isPresent()
                .contains(saksnummer);
    }

    @Test
    void test_doTask_svangerskapspenger_inntektsmelding() throws Exception {

        var prosessTaskData = new ProsessTaskData(OpprettSakTask.TASKNAME);
        prosessTaskData.setSekvens("1");

        String filename = "testsoknader/inntektsmelding-svp.xml";
        Path path = Paths.get(getClass().getClassLoader().getResource(filename).toURI());
        String xml = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);

        var ptData = new MottakMeldingDataWrapper(prosessTaskData);
        ptData.setArkivId("123");
        ptData.setAktørId("1000104134079");
        ptData.setBehandlingTema(BehandlingTema.SVANGERSKAPSPENGER);
        ptData.setDokumentKategori(DokumentKategori.UDEFINERT);

        ptData.setDokumentTypeId(DokumentTypeId.INNTEKTSMELDING);
        var imlDto = MeldingXmlParser.unmarshallXml(xml);
        imlDto.kopierTilMottakWrapper(ptData, aktørConsumer::hentAktørIdForPersonIdent);

        String saksnummer = "789";
        var saksnummerDto = new SaksnummerDto(saksnummer);
        when(fagsakRestKlient.opprettSak(any(OpprettSakDto.class))).thenReturn(saksnummerDto);

        var result = doTaskWithPrecondition(ptData);
        assertThat(result.getProsessTaskData().getTaskType()).isEqualTo(TilJournalføringTask.TASKNAME);
        assertThat(result.getSaksnummer()).isPresent()
                .contains(saksnummer);
    }

}
