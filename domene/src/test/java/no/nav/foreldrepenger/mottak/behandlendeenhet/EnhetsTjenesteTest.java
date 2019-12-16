package no.nav.foreldrepenger.mottak.behandlendeenhet;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.Tema;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentGeografiskTilknytningPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentGeografiskTilknytningSikkerhetsbegrensing;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Diskresjonskoder;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.GeografiskTilknytning;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Kommune;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Person;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentGeografiskTilknytningResponse;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonResponse;
import no.nav.vedtak.exception.ManglerTilgangException;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.integrasjon.person.PersonConsumer;

public class EnhetsTjenesteTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private static final String ENHET_ID = "enhetId001";
    private static final String FNR = "99999999999";
    private String fordelingsOppgaveEnhetsId = "4825";
    public static final String GEOGRAFISK_TILKNYTNING = "test";
    public static final String DISKRESJONSKODE = "diskresjonskode";
    private EnhetsTjeneste enhetsTjeneste; // objektet vi tester
    private ArbeidsfordelingTjeneste arbeidsfordelingTjeneste;
    private PersonConsumer personConsumer;
    private HentPersonResponse hentPersonResponse = new HentPersonResponse();;

    @Before
    public void setup() {
        Person person = new Person();
        hentPersonResponse.setPerson(person);
        personConsumer = mock(PersonConsumer.class);
        arbeidsfordelingTjeneste = mock(ArbeidsfordelingTjeneste.class);
        when(arbeidsfordelingTjeneste.finnAlleJournalførendeEnhetIdListe(any(BehandlingTema.class), anyBoolean())).thenReturn(Collections.singletonList(fordelingsOppgaveEnhetsId));

        enhetsTjeneste = new EnhetsTjeneste(personConsumer, arbeidsfordelingTjeneste);
    }

    @Test
    public void skal_returnere_enhetid() throws HentGeografiskTilknytningPersonIkkeFunnet, HentGeografiskTilknytningSikkerhetsbegrensing, HentPersonPersonIkkeFunnet, HentPersonSikkerhetsbegrensning {

        HentGeografiskTilknytningResponse response = new HentGeografiskTilknytningResponse();
        when(personConsumer.hentGeografiskTilknytning(any())).thenReturn(response);
        when(personConsumer.hentPersonResponse(any())).thenReturn(hentPersonResponse);
        when(arbeidsfordelingTjeneste.finnBehandlendeEnhetId(null, null, BehandlingTema.FORELDREPENGER, Tema.FORELDRE_OG_SVANGERSKAPSPENGER)).thenReturn(ENHET_ID);

        String enhetId = enhetsTjeneste.hentFordelingEnhetId(Tema.FORELDRE_OG_SVANGERSKAPSPENGER, BehandlingTema.FORELDREPENGER, Optional.empty(), Optional.of(FNR));

        assertThat(enhetId).isNotNull();
        assertThat(enhetId).isEqualTo(ENHET_ID);
    }

    @Test
    public void skal_returnere_enhetid_og_kalle_arbeidsfordelingstjeneste_med_geografisk_tilknytning() throws HentGeografiskTilknytningPersonIkkeFunnet, HentGeografiskTilknytningSikkerhetsbegrensing, HentPersonPersonIkkeFunnet, HentPersonSikkerhetsbegrensning {

        HentGeografiskTilknytningResponse response = new HentGeografiskTilknytningResponse();
        GeografiskTilknytning geografiskTilknytning = new Kommune();
        geografiskTilknytning.setGeografiskTilknytning(GEOGRAFISK_TILKNYTNING);
        response.setGeografiskTilknytning(geografiskTilknytning);
        when(personConsumer.hentGeografiskTilknytning(any())).thenReturn(response);
        when(personConsumer.hentPersonResponse(any())).thenReturn(hentPersonResponse);
        when(arbeidsfordelingTjeneste.finnBehandlendeEnhetId(GEOGRAFISK_TILKNYTNING, null, BehandlingTema.FORELDREPENGER, Tema.FORELDRE_OG_SVANGERSKAPSPENGER)).thenReturn(ENHET_ID);

        String enhetId = enhetsTjeneste.hentFordelingEnhetId(Tema.FORELDRE_OG_SVANGERSKAPSPENGER, BehandlingTema.FORELDREPENGER, Optional.empty(), Optional.of(FNR));

        assertThat(enhetId).isNotNull();
        assertThat(enhetId).isEqualTo(ENHET_ID);
    }

    @Test
    public void skal_returnere_enhetid_og_kalle_arbeidsfordelingstjeneste_med_diskresjonskode() throws HentGeografiskTilknytningPersonIkkeFunnet, HentGeografiskTilknytningSikkerhetsbegrensing, HentPersonPersonIkkeFunnet, HentPersonSikkerhetsbegrensning {

        HentGeografiskTilknytningResponse response = new HentGeografiskTilknytningResponse();
        Diskresjonskoder diskresjonskoder = new Diskresjonskoder();
        diskresjonskoder.setValue(DISKRESJONSKODE);
        response.setDiskresjonskode(diskresjonskoder);
        when(personConsumer.hentGeografiskTilknytning(any())).thenReturn(response);
        when(personConsumer.hentPersonResponse(any())).thenReturn(hentPersonResponse);
        when(arbeidsfordelingTjeneste.finnBehandlendeEnhetId(null, DISKRESJONSKODE, BehandlingTema.FORELDREPENGER, Tema.FORELDRE_OG_SVANGERSKAPSPENGER)).thenReturn(ENHET_ID);

        String enhetId = enhetsTjeneste.hentFordelingEnhetId(Tema.FORELDRE_OG_SVANGERSKAPSPENGER, BehandlingTema.FORELDREPENGER, Optional.empty(), Optional.of(FNR));

        assertThat(enhetId).isNotNull();
        assertThat(enhetId).isEqualTo(ENHET_ID);
    }

    @Test
    public void skal_returnere_enhetid_journalføring() throws HentGeografiskTilknytningPersonIkkeFunnet, HentGeografiskTilknytningSikkerhetsbegrensing, HentPersonPersonIkkeFunnet, HentPersonSikkerhetsbegrensning {
        HentGeografiskTilknytningResponse response = new HentGeografiskTilknytningResponse();
        Diskresjonskoder diskresjonskoder = new Diskresjonskoder();
        diskresjonskoder.setValue(DISKRESJONSKODE);
        response.setDiskresjonskode(diskresjonskoder);
        when(personConsumer.hentGeografiskTilknytning(any())).thenReturn(response);
        when(personConsumer.hentPersonResponse(any())).thenReturn(hentPersonResponse);
        when(arbeidsfordelingTjeneste.finnBehandlendeEnhetId(null, DISKRESJONSKODE, BehandlingTema.FORELDREPENGER, Tema.FORELDRE_OG_SVANGERSKAPSPENGER)).thenReturn(ENHET_ID);


        String enhetId = enhetsTjeneste.hentFordelingEnhetId(Tema.FORELDRE_OG_SVANGERSKAPSPENGER, BehandlingTema.FORELDREPENGER, Optional.empty(), Optional.of(FNR));

        assertThat(enhetId).isNotNull();
        assertThat(enhetId).isEqualTo(ENHET_ID);
    }

    @Test
    public void skal_returnere_enhetid_journalføring_uten_fnr()  {

        String enhetId = enhetsTjeneste.hentFordelingEnhetId(Tema.FORELDRE_OG_SVANGERSKAPSPENGER, BehandlingTema.FORELDREPENGER, Optional.empty(), Optional.empty());

        assertThat(enhetId).isNotNull();
        assertThat(enhetId).isEqualTo(fordelingsOppgaveEnhetsId);
    }

    @Test
    public void skal_kaste_sikkerhetsbegrening() throws HentGeografiskTilknytningPersonIkkeFunnet, HentGeografiskTilknytningSikkerhetsbegrensing, HentPersonPersonIkkeFunnet, HentPersonSikkerhetsbegrensning {
        expectedException.expect(ManglerTilgangException.class);
        expectedException.expectMessage("FP-509290:Mangler tilgang til å utføre hentGeografiskTilknytning");
        when(personConsumer.hentGeografiskTilknytning(any())).thenThrow(new HentGeografiskTilknytningSikkerhetsbegrensing(null, null));
        when(personConsumer.hentPersonResponse(any())).thenReturn(hentPersonResponse);

        @SuppressWarnings("unused")
        String enhetId = enhetsTjeneste.hentFordelingEnhetId(Tema.FORELDRE_OG_SVANGERSKAPSPENGER, BehandlingTema.FORELDREPENGER, Optional.empty(), Optional.of(FNR));

    }

    @SuppressWarnings("unused")
    @Test
    public void skal_kaste_person_ikke_funnet() throws HentGeografiskTilknytningPersonIkkeFunnet, HentGeografiskTilknytningSikkerhetsbegrensing, HentPersonPersonIkkeFunnet, HentPersonSikkerhetsbegrensning {
        expectedException.expect(TekniskException.class);
        expectedException.expectMessage("FP-070668:Person ikke funnet ved hentGeografiskTilknytning");
        HentGeografiskTilknytningResponse response = new HentGeografiskTilknytningResponse();
        when(personConsumer.hentGeografiskTilknytning(any())).thenThrow(new HentGeografiskTilknytningPersonIkkeFunnet(null, null));
        when(personConsumer.hentPersonResponse(any())).thenReturn(hentPersonResponse);

        @SuppressWarnings("unused")
        String enhetId = enhetsTjeneste.hentFordelingEnhetId(Tema.FORELDRE_OG_SVANGERSKAPSPENGER, BehandlingTema.FORELDREPENGER, Optional.empty(), Optional.of(FNR));

    }
}
