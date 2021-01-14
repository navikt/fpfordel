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
@MockitoSettings(strictness = Strictness.LENIENT)
public class OpprettSakTaskTest {

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
    public void setUp() {
        when(aktørConsumer.hentAktørIdForPersonIdent(FNR)).thenReturn(Optional.of(AKTØR_ID));
        VurderFagsystemResultat vurderFagsystemRespons = new VurderFagsystemResultat();
        vurderFagsystemRespons.setBehandlesIVedtaksløsningen(true);
        when(fagsakRestKlient.vurderFagsystem(any())).thenReturn(vurderFagsystemRespons);
        task = new OpprettSakTask(prosessTaskRepositoryMock, fagsakRestKlient);
    }

    @Test
    public void test_doTask_fødsel_strukturert() throws Exception {

        ProsessTaskData prosessTaskData = new ProsessTaskData(OpprettSakTask.TASKNAME);
        prosessTaskData.setSekvens("1");

        String filename = "testsoknader/engangsstoenad-termin-soeknad.xml";
        Path path = Paths.get(getClass().getClassLoader().getResource(filename).toURI());
        String xml = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);

        MottakMeldingDataWrapper ptData = new MottakMeldingDataWrapper(prosessTaskData);
        ptData.setArkivId("123");
        ptData.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_FØDSEL);
        ptData.setDokumentKategori(DokumentKategori.SØKNAD);
        ptData.setAktørId(AKTØR_ID);

        ptData.setDokumentTypeId(DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL);
        final MottattStrukturertDokument<?> soeknadDTO = MeldingXmlParser.unmarshallXml(xml);
        soeknadDTO.kopierTilMottakWrapper(ptData, aktørConsumer::hentAktørIdForPersonIdent);

        String saksnummer = "789";
        SaksnummerDto saksnummerDto = new SaksnummerDto(saksnummer);
        when(fagsakRestKlient.opprettSak(any(OpprettSakDto.class))).thenReturn(saksnummerDto);

        MottakMeldingDataWrapper result = doTaskWithPrecondition(ptData);
        assertThat(result.getProsessTaskData().getTaskType()).isEqualTo(TilJournalføringTask.TASKNAME);
        assertThat(result.getSaksnummer()).isPresent()
                .contains(saksnummer);
    }

    @Test
    public void test_doTask_fødsel_ustrukturert() {
        ProsessTaskData prosessTaskData = new ProsessTaskData(OpprettSakTask.TASKNAME);
        prosessTaskData.setSekvens("1");

        MottakMeldingDataWrapper ptData = new MottakMeldingDataWrapper(prosessTaskData);
        ptData.setArkivId("123");
        ptData.setAktørId(AKTØR_ID);

        ptData.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_FØDSEL);
        ptData.setDokumentKategori(DokumentKategori.SØKNAD);
        ptData.setDokumentTypeId(DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL);

        String saksnummer = "789";
        SaksnummerDto saksnummerDto = new SaksnummerDto(saksnummer);
        when(fagsakRestKlient.opprettSak(any(OpprettSakDto.class))).thenReturn(saksnummerDto);

        MottakMeldingDataWrapper result = doTaskWithPrecondition(ptData);
        assertThat(result.getProsessTaskData().getTaskType()).isEqualTo(TilJournalføringTask.TASKNAME);
        assertThat(result.getSaksnummer()).isPresent()
                .contains(saksnummer);
    }

    private MottakMeldingDataWrapper doTaskWithPrecondition(MottakMeldingDataWrapper ptData) {
        task.precondition(ptData);
        return task.doTask(ptData);
    }

    @Test
    public void test_doTask_anke_klage() {
        ProsessTaskData innData = new ProsessTaskData(OpprettSakTask.TASKNAME);
        innData.setSekvens("1");

        MottakMeldingDataWrapper ptData = new MottakMeldingDataWrapper(innData);

        ptData.setArkivId("123");
        ptData.setAktørId(AKTØR_ID);
        ptData.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_FØDSEL);
        ptData.setDokumentTypeId(DokumentTypeId.KLAGE_DOKUMENT);
        ptData.setDokumentKategori(DokumentKategori.KLAGE_ELLER_ANKE);

        MottakMeldingDataWrapper wrapper = doTaskWithPrecondition(ptData);
        assertThat(MidlJournalføringTask.TASKNAME).isEqualTo(wrapper.getProsessTaskData().getTaskType());
    }

    @Test
    public void test_doTask_uten_dokumentkategori() {
        ProsessTaskData innData = new ProsessTaskData(OpprettSakTask.TASKNAME);
        innData.setSekvens("1");

        MottakMeldingDataWrapper ptData = new MottakMeldingDataWrapper(innData);

        ptData.setAktørId(AKTØR_ID);
        ptData.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_FØDSEL);
        ptData.setDokumentTypeId(DokumentTypeId.KLAGE_DOKUMENT);
        var e = assertThrows(TekniskException.class, () -> doTaskWithPrecondition(ptData));
        assertTrue(e.getMessage().contains("FP-941984"));
    }

    @Test
    public void test_validerDatagrunnlag_skal_feile_ved_manglende_personId() {
        MottakMeldingDataWrapper meldingDataWrapper = new MottakMeldingDataWrapper(new ProsessTaskData(OpprettSakTask.TASKNAME));
        assertThrows(TekniskException.class, () -> task.precondition(meldingDataWrapper));
    }

    @Test
    public void test_validerDatagrunnlag_skal_feile_ved_manglende_behandlingstema() {
        MottakMeldingDataWrapper meldingDataWrapper = new MottakMeldingDataWrapper(new ProsessTaskData(OpprettSakTask.TASKNAME));
        meldingDataWrapper.setAktørId("123");
        assertThrows(TekniskException.class, () -> task.precondition(meldingDataWrapper));
    }

    @Test
    public void test_validerDatagrunnlag_uten_feil() {
        ProsessTaskData prosessTaskData = new ProsessTaskData(OpprettSakTask.TASKNAME);
        MottakMeldingDataWrapper data = new MottakMeldingDataWrapper(prosessTaskData);

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
    public void skalIkkeOppretteNySakHvisDetFinnesEksisterende() {
        String saksnr = "GjenspeilDinSjel";
        VurderFagsystemResultat vurderFagsystemRespons = new VurderFagsystemResultat();
        vurderFagsystemRespons.setSaksnummer(saksnr);
        vurderFagsystemRespons.setBehandlesIVedtaksløsningen(true);
        when(fagsakRestKlient.vurderFagsystem(any())).thenReturn(vurderFagsystemRespons);

        ProsessTaskData prosessTaskData = new ProsessTaskData(OpprettSakTask.TASKNAME);
        prosessTaskData.setSekvens("1");
        MottakMeldingDataWrapper ptData = new MottakMeldingDataWrapper(prosessTaskData);
        ptData.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_FØDSEL);
        ptData.setDokumentKategori(DokumentKategori.SØKNAD);
        ptData.setAktørId("1");
        ptData.setDokumentTypeId(DokumentTypeId.SØKNAD_ENGANGSSTØNAD_FØDSEL);

        MottakMeldingDataWrapper res = task.doTask(ptData);
        verify(fagsakRestKlient, never()).opprettSak(any());
        assertEquals(saksnr, res.getSaksnummer().get());
    }

    @Test
    public void test_doTask_svangerskapspenger_søknad() throws Exception {

        ProsessTaskData prosessTaskData = new ProsessTaskData(OpprettSakTask.TASKNAME);
        prosessTaskData.setSekvens("1");

        String filename = "testsoknader/svangerskapspenger.xml";
        Path path = Paths.get(getClass().getClassLoader().getResource(filename).toURI());
        String xml = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);

        MottakMeldingDataWrapper ptData = new MottakMeldingDataWrapper(prosessTaskData);
        ptData.setArkivId("123");
        ptData.setAktørId("9000000000009");
        ptData.setBehandlingTema(BehandlingTema.SVANGERSKAPSPENGER);
        ptData.setDokumentKategori(DokumentKategori.SØKNAD);

        ptData.setDokumentTypeId(DokumentTypeId.SØKNAD_SVANGERSKAPSPENGER);
        final MottattStrukturertDokument<?> soeknadDTO = MeldingXmlParser.unmarshallXml(xml);
        soeknadDTO.kopierTilMottakWrapper(ptData, aktørConsumer::hentAktørIdForPersonIdent);

        String saksnummer = "789";
        SaksnummerDto saksnummerDto = new SaksnummerDto(saksnummer);
        when(fagsakRestKlient.opprettSak(any(OpprettSakDto.class))).thenReturn(saksnummerDto);

        MottakMeldingDataWrapper result = doTaskWithPrecondition(ptData);
        assertThat(result.getProsessTaskData().getTaskType()).isEqualTo(TilJournalføringTask.TASKNAME);
        assertThat(result.getSaksnummer()).isPresent()
                .contains(saksnummer);
    }

    @Test
    public void test_doTask_svangerskapspenger_inntektsmelding() throws Exception {

        ProsessTaskData prosessTaskData = new ProsessTaskData(OpprettSakTask.TASKNAME);
        prosessTaskData.setSekvens("1");

        String filename = "testsoknader/inntektsmelding-svp.xml";
        Path path = Paths.get(getClass().getClassLoader().getResource(filename).toURI());
        String xml = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);

        MottakMeldingDataWrapper ptData = new MottakMeldingDataWrapper(prosessTaskData);
        ptData.setArkivId("123");
        ptData.setAktørId("1000104134079");
        ptData.setBehandlingTema(BehandlingTema.SVANGERSKAPSPENGER);
        ptData.setDokumentKategori(DokumentKategori.UDEFINERT);

        ptData.setDokumentTypeId(DokumentTypeId.INNTEKTSMELDING);
        final MottattStrukturertDokument<?> imlDto = MeldingXmlParser.unmarshallXml(xml);
        imlDto.kopierTilMottakWrapper(ptData, aktørConsumer::hentAktørIdForPersonIdent);

        String saksnummer = "789";
        SaksnummerDto saksnummerDto = new SaksnummerDto(saksnummer);
        when(fagsakRestKlient.opprettSak(any(OpprettSakDto.class))).thenReturn(saksnummerDto);

        MottakMeldingDataWrapper result = doTaskWithPrecondition(ptData);
        assertThat(result.getProsessTaskData().getTaskType()).isEqualTo(TilJournalføringTask.TASKNAME);
        assertThat(result.getSaksnummer()).isPresent()
                .contains(saksnummer);
    }

}
