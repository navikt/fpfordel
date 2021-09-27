package no.nav.foreldrepenger.mottak.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
import no.nav.foreldrepenger.mottak.klient.Fagsak;
import no.nav.foreldrepenger.mottak.person.PersonInformasjon;
import no.nav.foreldrepenger.mottak.task.joark.HentDataFraJoarkTask;
import no.nav.foreldrepenger.mottak.task.xml.MeldingXmlParser;
import no.nav.foreldrepenger.mottak.tjeneste.DestinasjonsRuter;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
class OpprettSakTaskTest {

    public static final String FNR = "99999999999";
    public static final String AKTØR_ID = "9000000000009";

    @Mock
    private Fagsak fagsakRestKlient;
    @Mock
    private PersonInformasjon aktørConsumer;

    private DestinasjonsRuter vurderVLSaker;

    @BeforeEach
    void setUp() {
        vurderVLSaker = new DestinasjonsRuter(null, fagsakRestKlient);
    }

    @Test
    void test_doTask_fødsel_strukturert() throws Exception {

        var prosessTaskData = ProsessTaskData.forProsessTaskHandler(HentDataFraJoarkTask.class);
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

        assertThat(vurderVLSaker.opprettSak(ptData)).isEqualTo(saksnummer);
    }

    @Test
    void test_doTask_fødsel_ustrukturert() {
        var prosessTaskData = ProsessTaskData.forProsessTaskHandler(HentDataFraJoarkTask.class);
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

        assertThat(vurderVLSaker.opprettSak(ptData)).isEqualTo(saksnummer);
    }

    @Test
    void test_doTask_anke_klage() {
        var innData = ProsessTaskData.forProsessTaskHandler(HentDataFraJoarkTask.class);
        innData.setSekvens("1");

        var ptData = new MottakMeldingDataWrapper(innData);

        ptData.setArkivId("123");
        ptData.setAktørId(AKTØR_ID);
        ptData.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_FØDSEL);
        ptData.setDokumentTypeId(DokumentTypeId.KLAGE_DOKUMENT);
        ptData.setDokumentKategori(DokumentKategori.KLAGE_ELLER_ANKE);

        assertThrows(IllegalArgumentException.class, () -> vurderVLSaker.opprettSak(ptData));
    }

    @Test
    void test_doTask_svangerskapspenger_søknad() throws Exception {

        var prosessTaskData = ProsessTaskData.forProsessTaskHandler(HentDataFraJoarkTask.class);
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
        var soeknadDTO = MeldingXmlParser.unmarshallXml(xml);
        soeknadDTO.kopierTilMottakWrapper(ptData, aktørConsumer::hentAktørIdForPersonIdent);

        String saksnummer = "789";
        var saksnummerDto = new SaksnummerDto(saksnummer);
        when(fagsakRestKlient.opprettSak(any(OpprettSakDto.class))).thenReturn(saksnummerDto);

        assertThat(vurderVLSaker.opprettSak(ptData)).isEqualTo(saksnummer);
    }

    @Test
    void test_doTask_svangerskapspenger_inntektsmelding() throws Exception {

        var prosessTaskData = ProsessTaskData.forProsessTaskHandler(HentDataFraJoarkTask.class);
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

        assertThat(vurderVLSaker.opprettSak(ptData)).isEqualTo(saksnummer);
    }

}
