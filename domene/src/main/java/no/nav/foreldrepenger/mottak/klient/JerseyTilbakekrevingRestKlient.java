package no.nav.foreldrepenger.mottak.klient;

import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

import java.net.URI;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.client.ClientRequestFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.kontrakter.fordel.JournalpostMottakDto;
import no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyOidcRestClient;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
@Tilbake
public class JerseyTilbakekrevingRestKlient extends AbstractJerseyOidcRestClient implements JournalpostSender {
    private static final String DEFAULT_TILBAKE_BASE_URI = "http://fptilbake";
    private static final String JOURNALPOST_PATH = "/fptilbake/api/fordel/journalpost";
    private static final Logger LOG = LoggerFactory.getLogger(JerseyTilbakekrevingRestKlient.class);

    private URI endpoint;

    public JerseyTilbakekrevingRestKlient() {
    }

    @Inject
    public JerseyTilbakekrevingRestKlient(@KonfigVerdi(value = "fptilbake.base.url", defaultVerdi = DEFAULT_TILBAKE_BASE_URI) URI endpoint) {
        this(endpoint, new ClientRequestFilter[0]);
    }

    JerseyTilbakekrevingRestKlient(URI endpoint, ClientRequestFilter... filters) {
        super(filters);
        this.endpoint = endpoint;
    }

    @Override
    public void send(JournalpostMottakDto journalpostMottakDto) {
        LOG.info("Sender journalpost");
        client.target(endpoint)
                .path(JOURNALPOST_PATH)
                .request(APPLICATION_JSON_TYPE)
                .buildPost(json(journalpostMottakDto))
                .invoke(Void.class);
        LOG.info("Sendt journalpost OK");
    }
}
