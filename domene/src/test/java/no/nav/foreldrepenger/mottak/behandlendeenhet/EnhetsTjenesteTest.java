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

import no.nav.foreldrepenger.mottak.person.GeoTilknytning;
import no.nav.foreldrepenger.mottak.person.PersonTjeneste;
import no.nav.vedtak.felles.integrasjon.arbeidsfordeling.rest.ArbeidsfordelingResponse;
import no.nav.vedtak.felles.integrasjon.arbeidsfordeling.rest.ArbeidsfordelingRestKlient;

@ExtendWith(MockitoExtension.class)
public class EnhetsTjenesteTest {
    private static final String AKTØR_ID = "9999999999999";
    private static final ArbeidsfordelingResponse ENHET = new ArbeidsfordelingResponse("4801", "Enhet", "Aktiv", "FPY");
    private static final ArbeidsfordelingResponse FORDELING_ENHET = new ArbeidsfordelingResponse("4825", "Oslo", "Aktiv", "FPY");
    public static final String GEOGRAFISK_TILKNYTNING = "test";
    public static final String DISKRESJONSKODE = "diskresjonskode";
    private EnhetsTjeneste enhetsTjeneste;
    @Mock
    private ArbeidsfordelingRestKlient arbeidsfordelingTjeneste;
    @Mock
    private PersonTjeneste personTjeneste;

    @BeforeEach
    public void setup() {
        when(arbeidsfordelingTjeneste.hentAlleAktiveEnheter(any())).thenReturn(List.of(FORDELING_ENHET));
        enhetsTjeneste = new EnhetsTjeneste(personTjeneste, arbeidsfordelingTjeneste);
    }

    @Test
    public void skal_returnere_enhetid() {

        var response = new GeoTilknytning(GEOGRAFISK_TILKNYTNING, null);
        when(personTjeneste.hentGeografiskTilknytning(any())).thenReturn(response);
        when(arbeidsfordelingTjeneste.finnEnhet(any())).thenReturn(List.of(ENHET));

        String enhetId = enhetsTjeneste.hentFordelingEnhetId(FORELDRE_OG_SVANGERSKAPSPENGER, FORELDREPENGER, Optional.empty(), AKTØR_ID);

        assertThat(enhetId).isNotNull();
        assertThat(enhetId).isEqualTo(ENHET.getEnhetNr());
    }

    @Test
    public void skal_returnere_enhetid_og_kalle_arbeidsfordelingstjeneste_med_geografisk_tilknytning() {

        var response = new GeoTilknytning(GEOGRAFISK_TILKNYTNING, null);
        when(personTjeneste.hentGeografiskTilknytning(any())).thenReturn(response);
        when(arbeidsfordelingTjeneste.finnEnhet(any())).thenReturn(List.of(ENHET));

        String enhetId = enhetsTjeneste.hentFordelingEnhetId(FORELDRE_OG_SVANGERSKAPSPENGER, FORELDREPENGER, Optional.empty(), AKTØR_ID);

        assertThat(enhetId).isNotNull();
        assertThat(enhetId).isEqualTo(ENHET.getEnhetNr());
    }

    @Test
    public void skal_returnere_enhetid_og_kalle_arbeidsfordelingstjeneste_med_diskresjonskode() {

        var response = new GeoTilknytning(GEOGRAFISK_TILKNYTNING, DISKRESJONSKODE);
        when(personTjeneste.hentGeografiskTilknytning(any())).thenReturn(response);
        when(arbeidsfordelingTjeneste.finnEnhet(any())).thenReturn(List.of(ENHET));

        String enhetId = enhetsTjeneste.hentFordelingEnhetId(FORELDRE_OG_SVANGERSKAPSPENGER, FORELDREPENGER, Optional.empty(), AKTØR_ID);

        assertThat(enhetId).isNotNull();
        assertThat(enhetId).isEqualTo(ENHET.getEnhetNr());
    }

    @Test
    public void skal_returnere_enhetid_journalføring() {
        var response = new GeoTilknytning(GEOGRAFISK_TILKNYTNING, DISKRESJONSKODE);
        when(personTjeneste.hentGeografiskTilknytning(any())).thenReturn(response);
        when(arbeidsfordelingTjeneste.finnEnhet(any())).thenReturn(List.of(ENHET));

        String enhetId = enhetsTjeneste.hentFordelingEnhetId(FORELDRE_OG_SVANGERSKAPSPENGER, FORELDREPENGER, Optional.empty(), AKTØR_ID);

        assertThat(enhetId).isNotNull();
        assertThat(enhetId).isEqualTo(ENHET.getEnhetNr());
    }

    @Test
    public void skal_returnere_enhetid_journalføring_uten_fnr() {

        String enhetId = enhetsTjeneste.hentFordelingEnhetId(FORELDRE_OG_SVANGERSKAPSPENGER, FORELDREPENGER, Optional.empty(), null);
        assertThat(enhetId).isNotNull();
        assertThat(enhetId).isEqualTo(FORDELING_ENHET.getEnhetNr());
    }

}
