package no.nav.foreldrepenger.mottak.infotrygd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPFactory;
import javax.xml.ws.soap.SOAPFaultException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import no.nav.tjeneste.virksomhet.infotrygdsak.v1.binding.FinnSakListePersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.infotrygdsak.v1.binding.FinnSakListeSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.infotrygdsak.v1.binding.FinnSakListeUgyldigInput;
import no.nav.tjeneste.virksomhet.infotrygdsak.v1.informasjon.Behandlingstema;
import no.nav.tjeneste.virksomhet.infotrygdsak.v1.informasjon.Resultat;
import no.nav.tjeneste.virksomhet.infotrygdsak.v1.informasjon.Sakstyper;
import no.nav.tjeneste.virksomhet.infotrygdsak.v1.informasjon.Status;
import no.nav.tjeneste.virksomhet.infotrygdsak.v1.informasjon.Tema;
import no.nav.tjeneste.virksomhet.infotrygdsak.v1.meldinger.FinnSakListeResponse;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.integrasjon.felles.ws.DateUtil;
import no.nav.vedtak.felles.integrasjon.felles.ws.SoapWebServiceFeil;
import no.nav.vedtak.felles.integrasjon.infotrygdsak.InfotrygdSakConsumer;

public class InfotrygdTjenesteImplTest {
    private static final String FNR_SØKER = "99999999999";
    private static final String FNR_EKTEFELLE = "99999999999";
    private static final LocalDate NOW = LocalDate.now();
    private static final LocalDate DATE_20170823 = NOW.minusMonths(4).plusDays(17);
    private static final LocalDate DATE_20170401 = NOW.minusMonths(8).minusDays(5);
    private static final LocalDate DATE_20171017 = NOW.minusMonths(2).plusDays(11);
    private static final LocalDate DATE_20180101 = NOW.plusDays(25);
    private static final String FORELDREPENGER = "Foreldrepenger";
    private static final String SAKSBEHANDLER_ID = "Saksbehandler";

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();
    @Mock
    private InfotrygdSakConsumer infotrygdSakConsumer;

    private InfotrygdTjeneste tjeneste;

    @Before
    public void setup() {
        tjeneste = new InfotrygdTjenesteImpl(infotrygdSakConsumer);
    }

    @Test
    public void skal_finne_sakliste() throws Exception {
        mockSakListeForSøker();

        final List<InfotrygdSak> saksliste = tjeneste.finnSakListe(FNR_SØKER, NOW);

        assertThat(saksliste).isNotEmpty();
        assertThat(saksliste.get(0).getTema()).isEqualTo("FA");
    }

    @Test
    public void skal_finne_sakliste_eksempel_fra_Q11() throws Exception {
        // Arrange
        FinnSakListeResponse response = new FinnSakListeResponse();
        response.getSakListe().add(opprettInfotrygdSak());
        response.getVedtakListe().add(opprettInfotrygdVedtak());
        when(infotrygdSakConsumer.finnSakListe(any())).thenReturn(response);

        // Act
        final List<InfotrygdSak> saksliste = tjeneste.finnSakListe(FNR_EKTEFELLE, NOW);

        // Assert
        assertThat(saksliste).hasSize(2);
        final InfotrygdSak infotrygdSak = saksliste.get(0);
        assertThat(infotrygdSak.getSakId()).isEqualTo("42");
        assertThat(infotrygdSak.getTema()).isNull();
        assertThat(infotrygdSak.getBehandlingsTema()).isEqualTo("FØ");
        final InfotrygdSak infotrygdSak2 = saksliste.get(1);
        assertThat(infotrygdSak2.getTema()).isEqualTo("FA");
        assertThat(infotrygdSak2.getBehandlingsTema()).isNull();
    }

    @Test
    public void skal_finne_sakliste_for_annen_forelder() throws Exception {
        mockSakListeForEktefelle();

        final List<InfotrygdSak> saksliste = tjeneste.finnSakListe(FNR_EKTEFELLE, NOW);

        assertThat(saksliste).isNotEmpty();
        assertThat(saksliste.get(0).getTema()).isEqualTo("FA");
    }

    @Test(expected = TekniskException.class)
    public void skal_kaste_nedetid() throws Exception {

        when(infotrygdSakConsumer.finnSakListe(any())).thenThrow(SoapWebServiceFeil.FACTORY.soapFaultIwebserviceKall("InfotrygdSakV1",
                new SOAPFaultException(SOAPFactory.newInstance().createFault("Basene i Infotrygd er ikke tilgjengelige \n at bla bla", new QName("abs")))).toException());

        tjeneste.finnSakListe(FNR_EKTEFELLE, NOW);
    }

