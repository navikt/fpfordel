package no.nav.foreldrepenger.fordel.web.app.selftest.checks;

import java.net.URI;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;
import no.nav.vedtak.konfig.KonfigVerdi;

//@ApplicationScoped
public class InfotrygdSakRestHealthCheck extends WebServiceHealthCheck {
    private static final String DEFAULT_URI = "http://infotrygd-svangerskapspenger.default/actuator/health";
    private static final Logger LOG = LoggerFactory.getLogger(InfotrygdSakRestHealthCheck.class);

    private OidcRestClient restClient;
    private URI uri;

    InfotrygdSakRestHealthCheck() {
        // For CDI proxy
    }

    @Inject
    public InfotrygdSakRestHealthCheck(OidcRestClient restClient,
            @KonfigVerdi(value = "fpfordel.it.svp.selftest.url", defaultVerdi = DEFAULT_URI) URI uri) {
        this.restClient = restClient;
        this.uri = uri;
    }

    @Override
    protected void performWebServiceSelftest() {
        LOG.trace("Tester IT REST SVP {}", uri);
        restClient.get(uri, String.class);
    }

    @Override
    protected String getDescription() {
        return "Test av REST service Infotrygd SVP";
    }

    @Override
    protected String getEndpoint() {
        return uri.toString();
    }

    @Override
    public boolean erKritiskTjeneste() {
        return false;
    }
}
