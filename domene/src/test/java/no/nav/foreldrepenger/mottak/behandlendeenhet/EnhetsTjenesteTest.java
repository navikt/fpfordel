package no.nav.foreldrepenger.mottak.behandlendeenhet;

import static no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema.FORELDREPENGER;
import static no.nav.foreldrepenger.fordel.kodeverdi.Tema.FORELDRE_OG_SVANGERSKAPSPENGER;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import no.nav.foreldrepenger.mottak.person.PersonInformasjon;
import no.nav.vedtak.felles.integrasjon.arbeidsfordeling.Arbeidsfordeling;
import no.nav.vedtak.felles.integrasjon.arbeidsfordeling.ArbeidsfordelingResponse;
import no.nav.vedtak.felles.integrasjon.skjerming.Skjerming;

@ExtendWith(MockitoExtension.class)
class EnhetsTjenesteTest {
    public static final String GEOGRAFISK_TILKNYTNING = "test";
    private static final String AKTØR_ID = "9999999999999";
    private static final String FNR = "99999999999";
    private static final ArbeidsfordelingResponse ENHET = new ArbeidsfordelingResponse("4801", "Enhet", "Aktiv", "FPY");
    private static final ArbeidsfordelingResponse FORDELING_ENHET = new ArbeidsfordelingResponse("4867", "Oslo", "Aktiv", "FPY");
    private EnhetsTjeneste enhetsTjeneste;
    @Mock
    private Arbeidsfordeling arbeidsfordeling;
    @Mock
    private PersonInformasjon personTjeneste;
    @Mock
    private Skjerming skjermetPersonKlient;

    @BeforeEach
    void setup() {
        when(personTjeneste.hentPersonIdentForAktørId(any())).thenReturn(Optional.of(FNR));
        when(arbeidsfordeling.hentAlleAktiveEnheter(any())).thenReturn(List.of(FORDELING_ENHET));
        when(arbeidsfordeling.finnEnhet(any())).thenReturn(List.of(ENHET));
        when(personTjeneste.hentGeografiskTilknytning(any(), any())).thenReturn(GEOGRAFISK_TILKNYTNING);
        enhetsTjeneste = new EnhetsTjeneste(personTjeneste, arbeidsfordeling, skjermetPersonKlient);
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void skal_returnere_enhetid() {
        assertThat(enhetId()).isNotNull().isEqualTo(FORDELING_ENHET.enhetNr());
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void skal_returnere_enhetid_skjermet() {
        when(skjermetPersonKlient.erSkjermet(any())).thenReturn(true);
        assertThat(enhetId()).isNotNull().isEqualTo(EnhetsTjeneste.SKJERMET_ENHET_ID);
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void skal_returnere_enhetid_journalføring_uten_fnr() {
        assertThat(enhetId(null)).isNotNull().isEqualTo(FORDELING_ENHET.enhetNr());
    }

    private String enhetId() {
        return enhetId(AKTØR_ID);
    }

    private String enhetId(String aktørId) {
        return enhetsTjeneste.hentFordelingEnhetId(FORELDRE_OG_SVANGERSKAPSPENGER, FORELDREPENGER, Optional.empty(), aktørId);
    }
}
