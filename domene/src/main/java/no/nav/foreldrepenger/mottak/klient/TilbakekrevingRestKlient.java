package no.nav.foreldrepenger.mottak.klient;

import java.net.URI;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.kontrakter.fordel.JournalpostMottakDto;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
public class TilbakekrevingRestKlient {
    private static final String ENDPOINT_KEY = "tilbake_mottaJournalpost.url";
    private static final String DEFAULT_URI = "http://fptilbake/fptilbake/api/fordel/journalpost";

    private OidcRestClient oidcRestClient;
    private URI endpoint;

    public TilbakekrevingRestKlient() {
    }

    @Inject
    public TilbakekrevingRestKlient(OidcRestClient oidcRestClient,
            @KonfigVerdi(value = ENDPOINT_KEY, defaultVerdi = DEFAULT_URI) URI endpoint) {
        this.oidcRestClient = oidcRestClient;
        this.endpoint = endpoint;
    }

    public void send(JournalpostMottakDto journalpostMottakDto) {
        oidcRestClient.post(endpoint, journalpostMottakDto);

    }
}
