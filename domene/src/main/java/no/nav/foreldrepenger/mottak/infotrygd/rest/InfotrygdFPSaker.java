package no.nav.foreldrepenger.mottak.infotrygd.rest;

import java.net.URI;

import javax.inject.Inject;

import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;
import no.nav.vedtak.konfig.KonfigVerdi;

//@ApplicationScoped
@Deprecated
public class InfotrygdFPSaker extends InfotrygdSaker {

    private static final String DEFAULT_URI = "http://infotrygd-foreldrepenger.default/saker";

    InfotrygdFPSaker() {
        super();
    }

    @Inject
    public InfotrygdFPSaker(OidcRestClient restClient,
            @KonfigVerdi(value = "fpfordel.it.fp.url", defaultVerdi = DEFAULT_URI) URI uri) {
        super(restClient, uri, new InfotrygdRestResponseMapper());
    }
}
