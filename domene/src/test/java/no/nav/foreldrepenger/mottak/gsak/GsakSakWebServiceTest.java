package no.nav.foreldrepenger.mottak.gsak;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import no.nav.foreldrepenger.fordel.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.fordel.kodeverk.Fagsystem;
import no.nav.foreldrepenger.fordel.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.fordel.kodeverk.KodeverkRepositoryImpl;
import no.nav.foreldrepenger.mottak.gsak.api.GsakSak;
import no.nav.tjeneste.virksomhet.sak.v1.binding.FinnSakForMangeForekomster;
import no.nav.tjeneste.virksomhet.sak.v1.binding.FinnSakUgyldigInput;
import no.nav.tjeneste.virksomhet.sak.v1.informasjon.Fagomraader;
import no.nav.tjeneste.virksomhet.sak.v1.informasjon.Fagsystemer;
import no.nav.tjeneste.virksomhet.sak.v1.informasjon.Sak;
import no.nav.tjeneste.virksomhet.sak.v1.meldinger.FinnSakRequest;
import no.nav.tjeneste.virksomhet.sak.v1.meldinger.FinnSakResponse;
import no.nav.vedtak.exception.VLException;
import no.nav.vedtak.felles.integrasjon.behandleoppgave.FagomradeKode;
import no.nav.vedtak.felles.integrasjon.sak.SakConsumer;
import no.nav.vedtak.felles.integrasjon.sak.SakSelftestConsumer;

public class GsakSakWebServiceTest {

    private GsakSakAdapterImpl service; // objektet vi tester

    private SakConsumer mockSakConsumer;
    private SakSelftestConsumer mockSakSelftestConsumer;
    private GsakSakTransformerer mockGsakSakTransformerer;

    private static final String BRUKER_FNR = "06016921295";
    
    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private KodeverkRepository kodeverkRepository = new KodeverkRepositoryImpl(repoRule.getEntityManager());


    @Before
    public void setup() {
        mockSakConsumer = mock(SakConsumer.class);
        mockSakSelftestConsumer = mock(SakSelftestConsumer.class);
        mockGsakSakTransformerer = new GsakSakTransformerer(kodeverkRepository);

        service = new GsakSakAdapterImpl(mockSakConsumer, mockSakSelftestConsumer, mockGsakSakTransformerer);
    }

    @Test
    public void test_ping() {
        service.ping();

        verify(mockSakSelftestConsumer).ping();
    }

    @Test
    public void test_finnInfotrygdSakerForBruker_ok() throws FinnSakUgyldigInput, FinnSakForMangeForekomster {

        Fagsystemer infotrygdSystem = new Fagsystemer();
        infotrygdSystem.setValue(Fagsystem.INFOTRYGD.getOffisiellKode());

        Sak sak1 = new Sak();
        sak1.setSakId("id1");
        Fagomraader fagomraade1 = new Fagomraader();
        fagomraade1.setValue(FagomradeKode.FOR.toString());
        sak1.setFagomraade(fagomraade1);
        sak1.setFagsystem(infotrygdSystem);

        Sak sak2 = new Sak();
        sak2.setSakId("id2");
        Fagomraader fagomraade2 = new Fagomraader();
        fagomraade2.setValue(FagomradeKode.UKJ.toString());
        sak2.setFagomraade(fagomraade2);
        sak2.setFagsystem(infotrygdSystem);

        FinnSakResponse response = new FinnSakResponse();
        response.getSakListe().add(sak1);
        response.getSakListe().add(sak2);

        ArgumentCaptor<FinnSakRequest> captor = ArgumentCaptor.forClass(FinnSakRequest.class);
        when(mockSakConsumer.finnSak(captor.capture())).thenReturn(response);

        List<GsakSak> gsakSaker = service.finnSaker(BRUKER_FNR);

        assertThat(gsakSaker.size()).isEqualTo(2);
        FinnSakRequest request = captor.getValue();
        assertThat(request.getBruker().getIdent()).isEqualTo(BRUKER_FNR);
    }

    @Test
    public void test_finnInfotrygdSakerForBruker_forMangeForekomster() throws FinnSakUgyldigInput, FinnSakForMangeForekomster {

        when(mockSakConsumer.finnSak(any(FinnSakRequest.class))).thenThrow(new FinnSakForMangeForekomster(null, null));

        try {
            service.finnSaker(BRUKER_FNR);
            fail("forventet exception");
        } catch (VLException e) {
            // ok
        }
    }

    @Test
    public void test_finnInfotrygdSakerForBruker_ugyldigInput() throws FinnSakUgyldigInput, FinnSakForMangeForekomster {

        when(mockSakConsumer.finnSak(any(FinnSakRequest.class))).thenThrow(new FinnSakUgyldigInput(null, null));

        try {
            service.finnSaker(BRUKER_FNR);
            fail("forventet exception");
        } catch (VLException e) {
            // ok
        }
    }
}
