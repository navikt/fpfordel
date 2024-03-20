package no.nav.foreldrepenger.mottak.behandlendeenhet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.foreldrepenger.mottak.klient.Los;
import no.nav.foreldrepenger.mottak.klient.TilhørendeEnhetDto;

@ExtendWith(MockitoExtension.class)
class LosEnheterCachedTjenesteTest {

    @Mock
    Los losKlient;

    LosEnheterCachedTjeneste tjeneste;

    @BeforeEach
    void setUp() {
        tjeneste = new LosEnheterCachedTjeneste(losKlient);
    }

    @Test
    void testHentingFraLos_svar_en_ehhet() {
        var ident = "X123456";
        var testEnhet = new TilhørendeEnhetDto("1234", "Test Enhet");
        when(losKlient.hentTilhørendeEnheter(ident)).thenReturn(List.of(testEnhet));

        var enheter = tjeneste.hentLosEnheterFor(ident);

        assertThat(enheter).isNotEmpty().hasSize(1).contains(testEnhet);
        verify(losKlient).hentTilhørendeEnheter(ident);
    }

    @Test
    void testHentingFraLos_svar_tomt_liste() {
        var ident = "X123456";
        when(losKlient.hentTilhørendeEnheter(ident)).thenReturn(List.of());

        var enheter = tjeneste.hentLosEnheterFor(ident);

        assertThat(enheter).isNotNull().isEmpty();
        verify(losKlient).hentTilhørendeEnheter(ident);
    }

    @Test
    void testHentingFraLos_test_cache() {
        var ident = "X123456";
        when(losKlient.hentTilhørendeEnheter(ident)).thenReturn(null);

        var enheter = tjeneste.hentLosEnheterFor(ident);

        assertThat(enheter).isNotNull().isEmpty();
        verify(losKlient).hentTilhørendeEnheter(ident);
    }

    @Test
    void testHentingFraLos_svar_null_exception_bør_ikke_skje() {
        var ident = "X123456";
        var testEnhet = new TilhørendeEnhetDto("1234", "Test Enhet");
        when(losKlient.hentTilhørendeEnheter(ident)).thenReturn(List.of(testEnhet));

        var enheter = tjeneste.hentLosEnheterFor(ident);
        var enheterTo = tjeneste.hentLosEnheterFor(ident);

        assertThat(enheter).isNotEmpty().hasSize(1).contains(testEnhet);
        assertEquals(enheter, enheterTo);

        verify(losKlient).hentTilhørendeEnheter(ident);
    }
}
