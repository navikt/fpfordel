package no.nav.foreldrepenger.mottak.sak;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.UriBuilder;

import no.nav.vedtak.felles.integrasjon.rest.NavHeaders;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;

@ApplicationScoped
@RestClientConfig(tokenConfig = TokenFlow.STS_CC, endpointProperty = "sak.rs.url", endpointDefault = "http://sak.default/api/v1/saker")
public class SakRestKlient implements SakClient {

    private final RestClient sender;
    private final RestConfig restConfig;

    public SakRestKlient() {
        this.sender = RestClient.client();
        this.restConfig = RestConfig.forClient(this.getClass());
    }

    @Override
    public SakJson hentSakId(String sakId) {
        var target = UriBuilder.fromUri(restConfig.endpoint()).path(sakId).build();
        var request = RestRequest.newGET(target, restConfig)
            .otherCallId(NavHeaders.HEADER_NAV_CORRELATION_ID);
        return sender.send(request, SakJson.class);
    }

}
