package no.nav.foreldrepenger.mottak.person;

import static no.nav.vedtak.felles.integrasjon.pdl.Tema.FOR;

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.pdl.Adressebeskyttelse;
import no.nav.pdl.AdressebeskyttelseGradering;
import no.nav.pdl.AdressebeskyttelseResponseProjection;
import no.nav.pdl.GeografiskTilknytningResponseProjection;
import no.nav.pdl.GtType;
import no.nav.pdl.HentPersonQueryRequest;
import no.nav.pdl.Navn;
import no.nav.pdl.NavnResponseProjection;
import no.nav.pdl.Person;
import no.nav.pdl.PersonResponseProjection;
import no.nav.vedtak.felles.integrasjon.pdl.PdlKlientMedCache;

@ApplicationScoped
public class PersonTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(PersonTjeneste.class);

    private PdlKlientMedCache pdlKlient;

    PersonTjeneste() {
        // CDI
    }

    @Inject
    public PersonTjeneste(PdlKlientMedCache pdlKlient) {
        this.pdlKlient = pdlKlient;
    }

    public Optional<String> hentAktørIdForPersonIdent(String personIdent) {
        return pdlKlient.hentAktørIdForPersonIdent(personIdent, FOR);
    }

    public Optional<String> hentPersonIdentForAktørId(String aktørId) {
        return pdlKlient.hentPersonIdentForAktørId(aktørId, FOR);
    }

    public String hentNavn(String aktørId) {
        var request = new HentPersonQueryRequest();
        request.setIdent(aktørId);
        var projection = new PersonResponseProjection()
                .navn(new NavnResponseProjection().forkortetNavn().fornavn().mellomnavn().etternavn());

        return pdlKlient.getDelegate().hentPerson(request, projection, FOR).getNavn()
                .stream()
                .map(PersonTjeneste::mapNavn)
                .findFirst()
                .orElseThrow();
    }

    // OBS: Ikke bruk denne!!! PDL kommer til å lage et nytt skjema og nytt kall
    // hentGeografiskTilknytning.
    public GeoTilknytning hentGeografiskTilknytning(String aktørId) {
        var query = new HentPersonQueryRequest();
        query.setIdent(aktørId);
        var projection = new PersonResponseProjection()
                .geografiskTilknytning(new GeografiskTilknytningResponseProjection().gtType().gtBydel().gtKommune().gtLand())
                .adressebeskyttelse(new AdressebeskyttelseResponseProjection().gradering());

        var person = pdlKlient.getDelegate().hentPerson(query, projection, FOR);

        var gt = new GeoTilknytning(getTilknytning(person), getDiskresjonskode(person));
        if (gt.getTilknytning() == null) {
            LOG.info("FPFORDEL PDL mangler GT for {}", aktørId);
        }
        return gt;
    }

    private static String mapNavn(Navn navn) {
        if (navn.getForkortetNavn() != null)
            return navn.getForkortetNavn();
        return navn.getEtternavn() + " " + navn.getFornavn() + (navn.getMellomnavn() == null ? "" : " " + navn.getMellomnavn());
    }

    private String getDiskresjonskode(Person person) {
        var kode = person.getAdressebeskyttelse().stream()
                .map(Adressebeskyttelse::getGradering)
                .filter(g -> !AdressebeskyttelseGradering.UGRADERT.equals(g))
                .findFirst().orElse(AdressebeskyttelseGradering.UGRADERT);
        if (AdressebeskyttelseGradering.STRENGT_FORTROLIG.equals(kode) || AdressebeskyttelseGradering.STRENGT_FORTROLIG_UTLAND.equals(kode))
            return "SPSF";
        return AdressebeskyttelseGradering.FORTROLIG.equals(kode) ? "SPFO" : null;
    }

    private String getTilknytning(Person person) {
        if (person.getGeografiskTilknytning() == null || person.getGeografiskTilknytning().getGtType() == null)
            return null;
        var kode = person.getGeografiskTilknytning().getGtType();
        if (GtType.BYDEL.equals(kode))
            return person.getGeografiskTilknytning().getGtBydel();
        if (GtType.KOMMUNE.equals(kode))
            return person.getGeografiskTilknytning().getGtKommune();
        if (GtType.UTLAND.equals(kode))
            return person.getGeografiskTilknytning().getGtLand();
        return null;
    }

}
