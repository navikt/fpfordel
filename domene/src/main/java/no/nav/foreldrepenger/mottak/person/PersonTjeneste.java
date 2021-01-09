package no.nav.foreldrepenger.mottak.person;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.pdl.Adressebeskyttelse;
import no.nav.pdl.AdressebeskyttelseGradering;
import no.nav.pdl.AdressebeskyttelseResponseProjection;
import no.nav.pdl.GeografiskTilknytningResponseProjection;
import no.nav.pdl.GtType;
import no.nav.pdl.HentIdenterQueryRequest;
import no.nav.pdl.HentPersonQueryRequest;
import no.nav.pdl.IdentGruppe;
import no.nav.pdl.IdentInformasjon;
import no.nav.pdl.IdentInformasjonResponseProjection;
import no.nav.pdl.IdentlisteResponseProjection;
import no.nav.pdl.Navn;
import no.nav.pdl.NavnResponseProjection;
import no.nav.pdl.Person;
import no.nav.pdl.PersonResponseProjection;
import no.nav.vedtak.felles.integrasjon.pdl.PdlKlient;
import no.nav.vedtak.felles.integrasjon.pdl.Tema;
import no.nav.vedtak.util.LRUCache;

@ApplicationScoped
public class PersonTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(PersonTjeneste.class);

    private static final int DEFAULT_CACHE_SIZE = 1000;
    private static final long DEFAULT_CACHE_TIMEOUT = TimeUnit.MILLISECONDS.convert(2, TimeUnit.HOURS);

    private LRUCache<String, String> cacheAktørIdTilIdent;
    private LRUCache<String, String> cacheIdentTilAktørId;

    private PdlKlient pdlKlient;

    PersonTjeneste() {
        // CDI
    }

    @Inject
    public PersonTjeneste(PdlKlient pdlKlient) {
        this.pdlKlient = pdlKlient;
        this.cacheAktørIdTilIdent = new LRUCache<>(DEFAULT_CACHE_SIZE, DEFAULT_CACHE_TIMEOUT);
        this.cacheIdentTilAktørId = new LRUCache<>(DEFAULT_CACHE_SIZE, DEFAULT_CACHE_TIMEOUT);
    }

    public Optional<String> hentAktørIdForPersonIdent(String personIdent) {
        var fraCache = cacheIdentTilAktørId.get(personIdent);
        if (fraCache != null) {
            return Optional.of(fraCache);
        }
        Optional<String> aktørId = hentIdentFraGruppe(personIdent, IdentGruppe.AKTORID);
        aktørId.ifPresent(a -> cacheIdentTilAktørId.put(personIdent, a));
        return aktørId;
    }

    public Optional<String> hentPersonIdentForAktørId(String aktørId) {
        var fraCache = cacheAktørIdTilIdent.get(aktørId);
        if (fraCache != null) {
            return Optional.of(fraCache);
        }
        Optional<String> ident = hentIdentFraGruppe(aktørId, IdentGruppe.FOLKEREGISTERIDENT);
        ident.ifPresent(i -> {
            cacheAktørIdTilIdent.put(aktørId, i);
            cacheIdentTilAktørId.put(i, aktørId); // OK her, men ikke over ettersom dette er gjeldende mapping
        });
        return ident;
    }

    public String hentNavn(String aktørId) {
        var request = new HentPersonQueryRequest();
        request.setIdent(aktørId);
        var projection = new PersonResponseProjection()
                .navn(new NavnResponseProjection().forkortetNavn().fornavn().mellomnavn().etternavn());

        var person = pdlKlient.hentPerson(request, projection, Tema.FOR);

        return person.getNavn().stream().map(PersonTjeneste::mapNavn).findFirst().orElseThrow();
    }

    // OBS: Ikke bruk denne!!! PDL kommer til å lage et nytt skjema og nytt kall
    // hentGeografiskTilknytning.
    public GeoTilknytning hentGeografiskTilknytning(String aktørId) {
        var query = new HentPersonQueryRequest();
        query.setIdent(aktørId);
        var projection = new PersonResponseProjection()
                .geografiskTilknytning(new GeografiskTilknytningResponseProjection().gtType().gtBydel().gtKommune().gtLand())
                .adressebeskyttelse(new AdressebeskyttelseResponseProjection().gradering());

        var person = pdlKlient.hentPerson(query, projection, Tema.FOR);

        var gt = new GeoTilknytning(getTilknytning(person), getDiskresjonskode(person));
        if (gt.getTilknytning() == null) {
            LOG.info("FPFORDEL PDL mangler GT for {}", aktørId);
        }
        return gt;
    }

    private Optional<String> hentIdentFraGruppe(String ident, IdentGruppe type) {
        var request = new HentIdenterQueryRequest();
        request.setIdent(ident);
        request.setGrupper(List.of(type));
        request.setHistorikk(Boolean.FALSE);
        var projection = new IdentlisteResponseProjection()
                .identer(new IdentInformasjonResponseProjection().ident());

        var identliste = pdlKlient.hentIdenter(request, projection, Tema.FOR);

        if (identliste.getIdenter().size() > 1) {
            LOG.info("FPFORDEL PDL flere enn en ident for {}", ident);
        }

        return identliste.getIdenter().stream().findFirst().map(IdentInformasjon::getIdent);
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
