package no.nav.foreldrepenger.mottak.behandlendeenhet;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.Tema;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentGeografiskTilknytningPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentGeografiskTilknytningSikkerhetsbegrensing;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Diskresjonskoder;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.GeografiskTilknytning;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Kommune;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentGeografiskTilknytningResponse;
import no.nav.vedtak.exception.ManglerTilgangException;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.integrasjon.arbeidsfordeling.rest.ArbeidsfordelingResponse;
import no.nav.vedtak.felles.integrasjon.arbeidsfordeling.rest.ArbeidsfordelingRestKlient;
import no.nav.vedtak.felles.integrasjon.person.PersonConsumer;

public class EnhetsTjenesteTest {

    private static final String FNR = "99999999999";
    private static final ArbeidsfordelingResponse ENHET = new ArbeidsfordelingResponse("4801", "Enhet", "Aktiv", "FPY");
    private static final ArbeidsfordelingResponse FORDELING_ENHET = new ArbeidsfordelingResponse("4825", "Oslo", "Aktiv", "FPY");
    public static final String GEOGRAFISK_TILKNYTNING = "test";
    public static final String DISKRESJONSKODE = "diskresjonskode";
    private EnhetsTjeneste enhetsTjeneste; // objektet vi tester
    private ArbeidsfordelingRestKlient arbeidsfordelingTjeneste;
    private PersonConsumer personConsumer;

    @BeforeEach
    public void setup() {
        personConsumer = mock(PersonConsumer.class);
        arbeidsfordelingTjeneste = mock(ArbeidsfordelingRestKlient.class);
        when(arbeidsfordelingTjeneste.hentAlleAktiveEnheter(any())).thenReturn(Collections.singletonList(FORDELING_ENHET));

        enhetsTjeneste = new EnhetsTjeneste(personConsumer, arbeidsfordelingTjeneste);
    }

    @Test
    public void skal_returnere_enhetid() throws HentGeografiskTilknytningPersonIkkeFunnet, HentGeografiskTilknytningSikkerhetsbegrensing {

        HentGeografiskTilknytningResponse response = new HentGeografiskTilknytningResponse();
        when(personConsumer.hentGeografiskTilknytning(any())).thenReturn(response);
        when(arbeidsfordelingTjeneste.finnEnhet(any())).thenReturn(Collections.singletonList(ENHET));

        String enhetId = enhetsTjeneste.hentFordelingEnhetId(Tema.FORELDRE_OG_SVANGERSKAPSPENGER, BehandlingTema.FORELDREPENGER, Optional.empty(),
                Optional.of(FNR));

        assertThat(enhetId).isNotNull();
        assertThat(enhetId).isEqualTo(ENHET.getEnhetNr());
    }

    @Test
    public void skal_returnere_enhetid_og_kalle_arbeidsfordelingstjeneste_med_geografisk_tilknytning()
            throws HentGeografiskTilknytningPersonIkkeFunnet, HentGeografiskTilknytningSikkerhetsbegrensing {

        HentGeografiskTilknytningResponse response = new HentGeografiskTilknytningResponse();
        GeografiskTilknytning geografiskTilknytning = new Kommune();
        geografiskTilknytning.setGeografiskTilknytning(GEOGRAFISK_TILKNYTNING);
        response.setGeografiskTilknytning(geografiskTilknytning);
        when(personConsumer.hentGeografiskTilknytning(any())).thenReturn(response);
        when(arbeidsfordelingTjeneste.finnEnhet(any())).thenReturn(Collections.singletonList(ENHET));

        String enhetId = enhetsTjeneste.hentFordelingEnhetId(Tema.FORELDRE_OG_SVANGERSKAPSPENGER, BehandlingTema.FORELDREPENGER, Optional.empty(),
                Optional.of(FNR));

        assertThat(enhetId).isNotNull();
        assertThat(enhetId).isEqualTo(ENHET.getEnhetNr());
    }

