package no.nav.foreldrepenger.mottak.infotrygd.rest;

import static java.util.Collections.emptyList;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.mottak.infotrygd.InfotrygdSak;
import no.nav.foreldrepenger.mottak.infotrygd.InfotrygdTjeneste;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;

public abstract class InfotrygdRESTTjeneste implements InfotrygdTjeneste {
    private static final Logger LOG = LoggerFactory.getLogger(InfotrygdRESTTjeneste.class);

    private OidcRestClient restClient;
    private URI uri;
    private InfotrygdRestResponseMapper mapper;

    public InfotrygdRESTTjeneste(OidcRestClient restClient, URI uri, InfotrygdRestResponseMapper mapper) {
        this.restClient = restClient;
        this.uri = uri;
        this.mapper = mapper;
    }

    public InfotrygdRESTTjeneste() {

    }

    @Override
    public List<InfotrygdSak> finnSakListe(String fnr, LocalDate fom) {
        try {
            var request = new URIBuilder(uri)
                    .addParameter("fnr", fnr)
                    .addParameter("fom", fom(fom))
                    .build();
            LOG.info("fpfordel infotrygd rest. Slår opp saker fra {} for {}", uri, getBehandlingsTema());
            var respons = restClient.get(request, Saker.class);
            LOG.info("fpfordel infotrygd rest {}. Fikk saker {}", getBehandlingsTema(), respons);
            var saker = mapper.map(respons);
            LOG.info("fpfordel infotrygd rest {}. Mappet saker {}", getBehandlingsTema(), saker);
            return saker;
        } catch (Exception e) {
            LOG.warn("Feil ved oppslag mot {}, returnerer ingen saker", uri, e);
            return emptyList();
        }
    }

    private static String fom(LocalDate fom) {
        return DateTimeFormatter.ISO_LOCAL_DATE.format(fom);
    }

    private String getBehandlingsTema() {
        return mapper.getBehandlingstema();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[restClient=" + restClient + ", uri=" + uri + "]";
    }

}
