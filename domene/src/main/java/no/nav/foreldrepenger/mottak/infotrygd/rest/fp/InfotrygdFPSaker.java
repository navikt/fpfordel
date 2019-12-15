package no.nav.foreldrepenger.mottak.infotrygd.rest.fp;

import static no.nav.foreldrepenger.fordel.kodeverdi.RelatertYtelseBehandlingstema.FORELDREPENGER_FODSEL_BEHANDLINGSTEMA;

import java.net.URI;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.mottak.infotrygd.rest.InfotrygdRestResponseMapper;
import no.nav.foreldrepenger.mottak.infotrygd.rest.InfotrygdSaker;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
@FP
public class InfotrygdFPSaker extends InfotrygdSaker {

    private static final String DEFAULT_URI = "http://infotrygd-foreldrepenger.default/saker";

    InfotrygdFPSaker() {
    }

    @Inject
    public InfotrygdFPSaker(OidcRestClient restClient,
            @KonfigVerdi(value = "fpfordel.it.fp.url", defaultVerdi = DEFAULT_URI) URI uri) {
        super(restClient, uri, new InfotrygdRestResponseMapper(FORELDREPENGER_FODSEL_BEHANDLINGSTEMA));
    }
}
