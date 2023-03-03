package no.nav.foreldrepenger.mottak.domene;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.mottak.domene.v3.Søknad;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.mottak.person.PersonInformasjon;
import no.nav.vedtak.exception.FunksjonellException;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.TaskType;
import no.nav.vedtak.felles.xml.soeknad.endringssoeknad.v3.Endringssoeknad;
import no.nav.vedtak.felles.xml.soeknad.engangsstoenad.v3.Engangsstønad;
import no.nav.vedtak.felles.xml.soeknad.felles.v3.Adopsjon;
import no.nav.vedtak.felles.xml.soeknad.felles.v3.Bruker;
import no.nav.vedtak.felles.xml.soeknad.felles.v3.Foedsel;
import no.nav.vedtak.felles.xml.soeknad.felles.v3.Omsorgsovertakelse;
import no.nav.vedtak.felles.xml.soeknad.felles.v3.Termin;
import no.nav.vedtak.felles.xml.soeknad.felles.v3.Ytelse;
import no.nav.vedtak.felles.xml.soeknad.foreldrepenger.v3.Foreldrepenger;
import no.nav.vedtak.felles.xml.soeknad.kodeverk.v3.Overfoeringsaarsaker;
import no.nav.vedtak.felles.xml.soeknad.uttak.v3.Fordeling;
import no.nav.vedtak.felles.xml.soeknad.v3.OmYtelse;
import no.nav.vedtak.felles.xml.soeknad.v3.Soeknad;

@ExtendWith(MockitoExtension.class)
class SøknadTest {

    private static String AKTØR_ID = "9000000000009";
    private static String SAKSNUMMER = "98765433";

    Soeknad søknad;
    Bruker bruker;
    @Mock
    PersonInformasjon aktørConsumer;
    MottakMeldingDataWrapper test;
    Søknad søknadXmlWrapper;

    private static OmYtelse mapOmYtelse(Ytelse ytelse) {
        OmYtelse omYtelse = new OmYtelse();
        omYtelse.getAny().add(ytelse);
        return omYtelse;
    }

    @BeforeEach
    void init() {
        søknad = new Soeknad();
        bruker = new Bruker();
        bruker.setAktoerId(AKTØR_ID);
        søknad.setSoeker(bruker);
        søknad.setMottattDato(LocalDate.of(2018, 3, 8));
        test = new MottakMeldingDataWrapper(ProsessTaskData.forTaskType(new TaskType("TEST")));
        test.setAktørId(AKTØR_ID);
        søknadXmlWrapper = (Søknad) MottattStrukturertDokument.toXmlWrapper(søknad);
    }

