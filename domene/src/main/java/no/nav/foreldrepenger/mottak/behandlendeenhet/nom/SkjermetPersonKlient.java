package no.nav.foreldrepenger.mottak.behandlendeenhet.nom;

import java.net.URI;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;

/*
 * Klient for å sjekke om person er skjermet.
 * Grensesnitt se #skjermingsløsningen
 */
@ApplicationScoped
@RestClientConfig(tokenConfig = TokenFlow.AZUREAD_CC, endpointProperty = "skjermet.person.rs.url", endpointDefault = "https://skjermede-personer-pip.intern.nav.no/skjermet",
    scopesProperty = "skjermet.person.rs.azure.scope", scopesDefault = "api://prod-gcp.nom.skjermede-personer-pip/.default")
public class SkjermetPersonKlient {

    private static final Logger LOG = LoggerFactory.getLogger(SkjermetPersonKlient.class);

    private URI uri;

    private RestClient client;


    @Inject
    public SkjermetPersonKlient(RestClient client) {
        this.uri = RestConfig.endpointFromAnnotation(SkjermetPersonKlient.class);
        this.client = client;
    }

    public SkjermetPersonKlient() {
        // CDI
    }


    public boolean erSkjermet(String fnr) {
        if (fnr == null) return false;
        var request = RestRequest.newPOSTJson(new SkjermetRequestDto(fnr), uri, SkjermetPersonKlient.class);
        var respons = client.sendReturnOptional(request, String.class).orElse("");
        if ("true".equalsIgnoreCase(respons)) {
            LOG.info("FPFORDEL skjermet person funnet");
            return true;
        }
        return false;
    }

    private record SkjermetRequestDto(String personident) {}

}
