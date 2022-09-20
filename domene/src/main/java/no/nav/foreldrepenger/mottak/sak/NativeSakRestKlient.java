package no.nav.foreldrepenger.mottak.sak;

import java.net.URI;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;

import no.nav.vedtak.felles.integrasjon.rest.NativeClient;
import no.nav.vedtak.felles.integrasjon.rest.NavHeaders;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;

@NativeClient
@ApplicationScoped
@RestClientConfig(tokenConfig = TokenFlow.STS_CC, endpointProperty = "sak.rs.url", endpointDefault = "http://sak.default/api/v1/saker")
public class NativeSakRestKlient implements SakClient {

    private RestClient sender;
    private URI endpoint;

    public NativeSakRestKlient() {
    }

    @Inject
    public NativeSakRestKlient(RestClient sender) {
        this.sender = sender;
        this.endpoint = RestConfig.endpointFromAnnotation(NativeSakRestKlient.class);
    }

    @Override
    public SakJson hentSakId(String sakId) {
        var target = UriBuilder.fromUri(endpoint).path(sakId).build();
        var request = RestRequest.newGET(target, NativeSakRestKlient.class)
            .otherCallId(NavHeaders.HEADER_NAV_CORRELATION_ID);
        return sender.send(request, SakJson.class);
    }

}
