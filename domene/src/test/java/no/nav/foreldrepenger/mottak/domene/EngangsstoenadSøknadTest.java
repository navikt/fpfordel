package no.nav.foreldrepenger.mottak.domene;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Optional;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.junit.Test;

import no.nav.foreldrepenger.fordel.kodeverk.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.mottak.felles.MottakMeldingDataWrapper;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.Bruker;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.FoedselEllerAdopsjon;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.OpplysningerOmBarn;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.SoeknadsskjemaEngangsstoenad;
import no.nav.foreldrepenger.soeknadsskjema.engangsstoenad.v1.Soknadsvalg;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumer;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;

public class EngangsstoenadSøknadTest {

    @Test
    public void skal_mappe_verdier_korrekt_til_data_wrapper() throws Exception {
        final SoeknadsskjemaEngangsstoenad soeknadsskjemaEngangsstoenad = new SoeknadsskjemaEngangsstoenad();
        final Bruker bruker = new Bruker();
        final String personidentifikator = "12341234";
        final String aktørId = "1234";
        bruker.setPersonidentifikator(personidentifikator);
        soeknadsskjemaEngangsstoenad.setBruker(bruker);
        final OpplysningerOmBarn opplysningerOmBarn = new OpplysningerOmBarn();
        opplysningerOmBarn.setAntallBarn(1);

        final LocalDate fiveMonthsUntil = LocalDate.now().plusMonths(5);
        opplysningerOmBarn.setTermindato(getXmlGregorianCalendar(fiveMonthsUntil));
        soeknadsskjemaEngangsstoenad.setOpplysningerOmBarn(opplysningerOmBarn);
        final Soknadsvalg soknadsvalg = new Soknadsvalg();
        soknadsvalg.setFoedselEllerAdopsjon(FoedselEllerAdopsjon.FOEDSEL);
        soeknadsskjemaEngangsstoenad.setSoknadsvalg(soknadsvalg);
        final EngangsstønadSøknad soeknadXmlWrapper = (EngangsstønadSøknad) MottattStrukturertDokument.toXmlWrapper(soeknadsskjemaEngangsstoenad);
        KodeverkRepository repository = mock(KodeverkRepository.class);
        AktørConsumer aktørConsumer = mock(AktørConsumer.class);
        when(aktørConsumer.hentAktørIdForPersonIdent(personidentifikator)).thenReturn(Optional.of(aktørId));
        when(repository.finn(BehandlingTema.class, BehandlingTema.ENGANGSSTØNAD_FØDSEL.getKode())).thenReturn(BehandlingTema.ENGANGSSTØNAD_FØDSEL);
        final MottakMeldingDataWrapper test = new MottakMeldingDataWrapper(repository, new ProsessTaskData("TEST"));
        test.setBehandlingTema(BehandlingTema.ENGANGSSTØNAD_FØDSEL);

        soeknadXmlWrapper.kopierTilMottakWrapper(test, aktørConsumer::hentAktørIdForPersonIdent);

        assertThat(test.getAktørId()).hasValueSatisfying(it -> assertThat(it).isEqualTo(aktørId));
        assertThat(test.getBarnTermindato()).hasValueSatisfying(it -> assertThat(it).isEqualTo(fiveMonthsUntil));

        assertThat(test.getBarnFodselsdato()).isNotPresent();
        assertThat(test.getOmsorgsovertakelsedato()).isNotPresent();
    }

    @Test
    public void skal_hente_behandlingstemaKode_fra_soeknad_naar_fødsel() {
        Soknadsvalg soknadsvalg = new Soknadsvalg();
        soknadsvalg.setFoedselEllerAdopsjon(FoedselEllerAdopsjon.FOEDSEL);
        SoeknadsskjemaEngangsstoenad soeknad = new SoeknadsskjemaEngangsstoenad();
        soeknad.setSoknadsvalg(soknadsvalg);

        EngangsstønadSøknad soeknadXmlWrapper = (EngangsstønadSøknad) MottattStrukturertDokument.toXmlWrapper(soeknad);

        assertThat(soeknadXmlWrapper.hentBehandlingTema()).isEqualTo(BehandlingTema.ENGANGSSTØNAD_FØDSEL);
    }

    @Test
    public void skal_hente_behandlingstemaKode_fra_soeknad_naar_adopsjon() {
        Soknadsvalg soknadsvalg = new Soknadsvalg();
        soknadsvalg.setFoedselEllerAdopsjon(FoedselEllerAdopsjon.ADOPSJON);
        SoeknadsskjemaEngangsstoenad soeknad = new SoeknadsskjemaEngangsstoenad();
        soeknad.setSoknadsvalg(soknadsvalg);

        EngangsstønadSøknad soeknadXmlWrapper = (EngangsstønadSøknad) MottattStrukturertDokument.toXmlWrapper(soeknad);

        assertThat(soeknadXmlWrapper.hentBehandlingTema()).isEqualTo(BehandlingTema.ENGANGSSTØNAD_ADOPSJON);
    }

    @Test
    public void skal_hente_behandlingstemaKode_fra_soeknad_naar_mangler() {
        Soknadsvalg soknadsvalg = new Soknadsvalg();
        // ikke noen FoedselEllerAdopsjon
        SoeknadsskjemaEngangsstoenad soeknad = new SoeknadsskjemaEngangsstoenad();
        soeknad.setSoknadsvalg(soknadsvalg);

        EngangsstønadSøknad soeknadXmlWrapper = (EngangsstønadSøknad) MottattStrukturertDokument.toXmlWrapper(soeknad);

        assertThat(soeknadXmlWrapper.hentBehandlingTema()).isEqualTo(BehandlingTema.UDEFINERT);
    }


    private XMLGregorianCalendar getXmlGregorianCalendar(LocalDate now) throws DatatypeConfigurationException {
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(Date.from(now.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        return DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
    }

}
