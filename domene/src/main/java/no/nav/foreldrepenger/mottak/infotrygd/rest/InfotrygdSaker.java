package no.nav.foreldrepenger.mottak.infotrygd.rest;

import static java.util.Collections.emptyList;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.mottak.infotrygd.InfotrygdSak;
import no.nav.foreldrepenger.mottak.infotrygd.InfotrygdTjeneste;
import no.nav.vedtak.felles.integrasjon.infotrygd.saker.v1.InfotrygdSakerConsumer;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;

public abstract class InfotrygdSaker extends InfotrygdSakerConsumer implements InfotrygdTjeneste {
    private static final Logger LOG = LoggerFactory.getLogger(InfotrygdSaker.class);

    private URI uri;
    private InfotrygdRestResponseMapper mapper;

    public InfotrygdSaker(OidcRestClient restClient, URI uri, InfotrygdRestResponseMapper mapper) {
        super(restClient, uri);
        this.uri = uri;
        this.mapper = mapper;
    }

    public InfotrygdSaker() {
        super();
    }

    @Override
    public List<InfotrygdSak> finnSakListe(String fnr, LocalDate fom) {
        try {
            LOG.trace("fpfordel infotrygd rest. Slår opp saker fra {}", uri);
            var respons = getSaker(fnr, fom);
            LOG.trace("fpfordel infotrygd rest. Fikk saker {}", respons);
            var saker = mapper.map(respons);
            LOG.trace("fpfordel infotrygd rest. Mappet saker {}", saker);
            return saker;
        } catch (Exception e) {
            LOG.warn("Feil ved oppslag mot {}, returnerer ingen saker", uri, e);
            return emptyList();
        }
    }

}
