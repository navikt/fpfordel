package no.nav.foreldrepenger.mottak.behandlendeenhet;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.fordel.kodeverdi.BehandlingTema;
import no.nav.foreldrepenger.fordel.kodeverdi.Tema;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentGeografiskTilknytningPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentGeografiskTilknytningSikkerhetsbegrensing;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Informasjonsbehov;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.NorskIdent;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Person;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.PersonIdent;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Personidenter;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentGeografiskTilknytningRequest;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentGeografiskTilknytningResponse;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonRequest;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonResponse;
import no.nav.vedtak.felles.integrasjon.person.PersonConsumer;
import no.nav.vedtak.util.FPDateUtil;

@ApplicationScoped
public class EnhetsTjeneste {

    private PersonConsumer personConsumer;
    private ArbeidsfordelingTjeneste arbeidsfordelingTjeneste;

    private LocalDate sisteInnhenting = LocalDate.MIN;
    private static final String DISKRESJON_K6 = "SPSF";
    private List<String> alleJournalførendeEnheter;
    private List<String> nfpJournalførendeEnheter;

    public EnhetsTjeneste() {
    }

    @Inject
    public EnhetsTjeneste(PersonConsumer personConsumer,
                              ArbeidsfordelingTjeneste arbeidsfordelingTjeneste) {
        this.personConsumer = personConsumer;
        this.arbeidsfordelingTjeneste = arbeidsfordelingTjeneste;
    }

    private String hentEnhetId(String fnr, BehandlingTema behandlingTema, Tema tema) {
        GeoTilknytning geoTilknytning = hentGeografiskTilknytning(fnr);

        String aktivDiskresjonskode = geoTilknytning.getDiskresjonskode();
        if (!DISKRESJON_K6.equals(aktivDiskresjonskode)) {
            boolean relasjonMedK6 = hentDiskresjonskoderForFamilierelasjoner(fnr).stream()
                    .anyMatch(geo -> DISKRESJON_K6.equals(geo.getDiskresjonskode()));
            if (relasjonMedK6) {
                aktivDiskresjonskode = DISKRESJON_K6;
            }
        }

        return arbeidsfordelingTjeneste.finnBehandlendeEnhetId(geoTilknytning.getTilknytning(), aktivDiskresjonskode,
                behandlingTema, tema);
    }

    public String hentFordelingEnhetId(Tema tema, BehandlingTema behandlingTema, Optional<String> enhetInput,
                                       Optional<String> fnr) {
        oppdaterEnhetCache();
        if (enhetInput.isPresent()) {
            for (String oEnhet : alleJournalførendeEnheter) {
                if (enhetInput.get().equals(oEnhet)) {
                    return oEnhet;
                }
            }
        }
        if (fnr.isPresent()) {
            return hentEnhetId(fnr.get(), behandlingTema, tema);
        } else {
            return nfpJournalførendeEnheter.get(LocalDateTime.now().getSecond() % nfpJournalførendeEnheter.size());
        }
    }

    private void oppdaterEnhetCache() {
        if (sisteInnhenting.isBefore(FPDateUtil.iDag())) {
            alleJournalførendeEnheter = arbeidsfordelingTjeneste
                    .finnAlleJournalførendeEnhetIdListe(BehandlingTema.UDEFINERT, true);
            nfpJournalførendeEnheter = arbeidsfordelingTjeneste
                    .finnAlleJournalførendeEnhetIdListe(BehandlingTema.UDEFINERT, false);
            sisteInnhenting = FPDateUtil.iDag();
        }
    }

    private GeoTilknytning hentGeografiskTilknytning(String fnr) {
        HentGeografiskTilknytningRequest request = new HentGeografiskTilknytningRequest();
        request.setAktoer(lagPersonIdent(fnr));
        try {
            HentGeografiskTilknytningResponse response = personConsumer.hentGeografiskTilknytning(request);
            String geoTilkn = response.getGeografiskTilknytning() != null
                    ? response.getGeografiskTilknytning().getGeografiskTilknytning()
                    : null;
            String diskKode = response.getDiskresjonskode() != null ? response.getDiskresjonskode().getValue() : null;
            return new GeoTilknytning(geoTilkn, diskKode);
        } catch (HentGeografiskTilknytningSikkerhetsbegrensing e) {
            throw EnhetsTjenesteFeil.FACTORY.enhetsTjenesteSikkerhetsbegrensing(e).toException();
        } catch (HentGeografiskTilknytningPersonIkkeFunnet e) {
            throw EnhetsTjenesteFeil.FACTORY.enhetsTjenestePersonIkkeFunnet(e).toException();
        }
    }

    private List<GeoTilknytning> hentDiskresjonskoderForFamilierelasjoner(String fnr) {
        HentPersonRequest request = new HentPersonRequest();
        request.setAktoer(lagPersonIdent(fnr));
        request.getInformasjonsbehov().add(Informasjonsbehov.FAMILIERELASJONER);
        try {
            HentPersonResponse response = personConsumer.hentPersonResponse(request);
            Person person = response.getPerson();
            return tilDiskresjonsKoder(person);
        } catch (HentPersonPersonIkkeFunnet e) {
            throw EnhetsTjenesteFeil.FACTORY.enhetsTjenestePersonIkkeFunnet(e).toException();
        } catch (HentPersonSikkerhetsbegrensning e) {
            throw EnhetsTjenesteFeil.FACTORY.enhetsTjenesteSikkerhetsbegrensing(e).toException();
        }
    }

    private List<GeoTilknytning> tilDiskresjonsKoder(Person person) {
        List<String> foreldreKoder = Arrays.asList("MORA", "FARA");

        return person.getHarFraRolleI().stream()
                .filter(rel -> !foreldreKoder.contains(rel.getTilRolle().getValue()))
                .map(this::relasjonTilGeoMedDiskresjonForKode6)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private GeoTilknytning relasjonTilGeoMedDiskresjonForKode6(
            no.nav.tjeneste.virksomhet.person.v3.informasjon.Familierelasjon familierelasjon) {
        Person person = familierelasjon.getTilPerson();

        if (person.getDiskresjonskode() != null && DISKRESJON_K6.equals(person.getDiskresjonskode().getValue())) {
            return new GeoTilknytning(null, person.getDiskresjonskode().getValue());
        }
        return null;
    }

    private static PersonIdent lagPersonIdent(String fnr) {
        if (fnr == null || fnr.isEmpty()) {
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
        return førsteTegn >= '4' && førsteTegn <= '7';
    }
}
