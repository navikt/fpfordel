package no.nav.foreldrepenger.mottak.domene;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.mottak.domene.v3.Søknad;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.vedtak.exception.FunksjonellException;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumerMedCache;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.xml.soeknad.endringssoeknad.v3.Endringssoeknad;
import no.nav.vedtak.felles.xml.soeknad.engangsstoenad.v3.Engangsstønad;
import no.nav.vedtak.felles.xml.soeknad.felles.v3.Adopsjon;
import no.nav.vedtak.felles.xml.soeknad.felles.v3.Bruker;
import no.nav.vedtak.felles.xml.soeknad.felles.v3.Foedsel;
import no.nav.vedtak.felles.xml.soeknad.felles.v3.Omsorgsovertakelse;
import no.nav.vedtak.felles.xml.soeknad.felles.v3.SoekersRelasjonTilBarnet;
import no.nav.vedtak.felles.xml.soeknad.felles.v3.Termin;
import no.nav.vedtak.felles.xml.soeknad.felles.v3.Ytelse;
import no.nav.vedtak.felles.xml.soeknad.foreldrepenger.v3.Foreldrepenger;
import no.nav.vedtak.felles.xml.soeknad.kodeverk.v3.Overfoeringsaarsaker;
import no.nav.vedtak.felles.xml.soeknad.uttak.v3.Fordeling;
import no.nav.vedtak.felles.xml.soeknad.v3.OmYtelse;
import no.nav.vedtak.felles.xml.soeknad.v3.Soeknad;

public class SøknadTest {

    private static String AKTØR_ID = "9000000000009";
    private static String SAKSNUMMER = "98765433";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    Soeknad søknad;
    Bruker bruker;
    AktørConsumerMedCache aktørConsumer;
    MottakMeldingDataWrapper test;
    Søknad søknadXmlWrapper;

    @Before
    public void init() {
        søknad = new Soeknad();
        bruker = new Bruker();
        bruker.setAktoerId(AKTØR_ID);
        søknad.setSoeker(bruker);
        søknad.setMottattDato(LocalDate.of(2018, 3, 8));
        aktørConsumer = mock(AktørConsumerMedCache.class);
        test = new MottakMeldingDataWrapper(new ProsessTaskData("TEST"));
        test.setAktørId(AKTØR_ID);
        søknadXmlWrapper = (Søknad) MottattStrukturertDokument.toXmlWrapper(søknad);
    }

    private OmYtelse mapOmYtelse(Ytelse ytelse) {
        OmYtelse omYtelse = new OmYtelse();
        omYtelse.getAny().add(ytelse);
        return omYtelse;
    }

