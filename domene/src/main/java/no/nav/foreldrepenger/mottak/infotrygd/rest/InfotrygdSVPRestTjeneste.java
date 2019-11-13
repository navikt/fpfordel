package no.nav.foreldrepenger.mottak.infotrygd.rest;

import static java.util.Collections.emptyList;
import static no.nav.foreldrepenger.mottak.infotrygd.rest.SVPInfotrygdRestResponseMapper.svpInfotrygdSaker;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.mottak.infotrygd.InfotrygdSak;
import no.nav.foreldrepenger.mottak.infotrygd.InfotrygdTjeneste;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
@SVP
public class InfotrygdSVPRestTjeneste implements InfotrygdTjeneste {

    private static final String DEFAULT_URI = "http://infotrygd-svangerskapspenger.default/saker";
    private static final Logger LOG = LoggerFactory.getLogger(InfotrygdSVPRestTjeneste.class);

    private OidcRestClient restClient;
    private URI uri;

    InfotrygdSVPRestTjeneste() {
    }

    @Inject
    public InfotrygdSVPRestTjeneste(OidcRestClient restClient,
            @KonfigVerdi(value = "fpfordel.it.svp.url", defaultVerdi = DEFAULT_URI) URI uri) {
        this.restClient = restClient;
        this.uri = uri;
    }

    @Override
    public List<InfotrygdSak> finnSakListe(String fnr, LocalDate fom) {
        try {
            var request = new URIBuilder(uri).addParameter("fnr", fnr).build();
            LOG.info("Slår opp saker fra {}", request);
            var respons = restClient.get(request, Saker.class);
            LOG.info("Fikk saker {}", respons);
            var saker = svpInfotrygdSaker(respons, fom);
            LOG.info("Returnerer saker {}", saker);
            return saker;
        } catch (Exception e) {
            LOG.warn("Feil ved oppslag mot {}, returnerer ingen saker", uri, e);
            return emptyList();
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[restClient=" + restClient + ", uri=" + uri + "]";
    }

}
