package no.nav.foreldrepenger.mottak.infotrygd.rest;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.mottak.infotrygd.InfotrygdSak;
import no.nav.foreldrepenger.mottak.infotrygd.InfotrygdTjeneste;
import no.nav.vedtak.felles.integrasjon.infotrygd.saker.v1.respons.Saker;
import no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyOidcRestClient;
import no.nav.vedtak.felles.integrasjon.rest.jersey.Jersey;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
@Jersey
public class JerseyInfotrygdFPSaker extends AbstractJerseyOidcRestClient implements InfotrygdTjeneste {

    private static final String DEFAULT_URI = "http://infotrygd-foreldrepenger.default/saker";

    private URI endpoint;

    private InfotrygdRestResponseMapper mapper;

    JerseyInfotrygdFPSaker() {
    }

    @Inject
    public JerseyInfotrygdFPSaker(@KonfigVerdi(value = "fpfordel.it.fp.url", defaultVerdi = DEFAULT_URI) URI endpoint) {
        this.endpoint = endpoint;
        this.mapper = new InfotrygdRestResponseMapper();
    }

    @Override
    public List<InfotrygdSak> finnSakListe(String fnr, LocalDate fom) {
        return mapper.map(client.target(endpoint)
                .queryParam("fnr", fnr)
                .queryParam("fom", fom(fom))
                .request(APPLICATION_JSON_TYPE)
                .get(Saker.class));
    }

    private static String fom(LocalDate fom) {
        return ISO_LOCAL_DATE.format(fom);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [endpoint=" + endpoint + ", mapper=" + mapper + "]";
    }

}
