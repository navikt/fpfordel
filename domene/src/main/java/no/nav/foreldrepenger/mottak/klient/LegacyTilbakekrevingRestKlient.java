package no.nav.foreldrepenger.mottak.klient;

import java.net.URI;

import javax.inject.Inject;

import no.nav.foreldrepenger.kontrakter.fordel.JournalpostMottakDto;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;
import no.nav.vedtak.konfig.KonfigVerdi;

//@ApplicationScoped
@Deprecated
@Tilbake
public class LegacyTilbakekrevingRestKlient implements JournalpostSender {
    private static final String DEFAULT_TILBAKE_BASE_URI = "http://fptilbake";
    private static final String JOURNALPOST_PATH = "/fptilbake/api/fordel/journalpost";

    private OidcRestClient oidcRestClient;
    private URI endpoint;

    public LegacyTilbakekrevingRestKlient() {
    }

    @Inject
    public LegacyTilbakekrevingRestKlient(OidcRestClient oidcRestClient,
            @KonfigVerdi(value = "fptilbake.base.url", defaultVerdi = DEFAULT_TILBAKE_BASE_URI) URI endpoint) {
        this.oidcRestClient = oidcRestClient;
        this.endpoint = URI.create(endpoint.toString() + JOURNALPOST_PATH);
    }

    @Override
    public void send(JournalpostMottakDto journalpostMottakDto) {
        oidcRestClient.post(endpoint, journalpostMottakDto);

    }
}
