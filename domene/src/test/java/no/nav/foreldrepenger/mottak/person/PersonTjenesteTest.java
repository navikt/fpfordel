package no.nav.foreldrepenger.mottak.person;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import no.nav.pdl.Adressebeskyttelse;
import no.nav.pdl.AdressebeskyttelseGradering;
import no.nav.pdl.GeografiskTilknytning;
import no.nav.pdl.GtType;
import no.nav.pdl.IdentGruppe;
import no.nav.pdl.IdentInformasjon;
import no.nav.pdl.Identliste;
import no.nav.pdl.Navn;
import no.nav.pdl.Person;
import no.nav.vedtak.felles.integrasjon.pdl.Pdl;

@ExtendWith(MockitoExtension.class)
public class PersonTjenesteTest {
    private static final String AKTØR_ID = "9999999999999";
    private static final String FNR = "99999999999";

    private PersonInformasjon personTjeneste;
    @Mock
    private Pdl pdl;

    @BeforeEach
    public void setup() {
        personTjeneste = new PersonTjeneste(pdl, pdl);
    }

    @Test
    public void skal_returnere_fnr() {
        var response = new Identliste(List.of(new IdentInformasjon(FNR, IdentGruppe.FOLKEREGISTERIDENT, false)));
        when(pdl.hentIdenter(argThat(a -> a.getInput().get("ident").equals(AKTØR_ID)), any())).thenReturn(response);
        Optional<String> fnr = personTjeneste.hentPersonIdentForAktørId(AKTØR_ID);
        assertThat(fnr).isPresent();
        assertThat(fnr).hasValueSatisfying(v -> assertThat(v).isEqualTo(FNR));
    }

    @Test
    public void skal_returnere_aktørid() {
        var response = new Identliste(List.of(new IdentInformasjon(AKTØR_ID, IdentGruppe.AKTORID, false)));
        when(pdl.hentIdenter(argThat(a -> a.getInput().get("ident").equals(FNR)), any())).thenReturn(response);
        Optional<String> aid = personTjeneste.hentAktørIdForPersonIdent(FNR);
        assertThat(aid).isPresent();
        assertThat(aid).hasValueSatisfying(v -> assertThat(v).isEqualTo(AKTØR_ID));
    }

    @Test
    public void skal_returnere_empty_uten_match() {
        var response = new Identliste(List.of());
        when(pdl.hentIdenter(argThat(a -> a.getInput().get("ident").equals(AKTØR_ID)), any())).thenReturn(response);
        Optional<String> fnr = personTjeneste.hentPersonIdentForAktørId(AKTØR_ID);
        assertThat(fnr).isEmpty();
    }

    @Test
    public void skal_returnere_forkortet_navn() {
        var responsenavn = new Navn("Ola", null, "Nordmann", "Nordmann Ola", null, null, null, null);
        var response = new Person();
        response.setNavn(List.of(responsenavn));
        when(pdl.hentPerson(argThat(a -> a.getInput().get("ident").equals(AKTØR_ID)), any())).thenReturn(response);
        String navn = personTjeneste.hentNavn(AKTØR_ID);
        assertThat(navn).isEqualTo("Nordmann Ola");
    }

    @Test
    public void skal_returnere_konstruert_navn() {
        var responsenavn = new Navn("Kari", "Mari", "Nordmann", null, null, null, null, null);
        var response = new Person();
        response.setNavn(List.of(responsenavn));
        when(pdl.hentPerson(argThat(a -> a.getInput().get("ident").equals(AKTØR_ID)), any())).thenReturn(response);
        String navn = personTjeneste.hentNavn(AKTØR_ID);
        assertThat(navn).isEqualTo("Nordmann Kari Mari");
    }

    @Test
    public void skal_returnere_tom_gt_hvisikkesatt() {
        var responsebeskyttelse = new Adressebeskyttelse(AdressebeskyttelseGradering.UGRADERT, null, null);
        var responsegt = new GeografiskTilknytning(null, null, null, null, null);
        var response = new Person();
        response.setAdressebeskyttelse(List.of(responsebeskyttelse));
        when(pdl.hentPerson(argThat(a -> a.getInput().get("ident").equals(AKTØR_ID)), any())).thenReturn(response);
        when(pdl.hentGT(argThat(a -> a.getInput().get("ident").equals(AKTØR_ID)), any())).thenReturn(responsegt);
        GeoTilknytning gt = personTjeneste.hentGeografiskTilknytning(AKTØR_ID);
        assertThat(gt.getTilknytning()).isNull();
        assertThat(gt.getDiskresjonskode()).isNull();
    }

    @Test
    public void skal_returnere_gt_bydel() {
        var responsebeskyttelse = new Adressebeskyttelse(AdressebeskyttelseGradering.FORTROLIG, null, null);
        var responsegt = new GeografiskTilknytning(GtType.BYDEL, "Oslo", "030110", "NOR", null);
        var response = new Person();
        response.setAdressebeskyttelse(List.of(responsebeskyttelse));
        when(pdl.hentPerson(argThat(a -> a.getInput().get("ident").equals(AKTØR_ID)), any())).thenReturn(response);
        when(pdl.hentGT(argThat(a -> a.getInput().get("ident").equals(AKTØR_ID)), any())).thenReturn(responsegt);
        GeoTilknytning gt = personTjeneste.hentGeografiskTilknytning(AKTØR_ID);
        assertThat(gt.getTilknytning()).isEqualTo("030110");
        assertThat(gt.getDiskresjonskode()).isEqualTo("SPFO");
    }

    @Test
    public void skal_returnere_gt_land() {
        var responsebeskyttelse = new Adressebeskyttelse(AdressebeskyttelseGradering.STRENGT_FORTROLIG, null, null);
        var responsegt = new GeografiskTilknytning(GtType.UTLAND, null, null, "POL", null);
        var response = new Person();
        response.setAdressebeskyttelse(List.of(responsebeskyttelse));
        when(pdl.hentPerson(argThat(a -> a.getInput().get("ident").equals(AKTØR_ID)), any())).thenReturn(response);
        when(pdl.hentGT(argThat(a -> a.getInput().get("ident").equals(AKTØR_ID)), any())).thenReturn(responsegt);

        GeoTilknytning gt = personTjeneste.hentGeografiskTilknytning(AKTØR_ID);

        assertThat(gt.getTilknytning()).isEqualTo("POL");
        assertThat(gt.getDiskresjonskode()).isEqualTo("SPSF");
    }
}
