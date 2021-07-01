package no.nav.foreldrepenger.mottak.infotrygd;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.vedtak.felles.integrasjon.infotrygd.saker.v1.respons.Saker;
import no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyOidcRestClient;

@Dependent
class InfotrygdFPSaker extends AbstractJerseyOidcRestClient implements InfotrygdTjeneste {

    private static final String DEFAULT_URI = "http://infotrygd-foreldrepenger.default/saker";
    private static final Logger LOG = LoggerFactory.getLogger(InfotrygdFPSaker.class);
    private URI endpoint;

    private final InfotrygdRestResponseMapper mapper;

    @Inject
    public InfotrygdFPSaker(@KonfigVerdi(value = "fpfordel.it.fp.url", defaultVerdi = DEFAULT_URI) URI endpoint) {
        this.endpoint = endpoint;
        this.mapper = new InfotrygdRestResponseMapper();
    }

    @Override
    public List<InfotrygdSak> finnSakListe(String fnr, LocalDate fom) {
        LOG.trace("Henter saksliste fra infotrygd");
        var liste = mapper.map(invoke(client.target(endpoint)
                .queryParam("fnr", fnr)
                .queryParam("fom", fom(fom))
                .request(APPLICATION_JSON_TYPE).buildGet(), Saker.class));
        LOG.info("Hentet saksliste med {} saker fra infotrygd OK", liste.size());
        return liste;
    }

    private static String fom(LocalDate fom) {
        return ISO_LOCAL_DATE.format(fom);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [endpoint=" + endpoint + ", mapper=" + mapper + "]";
    }

}
