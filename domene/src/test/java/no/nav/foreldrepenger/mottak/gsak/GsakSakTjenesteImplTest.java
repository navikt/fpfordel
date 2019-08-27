package no.nav.foreldrepenger.mottak.gsak;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import no.nav.foreldrepenger.mottak.gsak.api.GsakSak;
import no.nav.foreldrepenger.mottak.gsak.api.GsakSakAdapter;

public class GsakSakTjenesteImplTest {

    private GsakSakTjenesteImpl tjeneste; // objektet vi tester
    GsakSakAdapter mockAdapter;

    @Before
    public void setup() {
        mockAdapter = mock(GsakSakAdapter.class);
        tjeneste = new GsakSakTjenesteImpl(mockAdapter);
    }

    @Test
    public void test_finnInfotrygdSakerForBruker() {
        when(mockAdapter.finnSaker("hoi")).thenReturn(new ArrayList<GsakSak>());

        List<GsakSak> saker = tjeneste.finnSaker("hoi");

        assertThat(saker).isNotNull();
    }
}