    @Test
    public void skal_sjekke_engangs_søknad_fødsel() {
        final SoekersRelasjonTilBarnet søkersRelasjonTilBarnet = new Foedsel();
        søkersRelasjonTilBarnet.setAntallBarn(1);
        final Engangsstønad engangsstønad = new Engangsstønad();
        engangsstønad.setSoekersRelasjonTilBarnet(søkersRelasjonTilBarnet);
        søknad.setOmYtelse(mapOmYtelse(engangsstønad));

        test.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_FØDSEL);
        søknadXmlWrapper.kopierTilMottakWrapper(test, aktørConsumer::hentAktørIdForPersonIdent);
        assertThat(test.getAktørId().get()).isEqualTo(AKTØR_ID);

    }
    
    
    @Test
    public void skal_sjekke_engangs_søknad_termin() {
        final SoekersRelasjonTilBarnet søkersRelasjonTilBarnet = new Termin();
        søkersRelasjonTilBarnet.setAntallBarn(1);
        final Engangsstønad engangsstønad = new Engangsstønad();
        engangsstønad.setSoekersRelasjonTilBarnet(søkersRelasjonTilBarnet);
        søknad.setOmYtelse(mapOmYtelse(engangsstønad));

        test.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_FØDSEL);
        søknadXmlWrapper.kopierTilMottakWrapper(test, aktørConsumer::hentAktørIdForPersonIdent);
        assertThat(test.getAktørId().get()).isEqualTo(AKTØR_ID);

    }

    @Test
    public void skal_sjekke_engangs_søknad_adopsjon() {
        final SoekersRelasjonTilBarnet soekersRelasjonTilBarnet = new Adopsjon();
        soekersRelasjonTilBarnet.setAntallBarn(1);
        final Engangsstønad engangsstønad = new Engangsstønad();
        engangsstønad.setSoekersRelasjonTilBarnet(soekersRelasjonTilBarnet);
        søknad.setOmYtelse(mapOmYtelse(engangsstønad));

        test.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_ADOPSJON);
        søknadXmlWrapper.kopierTilMottakWrapper(test, aktørConsumer::hentAktørIdForPersonIdent);
        assertThat(test.getAktørId().get()).isEqualTo(AKTØR_ID);
    }
    
    @Test
    public void skal_sjekke_engangs_søknad_omsorgovertakelse() {
        final SoekersRelasjonTilBarnet soekersRelasjonTilBarnet = new Omsorgsovertakelse();
        soekersRelasjonTilBarnet.setAntallBarn(1);
        final Engangsstønad engangsstønad = new Engangsstønad();
        engangsstønad.setSoekersRelasjonTilBarnet(soekersRelasjonTilBarnet);
        søknad.setOmYtelse(mapOmYtelse(engangsstønad));

        test.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_ADOPSJON);
        søknadXmlWrapper.kopierTilMottakWrapper(test, aktørConsumer::hentAktørIdForPersonIdent);
        assertThat(test.getAktørId().get()).isEqualTo(AKTØR_ID);
    }

    @Test
    public void skal_sjekke_engangs_søknad() {

        final Engangsstønad engangsstønad = new Engangsstønad();
        søknad.setOmYtelse(mapOmYtelse(engangsstønad));

        test.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD);
        søknadXmlWrapper.kopierTilMottakWrapper(test, aktørConsumer::hentAktørIdForPersonIdent);

        assertThat(test.getAktørId().get()).isEqualTo(AKTØR_ID);

    }


    @Test
    public void skal_sjekke_foreldrepenger_søknad_fødsel() {

        final SoekersRelasjonTilBarnet søkersRelasjonTilBarnet = new Foedsel();
        søkersRelasjonTilBarnet.setAntallBarn(1);
        final Foreldrepenger foreldrepenger = new Foreldrepenger();
        foreldrepenger.setRelasjonTilBarnet(søkersRelasjonTilBarnet);
        søknad.setOmYtelse(mapOmYtelse(foreldrepenger));

        test.setBehandlingTema(BehandlingTema.FORELDREPENGER_FØDSEL);
        søknadXmlWrapper.kopierTilMottakWrapper(test, aktørConsumer::hentAktørIdForPersonIdent);

        assertThat(test.getAktørId().get()).isEqualTo(AKTØR_ID);
    }
    
    @Test
    public void skal_sjekke_foreldrepenger_søknad_termin() {

        final SoekersRelasjonTilBarnet søkersRelasjonTilBarnet = new Termin();
        søkersRelasjonTilBarnet.setAntallBarn(1);
        final Foreldrepenger foreldrepenger = new Foreldrepenger();
        foreldrepenger.setRelasjonTilBarnet(søkersRelasjonTilBarnet);
        søknad.setOmYtelse(mapOmYtelse(foreldrepenger));

        test.setBehandlingTema(BehandlingTema.FORELDREPENGER_FØDSEL);
        søknadXmlWrapper.kopierTilMottakWrapper(test, aktørConsumer::hentAktørIdForPersonIdent);

        assertThat(test.getAktørId().get()).isEqualTo(AKTØR_ID);
    }
    
    @Test
    public void skal_sjekke_foreldrepenger_søknad_adopsjon() {

        final SoekersRelasjonTilBarnet søkersRelasjonTilBarnet = new Adopsjon();
        søkersRelasjonTilBarnet.setAntallBarn(1);
        final Foreldrepenger foreldrepenger = new Foreldrepenger();
        foreldrepenger.setRelasjonTilBarnet(søkersRelasjonTilBarnet);
        søknad.setOmYtelse(mapOmYtelse(foreldrepenger));

        test.setBehandlingTema(BehandlingTema.FORELDREPENGER_ADOPSJON);
        søknadXmlWrapper.kopierTilMottakWrapper(test, aktørConsumer::hentAktørIdForPersonIdent);

        assertThat(test.getAktørId().get()).isEqualTo(AKTØR_ID);
    }
    
    @Test
    public void skal_sjekke_foreldrepenger_søknad_omsorgovertakelse() {

        final SoekersRelasjonTilBarnet søkersRelasjonTilBarnet = new Omsorgsovertakelse();
        søkersRelasjonTilBarnet.setAntallBarn(1);
        final Foreldrepenger foreldrepenger = new Foreldrepenger();
        foreldrepenger.setRelasjonTilBarnet(søkersRelasjonTilBarnet);
        søknad.setOmYtelse(mapOmYtelse(foreldrepenger));

        test.setBehandlingTema(BehandlingTema.FORELDREPENGER_ADOPSJON);
        søknadXmlWrapper.kopierTilMottakWrapper(test, aktørConsumer::hentAktørIdForPersonIdent);

        assertThat(test.getAktørId().get()).isEqualTo(AKTØR_ID);
    }
    
    @Test
    public void skal_kaste_ulikBehandlingstemaKodeITynnMeldingOgSøknadsdokument() {

        final SoekersRelasjonTilBarnet søkersRelasjonTilBarnet = new Omsorgsovertakelse();
        søkersRelasjonTilBarnet.setAntallBarn(1);
        final Foreldrepenger foreldrepenger = new Foreldrepenger();
        foreldrepenger.setRelasjonTilBarnet(søkersRelasjonTilBarnet);
        søknad.setOmYtelse(mapOmYtelse(foreldrepenger));

        test.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_ADOPSJON);
        
        assertThatThrownBy(() -> søknadXmlWrapper.kopierTilMottakWrapper(test, aktørConsumer::hentAktørIdForPersonIdent))
            .isInstanceOf(TekniskException.class)
            .hasMessageContaining("FP-404782");
    }

    @Test
    public void skal_sjekke_foreldrepenger_endringssøknad() {
        Fordeling fordeling = new Fordeling();
        fordeling.setAnnenForelderErInformert(false);
        fordeling.setOenskerKvoteOverfoert(new Overfoeringsaarsaker());
        final Endringssoeknad endringssoeknad = new Endringssoeknad();
        endringssoeknad.setSaksnummer(SAKSNUMMER);
        endringssoeknad.setFordeling(fordeling);
        søknad.setOmYtelse(mapOmYtelse(endringssoeknad));

        test.setSaksnummer(SAKSNUMMER);
        test.setBehandlingTema(BehandlingTema.FORELDREPENGER);
        søknadXmlWrapper.kopierTilMottakWrapper(test, aktørConsumer::hentAktørIdForPersonIdent);

        assertThat(test.getSaksnummer()).hasValue(SAKSNUMMER);
    }

    @Test
    public void skal_teste_validering_brukerId() {
        final SoekersRelasjonTilBarnet søkersRelasjonTilBarnet = new Foedsel();
        søkersRelasjonTilBarnet.setAntallBarn(1);
        final Foreldrepenger foreldrepenger = new Foreldrepenger();
        foreldrepenger.setRelasjonTilBarnet(søkersRelasjonTilBarnet);
        søknad.setOmYtelse(mapOmYtelse(foreldrepenger));

        test.setAktørId("95873742"); // simuler annen aktørId fra metadata
        test.setBehandlingTema(BehandlingTema.FORELDREPENGER_FØDSEL);

        expectedException.expect(TekniskException.class);
        expectedException.expectMessage("FP-502574");

        søknadXmlWrapper.kopierTilMottakWrapper(test, aktørConsumer::hentAktørIdForPersonIdent);
    }

    @Test
    public void skal_teste_validering_saksnummer() {
        Fordeling fordeling = new Fordeling();
        fordeling.setAnnenForelderErInformert(false);
        fordeling.setOenskerKvoteOverfoert(new Overfoeringsaarsaker());
        final Endringssoeknad endringssøknad = new Endringssoeknad();
        endringssøknad.setSaksnummer(SAKSNUMMER);
        endringssøknad.setFordeling(fordeling);
        søknad.setOmYtelse(mapOmYtelse(endringssøknad));

        test.setSaksnummer("857356"); // saksnummer fra metadata
        test.setBehandlingTema(BehandlingTema.FORELDREPENGER);

        expectedException.expect(FunksjonellException.class);
        expectedException.expectMessage("FP-401245");

        søknadXmlWrapper.kopierTilMottakWrapper(test, aktørConsumer::hentAktørIdForPersonIdent);
    }

    @Test
    public void skal_sjekke_udefinert_søknad() {
        søknad.setOmYtelse(mapOmYtelse(null));

        test.setBehandlingTema(BehandlingTema.UDEFINERT);
        søknadXmlWrapper.kopierTilMottakWrapper(test, aktørConsumer::hentAktørIdForPersonIdent);

        assertThat(test.getAktørId().get()).isEqualTo(AKTØR_ID);

    }
}