    private void mockSakListeForSøker() throws FinnSakListePersonIkkeFunnet, FinnSakListeSikkerhetsbegrensning, FinnSakListeUgyldigInput, DatatypeConfigurationException  {
        FinnSakListeResponse response = new FinnSakListeResponse();
        response.getSakListe().add(opprettInfotrygdSak("FE"));
        when(infotrygdSakConsumer.finnSakListe(any())).thenReturn(response);
    }

    private void mockSakListeForEktefelle() throws FinnSakListePersonIkkeFunnet, FinnSakListeSikkerhetsbegrensning, FinnSakListeUgyldigInput, DatatypeConfigurationException  {
        FinnSakListeResponse response = new FinnSakListeResponse();
        response.getSakListe().add(opprettInfotrygdSak("FE"));
        response.getSakListe().add(opprettInfotrygdSak("AE"));
        when(infotrygdSakConsumer.finnSakListe(any())).thenReturn(response);
    }

    private no.nav.tjeneste.virksomhet.infotrygdsak.v1.informasjon.InfotrygdSak opprettInfotrygdSak(String relatertYtelseBehandlingstema) throws DatatypeConfigurationException {
        no.nav.tjeneste.virksomhet.infotrygdsak.v1.informasjon.InfotrygdSak sak = new no.nav.tjeneste.virksomhet.infotrygdsak.v1.informasjon.InfotrygdSak();
        sak.setTema(lagTema());
        sak.setType(lagSakstyper());
        sak.setStatus(lagStatus());
        sak.setRegistrert(DateUtil.convertToXMLGregorianCalendar(DATE_20170823));
        Behandlingstema behandlingstema = new Behandlingstema();
        behandlingstema.setValue(relatertYtelseBehandlingstema);
        sak.setBehandlingstema(behandlingstema);
        return sak;
    }

    private no.nav.tjeneste.virksomhet.infotrygdsak.v1.informasjon.InfotrygdSak opprettInfotrygdSak() throws DatatypeConfigurationException {
        no.nav.tjeneste.virksomhet.infotrygdsak.v1.informasjon.InfotrygdSak sak = new no.nav.tjeneste.virksomhet.infotrygdsak.v1.informasjon.InfotrygdSak();
        sak.setSakId("42");
        sak.setRegistrert(DateUtil.convertToXMLGregorianCalendar(DATE_20170823));
        sak.setBehandlingstema(lagBehandlingstema());
        sak.setType(lagSakstyper());
        sak.setStatus(lagStatus());
        sak.setSaksbehandlerId(SAKSBEHANDLER_ID);
        sak.setVedtatt(DateUtil.convertToXMLGregorianCalendar(DATE_20170401));
        sak.setIverksatt(DateUtil.convertToXMLGregorianCalendar(DATE_20171017));
        return sak;
    }

    private no.nav.tjeneste.virksomhet.infotrygdsak.v1.informasjon.InfotrygdVedtak opprettInfotrygdVedtak() throws DatatypeConfigurationException {
        no.nav.tjeneste.virksomhet.infotrygdsak.v1.informasjon.InfotrygdVedtak vedtak = new no.nav.tjeneste.virksomhet.infotrygdsak.v1.informasjon.InfotrygdVedtak();
        vedtak.setSakId("66");
        vedtak.setRegistrert(DateUtil.convertToXMLGregorianCalendar(DATE_20170823));
        vedtak.setTema(lagTema());
        vedtak.setResultat(lagResultat());
        vedtak.setSaksbehandlerId(SAKSBEHANDLER_ID);
        vedtak.setVedtatt(DateUtil.convertToXMLGregorianCalendar(DATE_20170401));
        vedtak.setIverksatt(DateUtil.convertToXMLGregorianCalendar(DATE_20171017));
        vedtak.setOpphoerFom(DateUtil.convertToXMLGregorianCalendar(DATE_20180101));
        return vedtak;
    }

    private Resultat lagResultat() {
        final Resultat resultat = new Resultat();
        resultat.setValue("Vedtatt");
        return resultat;
    }

    private Sakstyper lagSakstyper() {
        Sakstyper value = new Sakstyper();
        value.setValue(FORELDREPENGER);
        return value;
    }

    private Status lagStatus() {
        Status status = new Status();
        status.setKodeRef("L");
        status.setTermnavn("L");
        status.setValue("L");
        return status;
    }

    private Behandlingstema lagBehandlingstema() {
        Behandlingstema behandlingstema = new Behandlingstema();
        behandlingstema.setValue("FØ");
        behandlingstema.setKodeRef("FØ");
        behandlingstema.setTermnavn("FØ");
        return behandlingstema;
    }

    private Tema lagTema() {
        Tema tema = new Tema();
        tema.setValue("FA");
        tema.setKodeRef("FA");
        tema.setTermnavn("FA");
        return tema;
    }
}
