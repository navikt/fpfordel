package no.nav.foreldrepenger.mottak.behandlendeenhet;

import static no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema.FORELDREPENGER;
import static no.nav.foreldrepenger.fordel.kodeverdi.Tema.FORELDRE_OG_SVANGERSKAPSPENGER;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.tjeneste.virksomhet.person.v3.binding.HentGeografiskTilknytningPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentGeografiskTilknytningSikkerhetsbegrensing;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Diskresjonskoder;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Kommune;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentGeografiskTilknytningResponse;
import no.nav.vedtak.exception.ManglerTilgangException;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.integrasjon.arbeidsfordeling.rest.ArbeidsfordelingResponse;
import no.nav.vedtak.felles.integrasjon.arbeidsfordeling.rest.ArbeidsfordelingRestKlient;
import no.nav.vedtak.felles.integrasjon.person.PersonConsumer;

@ExtendWith(MockitoExtension.class)
public class EnhetsTjenesteTest {
    private static final String FNR = "99999999999";
    private static final ArbeidsfordelingResponse ENHET = new ArbeidsfordelingResponse("4801", "Enhet", "Aktiv", "FPY");
    private static final ArbeidsfordelingResponse FORDELING_ENHET = new ArbeidsfordelingResponse("4825", "Oslo", "Aktiv", "FPY");
    public static final String GEOGRAFISK_TILKNYTNING = "test";
    public static final String DISKRESJONSKODE = "diskresjonskode";
    private EnhetsTjeneste enhetsTjeneste;
    @Mock
    private ArbeidsfordelingRestKlient arbeidsfordelingTjeneste;
    @Mock
    private PersonConsumer personConsumer;

    @BeforeEach
    public void setup() {
        when(arbeidsfordelingTjeneste.hentAlleAktiveEnheter(any())).thenReturn(List.of(FORDELING_ENHET));
        enhetsTjeneste = new EnhetsTjeneste(personConsumer, arbeidsfordelingTjeneste);
    }

    @Test
    public void skal_returnere_enhetid() throws Exception {

        var response = new HentGeografiskTilknytningResponse();
        when(personConsumer.hentGeografiskTilknytning(any())).thenReturn(response);
        when(arbeidsfordelingTjeneste.finnEnhet(any())).thenReturn(List.of(ENHET));

        String enhetId = enhetsTjeneste.hentFordelingEnhetId(FORELDRE_OG_SVANGERSKAPSPENGER, FORELDREPENGER, Optional.empty(),
                Optional.of(FNR));

        assertThat(enhetId).isNotNull();
        assertThat(enhetId).isEqualTo(ENHET.getEnhetNr());
    }

    @Test
    public void skal_returnere_enhetid_og_kalle_arbeidsfordelingstjeneste_med_geografisk_tilknytning() throws Exception {

        var response = new HentGeografiskTilknytningResponse();
        var geografiskTilknytning = new Kommune();
        geografiskTilknytning.setGeografiskTilknytning(GEOGRAFISK_TILKNYTNING);
        response.setGeografiskTilknytning(geografiskTilknytning);
        when(personConsumer.hentGeografiskTilknytning(any())).thenReturn(response);
        when(arbeidsfordelingTjeneste.finnEnhet(any())).thenReturn(List.of(ENHET));

        String enhetId = enhetsTjeneste.hentFordelingEnhetId(FORELDRE_OG_SVANGERSKAPSPENGER, FORELDREPENGER, Optional.empty(),
                Optional.of(FNR));

        assertThat(enhetId).isNotNull();
        assertThat(enhetId).isEqualTo(ENHET.getEnhetNr());
    }

    @Test
    public void skal_returnere_enhetid_og_kalle_arbeidsfordelingstjeneste_med_diskresjonskode() throws Exception {

        var response = new HentGeografiskTilknytningResponse();
        var diskresjonskoder = new Diskresjonskoder();
        diskresjonskoder.setValue(DISKRESJONSKODE);
        response.setDiskresjonskode(diskresjonskoder);
        when(personConsumer.hentGeografiskTilknytning(any())).thenReturn(response);
        when(arbeidsfordelingTjeneste.finnEnhet(any())).thenReturn(List.of(ENHET));

        String enhetId = enhetsTjeneste.hentFordelingEnhetId(FORELDRE_OG_SVANGERSKAPSPENGER, FORELDREPENGER, Optional.empty(),
                Optional.of(FNR));

        assertThat(enhetId).isNotNull();
        assertThat(enhetId).isEqualTo(ENHET.getEnhetNr());
    }

    @Test
    public void skal_returnere_enhetid_journalføring() throws Exception {
        var response = new HentGeografiskTilknytningResponse();
        var diskresjonskoder = new Diskresjonskoder();
        diskresjonskoder.setValue(DISKRESJONSKODE);
        response.setDiskresjonskode(diskresjonskoder);
        when(personConsumer.hentGeografiskTilknytning(any())).thenReturn(response);
        when(arbeidsfordelingTjeneste.finnEnhet(any())).thenReturn(List.of(ENHET));

        String enhetId = enhetsTjeneste.hentFordelingEnhetId(FORELDRE_OG_SVANGERSKAPSPENGER, FORELDREPENGER, Optional.empty(),
                Optional.of(FNR));

        assertThat(enhetId).isNotNull();
        assertThat(enhetId).isEqualTo(ENHET.getEnhetNr());
    }

    @Test
    public void skal_returnere_enhetid_journalføring_uten_fnr() {

        String enhetId = enhetsTjeneste.hentFordelingEnhetId(FORELDRE_OG_SVANGERSKAPSPENGER, FORELDREPENGER, Optional.empty(),
                Optional.empty());
        assertThat(enhetId).isNotNull();
        assertThat(enhetId).isEqualTo(FORDELING_ENHET.getEnhetNr());
    }

    @Test
    public void skal_kaste_sikkerhetsbegrening() throws Exception {
        when(personConsumer.hentGeografiskTilknytning(any())).thenThrow(new HentGeografiskTilknytningSikkerhetsbegrensing(null, null));
        var e = assertThrows(ManglerTilgangException.class,
                () -> enhetsTjeneste.hentFordelingEnhetId(FORELDRE_OG_SVANGERSKAPSPENGER, FORELDREPENGER, Optional.empty(),
                        Optional.of(FNR)));
        assertThat(e.getMessage().contains("FP-509290")).isTrue();

    }

    @Test
    public void skal_kaste_person_ikke_funnet() throws HentGeografiskTilknytningPersonIkkeFunnet, HentGeografiskTilknytningSikkerhetsbegrensing {
        when(personConsumer.hentGeografiskTilknytning(any())).thenThrow(new HentGeografiskTilknytningPersonIkkeFunnet(null, null));
        var e = assertThrows(TekniskException.class,
                () -> enhetsTjeneste.hentFordelingEnhetId(FORELDRE_OG_SVANGERSKAPSPENGER, FORELDREPENGER, Optional.empty(),
                        Optional.of(FNR)));
        assertTrue(e.getMessage().contains("FP-070668"));

    }
}
