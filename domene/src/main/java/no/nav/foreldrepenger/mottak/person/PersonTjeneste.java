package no.nav.foreldrepenger.mottak.person;

import java.util.Objects;

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
import no.nav.tjeneste.virksomhet.person.v3.binding.HentGeografiskTilknytningPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentGeografiskTilknytningSikkerhetsbegrensing;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.NorskIdent;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.PersonIdent;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Personidenter;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentGeografiskTilknytningRequest;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentGeografiskTilknytningResponse;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonRequest;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonResponse;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.ManglerTilgangFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;
import no.nav.vedtak.felles.integrasjon.aktør.klient.AktørConsumerMedCache;
import no.nav.vedtak.felles.integrasjon.pdl.PdlKlient;
import no.nav.vedtak.felles.integrasjon.pdl.Tema;
import no.nav.vedtak.felles.integrasjon.person.PersonConsumer;
import no.nav.vedtak.util.env.Environment;

@ApplicationScoped
public class PersonTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(PersonTjeneste.class);

    private AktørConsumerMedCache aktørConsumer;
    private PersonConsumer personConsumer;
    private PdlKlient pdlKlient;
    private boolean isProd = Environment.current().isProd();

    PersonTjeneste() {
        // CDI
    }

    @Inject
    public PersonTjeneste(PersonConsumer personConsumer,
                          PdlKlient pdlKlient,
                          AktørConsumerMedCache aktørConsumer) {
        this.personConsumer = personConsumer;
        this.aktørConsumer = aktørConsumer;
        this.pdlKlient = pdlKlient;
    }

    public String hentNavn(String fnr) {
        var navn = brukersNavn(fnr);
        if (isProd && navn != null) {
            try {
                var request = new HentPersonQueryRequest();
                request.setIdent(fnr);
                var projection = new PersonResponseProjection()
                        .navn(new NavnResponseProjection().forkortetNavn());
                var person = pdlKlient.hentPerson(request, projection, Tema.FOR);
                var pdlNavn = person.getNavn().stream().map(Navn::getForkortetNavn).findFirst().orElse(null);
                if (Objects.equals(navn, pdlNavn)) {
                    LOG.info("FPFORDEL PDL navn: sammensatt og forkortet navn likt");
                } else {
                    LOG.info("FPFORDEL PDL navn: ulike navn TPS {} og PDL {}", navn, pdlNavn);
                }
            } catch (Exception e) {
                LOG.info("FPFORDEL PDL navn error", e);
            }
        }
        return navn;
    }

    public GeoTilknytning hentGeografiskTilknytning(String fnr) {
        HentGeografiskTilknytningRequest request = new HentGeografiskTilknytningRequest();
        request.setAktoer(lagPersonIdent(fnr));
        try {
            HentGeografiskTilknytningResponse response = personConsumer.hentGeografiskTilknytning(request);
            String geoTilkn = response.getGeografiskTilknytning() != null
                    ? response.getGeografiskTilknytning().getGeografiskTilknytning()
                    : null;
            String diskKode = response.getDiskresjonskode() != null ? response.getDiskresjonskode().getValue() : null;
            if (isProd) {
                pdlGTLogSammenlign(fnr, geoTilkn, diskKode);
            }
            return new GeoTilknytning(geoTilkn, diskKode);
        } catch (HentGeografiskTilknytningSikkerhetsbegrensing e) {
            throw PersonTjeneste.PersonTjenesteFeil.FACTORY.enhetsTjenesteSikkerhetsbegrensing(e).toException();
        } catch (HentGeografiskTilknytningPersonIkkeFunnet e) {
            throw PersonTjeneste.PersonTjenesteFeil.FACTORY.enhetsTjenestePersonIkkeFunnet(e).toException();
        }
    }

    private void pdlGTLogSammenlign(String fnr, String geoTilkn, String diskKode) {
        try {
            var query = new HentPersonQueryRequest();
            query.setIdent(fnr);
            var projection = new PersonResponseProjection()
                    .geografiskTilknytning(new GeografiskTilknytningResponseProjection().all$())
                    .adressebeskyttelse(new AdressebeskyttelseResponseProjection().gradering());
            var person = pdlKlient.hentPerson(query, projection, Tema.FOR);
            var pdlDiskresjon = getDiskresjonskode(person);
            var pdlTilknytning = getTilknytning(person);
            if (Objects.equals(diskKode, pdlDiskresjon)) {
                LOG.info("FPFORDEL PDL diskkode: like svar");
            } else {
                LOG.info("FPFORDEL PDL diskkode: avvik");
            }
            if (Objects.equals(geoTilkn, pdlTilknytning)) {
                LOG.info("FPFORDEL PDL tilknytning: like svar");
            } else {
                LOG.info("FPFORDEL PDL tilknytning: avvik tps {} pdl {}", geoTilkn, pdlTilknytning);
            }
        } catch (Exception e) {
            LOG.info("FPFORDEL PDL geotilknytning error", e);
        }
    }

    private String brukersNavn(String fnr) {
        if (fnr == null) {
            return null;
        }
        PersonIdent personIdent = new PersonIdent();
        NorskIdent norskIdent = new NorskIdent();
        norskIdent.setIdent(fnr);
        Personidenter type = new Personidenter();
        type.setValue((fnr.charAt(0) >= '4') && (fnr.charAt(0) <= '7') ? "DNR" : "FNR");
        norskIdent.setType(type);
        personIdent.setIdent(norskIdent);
        HentPersonRequest request = new HentPersonRequest();
        request.setAktoer(personIdent);
        try {
            HentPersonResponse response = personConsumer.hentPersonResponse(request);
            return response.getPerson().getPersonnavn().getSammensattNavn();
        } catch (Exception e) {
            throw new IllegalArgumentException("Fant ikke person", e);
        }
    }

    private String getDiskresjonskode(Person person) {
        var kode = person.getAdressebeskyttelse().stream()
                .map(Adressebeskyttelse::getGradering)
                .filter(g -> !AdressebeskyttelseGradering.UGRADERT.equals(g))
                .findFirst().orElse(null);
        if (AdressebeskyttelseGradering.STRENGT_FORTROLIG.equals(kode) || AdressebeskyttelseGradering.STRENGT_FORTROLIG_UTLAND.equals(kode))
            return "SPSF";
        return AdressebeskyttelseGradering.FORTROLIG.equals(kode) ? "SPFO" : null;
    }

    private String getTilknytning(Person person) {
        var kode = person.getGeografiskTilknytning().getGtType();
        if (GtType.BYDEL.equals(kode))
            return person.getGeografiskTilknytning().getGtBydel();
        if (GtType.KOMMUNE.equals(kode))
            return person.getGeografiskTilknytning().getGtKommune();
        if (GtType.UTLAND.equals(kode))
            return person.getGeografiskTilknytning().getGtLand();
        return null;
    }

    private static PersonIdent lagPersonIdent(String fnr) {
        if ((fnr == null) || fnr.isEmpty()) {
            throw new IllegalArgumentException("Fødselsnummer kan ikke være null eller tomt");
        }

        PersonIdent personIdent = new PersonIdent();
        NorskIdent norskIdent = new NorskIdent();
        norskIdent.setIdent(fnr);

        Personidenter type = new Personidenter();
        type.setValue(erDNr(fnr) ? "DNR" : "FNR");
        norskIdent.setType(type);

        personIdent.setIdent(norskIdent);
        return personIdent;
    }

    private static boolean erDNr(String fnr) {
        // D-nummer kan indentifiseres ved at første siffer er 4 større enn hva som
        // finnes i fødselsnumre
        char førsteTegn = fnr.charAt(0);
        return (førsteTegn >= '4') && (førsteTegn <= '7');
    }

    private interface PersonTjenesteFeil extends DeklarerteFeil {

        PersonTjeneste.PersonTjenesteFeil FACTORY = FeilFactory.create(PersonTjeneste.PersonTjenesteFeil.class);

        @TekniskFeil(feilkode = "FP-070668", feilmelding = "Person ikke funnet ved hentGeografiskTilknytning eller relasjoner", logLevel = LogLevel.ERROR)
        Feil enhetsTjenestePersonIkkeFunnet(Exception e);

        @ManglerTilgangFeil(feilkode = "FP-509290", feilmelding = "Mangler tilgang til å utføre hentGeografiskTilknytning eller hentrelasjoner", logLevel = LogLevel.ERROR)
        Feil enhetsTjenesteSikkerhetsbegrensing(Exception e);

    }

}
