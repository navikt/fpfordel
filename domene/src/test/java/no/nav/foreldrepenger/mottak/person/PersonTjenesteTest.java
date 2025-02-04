package no.nav.foreldrepenger.mottak.person;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.pdl.GeografiskTilknytning;
import no.nav.pdl.GtType;
import no.nav.pdl.Navn;
import no.nav.pdl.Person;
import no.nav.vedtak.felles.integrasjon.person.Persondata;

@ExtendWith(MockitoExtension.class)
class PersonTjenesteTest {
    private static final String AKTØR_ID = "2222222222222";
    private static final String FNR = "11111111111";
    private PersonInformasjon personTjeneste;
    @Mock
    private Persondata pdl;

    @BeforeEach
    void setup() {
        personTjeneste = new PersonTjeneste(pdl, 500);
    }

    @Test
    void skal_returnere_fnr() throws Exception {
        when(pdl.hentPersonIdentForAktørId(AKTØR_ID)).thenReturn(Optional.of(FNR));
        var fnr = personTjeneste.hentPersonIdentForAktørId(AKTØR_ID);
        assertEquals(Optional.of(FNR), fnr);
        personTjeneste.hentPersonIdentForAktørId(AKTØR_ID);
        verify(pdl).hentPersonIdentForAktørId(AKTØR_ID);
        Thread.sleep(1000);
        personTjeneste.hentPersonIdentForAktørId(AKTØR_ID);
        verify(pdl, times(2)).hentPersonIdentForAktørId(AKTØR_ID);
    }

    @Test
    void skal_returnere_aktørid() throws Exception {
        when(pdl.hentAktørIdForPersonIdent(FNR)).thenReturn(Optional.of(AKTØR_ID));
        var aid = personTjeneste.hentAktørIdForPersonIdent(FNR);
        assertEquals(Optional.of(AKTØR_ID), aid);
        personTjeneste.hentAktørIdForPersonIdent(FNR);
        verify(pdl).hentAktørIdForPersonIdent(FNR);
        Thread.sleep(1000);
        personTjeneste.hentAktørIdForPersonIdent(FNR);
        verify(pdl, times(2)).hentAktørIdForPersonIdent(FNR);
    }

    @Test
    void skal_returnere_empty_uten_match() {
        when(pdl.hentPersonIdentForAktørId(AKTØR_ID)).thenReturn(Optional.empty());
        assertThat(personTjeneste.hentPersonIdentForAktørId(AKTØR_ID)).isEmpty();
    }

    @Test
    void skal_returnere_konstruert_navn() {
        var responsenavn = new Navn("Kari", "Mari", "Nordmann", null, null, null, null, null);
        var response = new Person();
        response.setNavn(List.of(responsenavn));
        when(pdl.hentPerson(any(), argThat(a -> a.getInput().get("ident").equals(AKTØR_ID)), any())).thenReturn(response);
        String navn = personTjeneste.hentNavn(BehandlingTema.FORELDREPENGER, AKTØR_ID);
        assertThat(navn).isEqualTo("Kari Mari Nordmann");
    }

    @Test
    void skal_returnere_tom_gt_hvisikkesatt() {
        var responsegt = new GeografiskTilknytning(null, null, null, null, null);
        when(pdl.hentGT(any(), argThat(a -> a.getInput().get("ident").equals(AKTØR_ID)), any())).thenReturn(responsegt);
        var gt = personTjeneste.hentGeografiskTilknytning(BehandlingTema.FORELDREPENGER, AKTØR_ID);
        assertThat(gt).isNull();
    }

    @Test
    void skal_returnere_gt_bydel() {
        var responsegt = new GeografiskTilknytning(GtType.BYDEL, "Oslo", "030110", "NOR", null);
        when(pdl.hentGT(any(), argThat(a -> a.getInput().get("ident").equals(AKTØR_ID)), any())).thenReturn(responsegt);
        var gt = personTjeneste.hentGeografiskTilknytning(BehandlingTema.FORELDREPENGER, AKTØR_ID);
        assertThat(gt).isEqualTo("030110");
    }

    @Test
    void skal_returnere_gt_kommune() {
        var responsegt = new GeografiskTilknytning(GtType.KOMMUNE, "Stord", null, "NOR", null);
        when(pdl.hentGT(any(), argThat(a -> a.getInput().get("ident").equals(AKTØR_ID)), any())).thenReturn(responsegt);
        var gt = personTjeneste.hentGeografiskTilknytning(BehandlingTema.FORELDREPENGER, AKTØR_ID);
        assertThat(gt).isEqualTo("Stord");
    }

    @Test
    void skal_returnere_gt_land() {
        var responsegt = new GeografiskTilknytning(GtType.UTLAND, null, null, "POL", null);
        when(pdl.hentGT(any(), argThat(a -> a.getInput().get("ident").equals(AKTØR_ID)), any())).thenReturn(responsegt);

        var gt = personTjeneste.hentGeografiskTilknytning(BehandlingTema.FORELDREPENGER, AKTØR_ID);
        assertThat(gt).isNull();
    }
}
