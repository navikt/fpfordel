package no.nav.foreldrepenger.mottak.behandlendeenhet.nom;

import java.net.URI;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.vedtak.felles.integrasjon.rest.AzureADRestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.konfig.KonfigVerdi;

/*
 * Klient for å sjekke om person er skjermet.
 * Grensesnitt se #skjermingsløsningen
 */
@ApplicationScoped
public class SkjermetPersonKlient {

    private static final String DEFAULT_URI = "https://skjermede-personer-pip.intern.nav.no/skjermet";
    private static final String DEFAULT_AZURE_SCOPE = "api://prod-gcp.nom.skjermede-personer-pip/.default";

    private static final Logger LOG = LoggerFactory.getLogger(SkjermetPersonKlient.class);

    private URI uri;

    private AzureADRestClient client;

    
    @Inject
    public SkjermetPersonKlient(@KonfigVerdi(value = "skjermet.person.rs.url", defaultVerdi = DEFAULT_URI) URI uri,
                                @KonfigVerdi(value = "skjermet.person.rs.azure.scope", defaultVerdi = DEFAULT_AZURE_SCOPE) String scope) {
        this.uri = uri;
        this.client = AzureADRestClient.builder().scope(scope).build();
    }

    public SkjermetPersonKlient() {
        // CDI
    }


    public boolean erSkjermet(String fnr) {
        if (fnr == null) return false;
        var request = new SkjermetRequestDto(fnr);
        var respons = client.post(uri, request);
        if ("true".equalsIgnoreCase(respons)) {
            LOG.info("FPFORDEL skjermet person funnet");
            return true;
        }
        return false;
    }

    private record SkjermetRequestDto(String personident) {}

}
