package no.nav.foreldrepenger.mottak.gsak;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import no.nav.foreldrepenger.fordel.kodeverdi.Fagsystem;
import no.nav.foreldrepenger.fordel.kodeverdi.Tema;
import no.nav.tjeneste.virksomhet.sak.v1.informasjon.Fagomraader;
import no.nav.tjeneste.virksomhet.sak.v1.informasjon.Fagsystemer;
import no.nav.tjeneste.virksomhet.sak.v1.informasjon.Sak;
import no.nav.tjeneste.virksomhet.sak.v1.meldinger.FinnSakResponse;
import no.nav.vedtak.felles.integrasjon.sak.SakConsumer;

public class GsakSakOversetterTest {

    private GsakSakTjeneste oversetter; // objekt vi tester
    private SakConsumer sakConsumer = mock(SakConsumer.class);

    @Before
    public void setup() {
        var restklient = mock(SakRestKlient.class);
        oversetter = new GsakSakTjeneste(sakConsumer, null, restklient);
    }

    @Test
    public void test_tilListInfotrygdSak() throws Exception {

        final String fnr = "99999999999";

        final Fagsystem fagsystemInfotrygdKV = Fagsystem.INFOTRYGD;
        final Fagsystemer fagsystemInfotrygd = new Fagsystemer();
        fagsystemInfotrygd.setValue(fagsystemInfotrygdKV.getKode());

        Sak sak1 = new Sak();
        sak1.setSakId("id1");
        Fagomraader fagomraade1 = new Fagomraader();
        Tema forOgSvangTema = Tema.FORELDRE_OG_SVANGERSKAPSPENGER;
        fagomraade1.setValue(forOgSvangTema.getOffisiellKode());
        sak1.setFagomraade(fagomraade1);
        sak1.setFagsystem(fagsystemInfotrygd);


        Sak sak2 = new Sak();
        sak2.setSakId("id2");
        Fagomraader fagomraade2 = new Fagomraader();
        fagomraade2.setValue("boink!");
        sak2.setFagomraade(fagomraade2);
        sak2.setFagsystem(fagsystemInfotrygd);


        List<Sak> saker = Arrays.asList(sak1, sak2);
        FinnSakResponse response = new FinnSakResponse();
        response.getSakListe().addAll(saker);

        when(sakConsumer.finnSak(any())).thenReturn(response);

        List<GsakSak> gsakSaker = oversetter.finnSaker(fnr);

        assertThat(gsakSaker.size()).isEqualTo(2);

        GsakSak gsakSak1 = gsakSaker.get(0);
        assertThat(gsakSak1.getBrukerFnr()).isEqualTo(fnr);
        assertThat(gsakSak1.getSakId()).isEqualTo("id1");
        assertThat(gsakSak1.getTema()).isEqualTo(Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
        assertThat(gsakSak1.getFagsystem()).isEqualTo(Fagsystem.INFOTRYGD);

        GsakSak gsakSak2 = gsakSaker.get(1);
        assertThat(gsakSak2.getBrukerFnr()).isEqualTo(fnr);
        assertThat(gsakSak2.getSakId()).isEqualTo("id2");
        assertThat(gsakSak2.getTema()).isEqualTo(Tema.UDEFINERT);
        assertThat(gsakSak2.getFagsystem()).isEqualTo(Fagsystem.INFOTRYGD);
    }
}
