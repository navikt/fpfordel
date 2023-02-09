package no.nav.foreldrepenger.mottak.klient;

import no.nav.vedtak.felles.integrasjon.rest.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.List;

@RestClientConfig(tokenConfig = TokenFlow.AZUREAD_CC, application = FpApplication.FPLOS)
@ApplicationScoped
public class LosKlient implements Los {

    private static final Logger LOG = LoggerFactory.getLogger(LosKlient.class);

    private final URI enheterEndepunkt;
    private final RestClient klient;
    private final RestConfig restConfig;

    public LosKlient() {
        this.klient = RestClient.client();
        this.restConfig = RestConfig.forClient(this.getClass());
        var endpoint = restConfig.fpContextPath();
        this.enheterEndepunkt = lagURI(endpoint);
    }

    @Override
    public List<TilhørendeEnhetDto> hentTilhørendeEnheter(String ident) {
        LOG.info("Henter alle enheter for en saksbehandler");
        var target = UriBuilder.fromUri(enheterEndepunkt).queryParam("ident", ident).build();
        var request = RestRequest.newGET(target, restConfig);
        return klient.sendReturnList(request, TilhørendeEnhetDto.class);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [endpoint=" + enheterEndepunkt + "]";
    }

    private URI lagURI(URI context) {
        return UriBuilder.fromUri(context).path("/api/saksbehandler/enhet").build();
    }

}