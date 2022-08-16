package no.nav.foreldrepenger.mottak.behandlendeenhet.nom;

import java.net.URI;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.vedtak.felles.integrasjon.rest.AzureADRestClient;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.konfig.KonfigVerdi;

/*
 * Klient for å sjekke om person er skjermet.
 * Grensesnitt https://skjermede-personer-pip.<intern-standard-dev-uri>/swagger-ui
 */
@ApplicationScoped
public class SkjermetPersonKlient {

    private static final String DEFAULT_URI_ONPREM = "http://skjermede-personer-pip.nom/skjermet";

    private static final String DEFAULT_URI = "https://skjermede-personer-pip.intern.nav.no/skjermet";
    private static final String DEFAULT_AZURE_SCOPE = "api://prod-gcp.nom.skjermede-personer-pip/.default";

    private static final Logger LOG = LoggerFactory.getLogger(SkjermetPersonKlient.class);

    private URI uriGcp;
    private URI uriOnprem;

    private AzureADRestClient clientGcp;

    private OidcRestClient clientOnprem;

    
    @Inject
    public SkjermetPersonKlient(@KonfigVerdi(value = "skjermet.person.rs.url.onprem", defaultVerdi = DEFAULT_URI_ONPREM) URI uriOnprem,
                                @KonfigVerdi(value = "skjermet.person.rs.url", defaultVerdi = DEFAULT_URI) URI uriGcp,
                                @KonfigVerdi(value = "skjermet.person.rs.azure.scope", defaultVerdi = DEFAULT_AZURE_SCOPE) String scope,
                                OidcRestClient opremClient) {
        this.uriGcp = uriGcp;
        this.uriOnprem = uriOnprem;
        this.clientOnprem = opremClient;
        this.clientGcp = AzureADRestClient.builder().scope(scope).build();
    }

    public SkjermetPersonKlient() {
        // CDI
    }


    public boolean erSkjermet(String fnr) {
        if (fnr == null) return false;
        var request = new SkjermetRequestDto(fnr);
        var skjermet = clientOnprem.post(uriOnprem, request);
        sjekkGcp(skjermet, request);
        if ("true".equalsIgnoreCase(skjermet)) {
            LOG.info("FPFORDEL skjermet person funnet");
            return true;
        }
        return false;
    }

    private void sjekkGcp(String onpremRespons, SkjermetRequestDto request) {
        try {
            var gcpRespons = clientGcp.post(uriGcp, request);
            if (onpremRespons.equalsIgnoreCase(gcpRespons)) {
                LOG.info("SkjermetPersonKlient gir likt resultat i gcp og onPrem");
            } else {
                LOG.info("SkjermetPersonKlient gir avvikende resultat i gcp {} og onPrem {}", gcpRespons, onpremRespons);
            }
        } catch (Exception e) {
            LOG.info("SkjermetPersonKlient gir exception mot gcp", e);
        }
    }

    private record SkjermetRequestDto(String personident) {}

}
