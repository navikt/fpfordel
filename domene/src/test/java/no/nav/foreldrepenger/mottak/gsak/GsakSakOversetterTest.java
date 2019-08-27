package no.nav.foreldrepenger.mottak.gsak;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import no.nav.foreldrepenger.fordel.dbstoette.UnittestRepositoryRule;
import no.nav.foreldrepenger.fordel.kodeverk.Fagsystem;
import no.nav.foreldrepenger.fordel.kodeverk.KodeverkRepository;
import no.nav.foreldrepenger.fordel.kodeverk.KodeverkRepositoryImpl;
import no.nav.foreldrepenger.fordel.kodeverk.Tema;
import no.nav.foreldrepenger.mottak.gsak.api.GsakSak;
import no.nav.tjeneste.virksomhet.sak.v1.informasjon.Fagomraader;
import no.nav.tjeneste.virksomhet.sak.v1.informasjon.Fagsystemer;
import no.nav.tjeneste.virksomhet.sak.v1.informasjon.Sak;

public class GsakSakOversetterTest {

    @Rule
    public UnittestRepositoryRule repoRule = new UnittestRepositoryRule();
    private KodeverkRepository kodeverkRepository = new KodeverkRepositoryImpl(repoRule.getEntityManager());

    private GsakSakTransformerer oversetter; // objekt vi tester

    @Before
    public void setup() {
        oversetter = new GsakSakTransformerer(kodeverkRepository);
    }

    @Test
    public void test_tilListInfotrygdSak() {

        final String fnr = "01020312345";

        final Fagsystem fagsystemInfotrygdKV = kodeverkRepository.finn(Fagsystem.class, Fagsystem.INFOTRYGD);
        final Fagsystemer fagsystemInfotrygd = new Fagsystemer();
        fagsystemInfotrygd.setValue(fagsystemInfotrygdKV.getOffisiellKode());

        Sak sak1 = new Sak();
        sak1.setSakId("id1");
        Fagomraader fagomraade1 = new Fagomraader();
        Tema forOgSvangTema = kodeverkRepository.finn(Tema.class, Tema.FORELDRE_OG_SVANGERSKAPSPENGER);
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

        List<GsakSak> gsakSaker = oversetter.transformer(fnr, saker);

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
