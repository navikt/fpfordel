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

public abstract class InfotrygdSaker implements InfotrygdTjeneste {
    private static final Logger LOG = LoggerFactory.getLogger(InfotrygdSaker.class);

    private OidcRestClient restClient;
    private URI uri;
    private InfotrygdRestResponseMapper mapper;

    public InfotrygdSaker(OidcRestClient restClient, URI uri, InfotrygdRestResponseMapper mapper) {
        this.restClient = restClient;
        this.uri = uri;
        this.mapper = mapper;
    }

    public InfotrygdSaker() {

    }

    @Override
    public List<InfotrygdSak> finnSakListe(String fnr, LocalDate fom) {
        try {
            var request = new URIBuilder(uri)
                    .addParameter("fnr", fnr)
                    .addParameter("fom", fom(fom))
                    .build();
            LOG.trace("fpfordel infotrygd rest. Slår opp saker fra {}", uri);
            var respons = restClient.get(request, Saker.class);
            LOG.trace("fpfordel infotrygd rest. Fikk saker {}", respons);
            var saker = mapper.map(respons);
            LOG.trace("fpfordel infotrygd rest. Mappet saker {}", saker);
            return saker;
        } catch (Exception e) {
            LOG.warn("Feil ved oppslag mot {}, returnerer ingen saker", uri, e);
            return emptyList();
        }
    }

    private static String fom(LocalDate fom) {
        return DateTimeFormatter.ISO_LOCAL_DATE.format(fom);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[restClient=" + restClient + ", uri=" + uri + "]";
    }

}