    @Test
    public void skal_returnere_enhetid_og_kalle_arbeidsfordelingstjeneste_med_diskresjonskode() throws HentGeografiskTilknytningPersonIkkeFunnet,
            HentGeografiskTilknytningSikkerhetsbegrensing {

        HentGeografiskTilknytningResponse response = new HentGeografiskTilknytningResponse();
        Diskresjonskoder diskresjonskoder = new Diskresjonskoder();
        diskresjonskoder.setValue(DISKRESJONSKODE);
        response.setDiskresjonskode(diskresjonskoder);
        when(personConsumer.hentGeografiskTilknytning(any())).thenReturn(response);
        when(arbeidsfordelingTjeneste.finnEnhet(any())).thenReturn(Collections.singletonList(ENHET));

        String enhetId = enhetsTjeneste.hentFordelingEnhetId(Tema.FORELDRE_OG_SVANGERSKAPSPENGER, BehandlingTema.FORELDREPENGER, Optional.empty(),
                Optional.of(FNR));

        assertThat(enhetId).isNotNull();
        assertThat(enhetId).isEqualTo(ENHET.getEnhetNr());
    }

    @Test
    public void skal_returnere_enhetid_journalføring() throws HentGeografiskTilknytningPersonIkkeFunnet,
            HentGeografiskTilknytningSikkerhetsbegrensing {
        HentGeografiskTilknytningResponse response = new HentGeografiskTilknytningResponse();
        Diskresjonskoder diskresjonskoder = new Diskresjonskoder();
        diskresjonskoder.setValue(DISKRESJONSKODE);
        response.setDiskresjonskode(diskresjonskoder);
        when(personConsumer.hentGeografiskTilknytning(any())).thenReturn(response);
        when(arbeidsfordelingTjeneste.finnEnhet(any())).thenReturn(Collections.singletonList(ENHET));

        String enhetId = enhetsTjeneste.hentFordelingEnhetId(Tema.FORELDRE_OG_SVANGERSKAPSPENGER, BehandlingTema.FORELDREPENGER, Optional.empty(),
                Optional.of(FNR));

        assertThat(enhetId).isNotNull();
        assertThat(enhetId).isEqualTo(ENHET.getEnhetNr());
    }

    @Test
    public void skal_returnere_enhetid_journalføring_uten_fnr() {

        String enhetId = enhetsTjeneste.hentFordelingEnhetId(Tema.FORELDRE_OG_SVANGERSKAPSPENGER, BehandlingTema.FORELDREPENGER, Optional.empty(),
                Optional.empty());

        assertThat(enhetId).isNotNull();
        assertThat(enhetId).isEqualTo(FORDELING_ENHET.getEnhetNr());
    }

    @Test
    public void skal_kaste_sikkerhetsbegrening() throws HentGeografiskTilknytningPersonIkkeFunnet, HentGeografiskTilknytningSikkerhetsbegrensing {
        when(personConsumer.hentGeografiskTilknytning(any())).thenThrow(new HentGeografiskTilknytningSikkerhetsbegrensing(null, null));
        var e = assertThrows(ManglerTilgangException.class,
                () -> enhetsTjeneste.hentFordelingEnhetId(Tema.FORELDRE_OG_SVANGERSKAPSPENGER, BehandlingTema.FORELDREPENGER, Optional.empty(),
                        Optional.of(FNR)));
        assertTrue(e.getMessage().contains("FP-509290"));

    }

    @Test
    public void skal_kaste_person_ikke_funnet() throws HentGeografiskTilknytningPersonIkkeFunnet, HentGeografiskTilknytningSikkerhetsbegrensing {
        when(personConsumer.hentGeografiskTilknytning(any())).thenThrow(new HentGeografiskTilknytningPersonIkkeFunnet(null, null));
        var e = assertThrows(TekniskException.class,
                () -> enhetsTjeneste.hentFordelingEnhetId(Tema.FORELDRE_OG_SVANGERSKAPSPENGER, BehandlingTema.FORELDREPENGER, Optional.empty(),
                        Optional.of(FNR)));
        assertTrue(e.getMessage().contains("FP-070668"));

    }
}