    @Test
    void skal_sjekke_engangs_søknad_fødsel() {
        var søkersRelasjonTilBarnet = new Foedsel();
        søkersRelasjonTilBarnet.setAntallBarn(1);
        var engangsstønad = new Engangsstønad();
        engangsstønad.setSoekersRelasjonTilBarnet(søkersRelasjonTilBarnet);
        søknad.setOmYtelse(mapOmYtelse(engangsstønad));
        test.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_FØDSEL);
        søknadXmlWrapper.kopierTilMottakWrapper(test, aktørConsumer::hentAktørIdForPersonIdent);
        assertThat(test.getAktørId()).contains(AKTØR_ID);

    }

    @Test
    void skal_sjekke_engangs_søknad_termin() {
        var søkersRelasjonTilBarnet = new Termin();
        søkersRelasjonTilBarnet.setAntallBarn(1);
        var engangsstønad = new Engangsstønad();
        engangsstønad.setSoekersRelasjonTilBarnet(søkersRelasjonTilBarnet);
        søknad.setOmYtelse(mapOmYtelse(engangsstønad));
        test.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_FØDSEL);
        søknadXmlWrapper.kopierTilMottakWrapper(test, aktørConsumer::hentAktørIdForPersonIdent);
        assertThat(test.getAktørId()).contains(AKTØR_ID);
    }

    @Test
    void skal_sjekke_engangs_søknad_adopsjon() {
        var soekersRelasjonTilBarnet = new Adopsjon();
        soekersRelasjonTilBarnet.setAntallBarn(1);
        var engangsstønad = new Engangsstønad();
        engangsstønad.setSoekersRelasjonTilBarnet(soekersRelasjonTilBarnet);
        søknad.setOmYtelse(mapOmYtelse(engangsstønad));
        test.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_ADOPSJON);
        søknadXmlWrapper.kopierTilMottakWrapper(test, aktørConsumer::hentAktørIdForPersonIdent);
        assertThat(test.getAktørId()).contains(AKTØR_ID);
    }

    @Test
    void skal_sjekke_engangs_søknad_omsorgovertakelse() {
        var soekersRelasjonTilBarnet = new Omsorgsovertakelse();
        soekersRelasjonTilBarnet.setAntallBarn(1);
        var engangsstønad = new Engangsstønad();
        engangsstønad.setSoekersRelasjonTilBarnet(soekersRelasjonTilBarnet);
        søknad.setOmYtelse(mapOmYtelse(engangsstønad));
        test.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_ADOPSJON);
        søknadXmlWrapper.kopierTilMottakWrapper(test, aktørConsumer::hentAktørIdForPersonIdent);
        assertThat(test.getAktørId()).contains(AKTØR_ID);
    }

    @Test
    void skal_sjekke_engangs_søknad() {
        søknad.setOmYtelse(mapOmYtelse(new Engangsstønad()));
        test.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD);
        søknadXmlWrapper.kopierTilMottakWrapper(test, aktørConsumer::hentAktørIdForPersonIdent);
        assertThat(test.getAktørId()).contains(AKTØR_ID);

    }

    @Test
    void skal_sjekke_foreldrepenger_søknad_fødsel() {
        var søkersRelasjonTilBarnet = new Foedsel();
        søkersRelasjonTilBarnet.setAntallBarn(1);
        var foreldrepenger = new Foreldrepenger();
        foreldrepenger.setRelasjonTilBarnet(søkersRelasjonTilBarnet);
        søknad.setOmYtelse(mapOmYtelse(foreldrepenger));
        test.setBehandlingTema(BehandlingTema.FORELDREPENGER_FØDSEL);
        søknadXmlWrapper.kopierTilMottakWrapper(test, aktørConsumer::hentAktørIdForPersonIdent);
        assertThat(test.getAktørId()).contains(AKTØR_ID);
    }

    @Test
    void skal_sjekke_foreldrepenger_søknad_termin() {
        var søkersRelasjonTilBarnet = new Termin();
        søkersRelasjonTilBarnet.setAntallBarn(1);
        var foreldrepenger = new Foreldrepenger();
        foreldrepenger.setRelasjonTilBarnet(søkersRelasjonTilBarnet);
        søknad.setOmYtelse(mapOmYtelse(foreldrepenger));
        test.setBehandlingTema(BehandlingTema.FORELDREPENGER_FØDSEL);
        søknadXmlWrapper.kopierTilMottakWrapper(test, aktørConsumer::hentAktørIdForPersonIdent);
        assertThat(test.getAktørId()).contains(AKTØR_ID);
    }

    @Test
    void skal_sjekke_foreldrepenger_søknad_adopsjon() {
        var søkersRelasjonTilBarnet = new Adopsjon();
        søkersRelasjonTilBarnet.setAntallBarn(1);
        var foreldrepenger = new Foreldrepenger();
        foreldrepenger.setRelasjonTilBarnet(søkersRelasjonTilBarnet);
        søknad.setOmYtelse(mapOmYtelse(foreldrepenger));
        test.setBehandlingTema(BehandlingTema.FORELDREPENGER_ADOPSJON);
        søknadXmlWrapper.kopierTilMottakWrapper(test, aktørConsumer::hentAktørIdForPersonIdent);
        assertThat(test.getAktørId()).contains(AKTØR_ID);
    }

    @Test
    void skal_sjekke_foreldrepenger_søknad_omsorgovertakelse() {
        var søkersRelasjonTilBarnet = new Omsorgsovertakelse();
        søkersRelasjonTilBarnet.setAntallBarn(1);
        var foreldrepenger = new Foreldrepenger();
        foreldrepenger.setRelasjonTilBarnet(søkersRelasjonTilBarnet);
        søknad.setOmYtelse(mapOmYtelse(foreldrepenger));
        test.setBehandlingTema(BehandlingTema.FORELDREPENGER_ADOPSJON);
        søknadXmlWrapper.kopierTilMottakWrapper(test, aktørConsumer::hentAktørIdForPersonIdent);
        assertThat(test.getAktørId()).contains(AKTØR_ID);
    }

    @Test
    @DisplayName("Tester at exception kastes om tema i tynnmelding og søknadsdokment er forskjellige")
    void ulikeTema() {

        var søkersRelasjonTilBarnet = new Omsorgsovertakelse();
        søkersRelasjonTilBarnet.setAntallBarn(1);
        Foreldrepenger foreldrepenger = new Foreldrepenger();
        foreldrepenger.setRelasjonTilBarnet(søkersRelasjonTilBarnet);
        søknad.setOmYtelse(mapOmYtelse(foreldrepenger));
        test.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_ADOPSJON);
        assertThatThrownBy(() -> søknadXmlWrapper.kopierTilMottakWrapper(test, aktørConsumer::hentAktørIdForPersonIdent)).isInstanceOf(
            TekniskException.class).hasMessageContaining("FP-404782");
    }

    @Test
    void skal_sjekke_foreldrepenger_endringssøknad() {
        var fordeling = new Fordeling();
        fordeling.setAnnenForelderErInformert(false);
        fordeling.setOenskerKvoteOverfoert(new Overfoeringsaarsaker());
        var endringssoeknad = new Endringssoeknad();
        endringssoeknad.setSaksnummer(SAKSNUMMER);
        endringssoeknad.setFordeling(fordeling);
        søknad.setOmYtelse(mapOmYtelse(endringssoeknad));
        test.setSaksnummer(SAKSNUMMER);
        test.setBehandlingTema(BehandlingTema.FORELDREPENGER);
        søknadXmlWrapper.kopierTilMottakWrapper(test, aktørConsumer::hentAktørIdForPersonIdent);
        assertThat(test.getSaksnummer()).hasValue(SAKSNUMMER);
    }

    @Test
    void skal_teste_validering_brukerId() {
        var søkersRelasjonTilBarnet = new Foedsel();
        søkersRelasjonTilBarnet.setAntallBarn(1);
        var foreldrepenger = new Foreldrepenger();
        foreldrepenger.setRelasjonTilBarnet(søkersRelasjonTilBarnet);
        søknad.setOmYtelse(mapOmYtelse(foreldrepenger));
        test.setAktørId("95873742"); // simuler annen aktørId fra metadata
        test.setBehandlingTema(BehandlingTema.FORELDREPENGER_FØDSEL);
        assertTrue(assertThrows(TekniskException.class,
            () -> søknadXmlWrapper.kopierTilMottakWrapper(test, aktørConsumer::hentAktørIdForPersonIdent)).getMessage().contains("FP-502574"));
    }

    @Test
    void skal_teste_validering_saksnummer() {
        var fordeling = new Fordeling();
        fordeling.setAnnenForelderErInformert(false);
        fordeling.setOenskerKvoteOverfoert(new Overfoeringsaarsaker());
        var endringssøknad = new Endringssoeknad();
        endringssøknad.setSaksnummer(SAKSNUMMER);
        endringssøknad.setFordeling(fordeling);
        søknad.setOmYtelse(mapOmYtelse(endringssøknad));

        test.setSaksnummer("857356"); // saksnummer fra metadata
        test.setBehandlingTema(BehandlingTema.FORELDREPENGER);

        assertTrue(assertThrows(FunksjonellException.class,
            () -> søknadXmlWrapper.kopierTilMottakWrapper(test, aktørConsumer::hentAktørIdForPersonIdent)).getMessage().contains("FP-401245"));
    }

    @Test
    void skal_sjekke_udefinert_søknad() {
        søknad.setOmYtelse(mapOmYtelse(null));
        test.setBehandlingTema(BehandlingTema.UDEFINERT);
        søknadXmlWrapper.kopierTilMottakWrapper(test, aktørConsumer::hentAktørIdForPersonIdent);
        assertThat(test.getAktørId()).contains(AKTØR_ID);
    }
}
