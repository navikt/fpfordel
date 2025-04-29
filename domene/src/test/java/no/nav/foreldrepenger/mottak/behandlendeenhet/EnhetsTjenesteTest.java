package no.nav.foreldrepenger.mottak.behandlendeenhet;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import no.nav.vedtak.felles.integrasjon.ruting.RutingResultat;

@ExtendWith(MockitoExtension.class)
class EnhetsTjenesteTest {

    private static final String AKTØR_ID = "9999999999999";
    private static final String FORDELING_ENHET = "4867";
    private EnhetsTjeneste enhetsTjeneste;
    @Mock
    private RutingKlient rutingKlient;

    @BeforeEach
    void setup() {
        enhetsTjeneste = new EnhetsTjeneste(rutingKlient);
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void skal_returnere_enhetid() {
        assertThat(enhetId()).isNotNull().isEqualTo(FORDELING_ENHET);
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void skal_returnere_enhetid_skjermet() {
        when(rutingKlient.finnRutingEgenskaper(anySet())).thenReturn(Set.of(RutingResultat.SKJERMING));
        assertThat(enhetId()).isNotNull().isEqualTo(EnhetsTjeneste.SKJERMET_ENHET_ID);
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void skal_returnere_enhetid_strengt_fortrolig() {
        when(rutingKlient.finnRutingEgenskaper(anySet())).thenReturn(Set.of(RutingResultat.STRENGTFORTROLIG));
        assertThat(enhetId()).isNotNull().isEqualTo(EnhetsTjeneste.SF_ENHET_ID);
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void skal_returnere_enhetid_utland() {
        when(rutingKlient.finnRutingEgenskaper(anySet())).thenReturn(Set.of(RutingResultat.UTLAND));
        assertThat(enhetId()).isNotNull().isEqualTo(EnhetsTjeneste.UTLAND_ENHET_ID);
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void skal_returnere_enhetid_journalføring_uten_fnr() {
        assertThat(enhetId(null)).isNotNull().isEqualTo(FORDELING_ENHET);
    }

    private String enhetId() {
        return enhetId(AKTØR_ID);
    }

    private String enhetId(String aktørId) {
        return enhetsTjeneste.hentFordelingEnhetId(Optional.empty(), aktørId);
    }
}
