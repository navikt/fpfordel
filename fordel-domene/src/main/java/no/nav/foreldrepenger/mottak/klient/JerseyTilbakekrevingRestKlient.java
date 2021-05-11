package no.nav.foreldrepenger.mottak.klient;

import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

import java.net.URI;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.foreldrepenger.kontrakter.fordel.JournalpostMottakDto;
import no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyOidcRestClient;
import no.nav.vedtak.felles.integrasjon.rest.jersey.Jersey;

@Dependent
@Jersey("tilbake")
public class JerseyTilbakekrevingRestKlient extends AbstractJerseyOidcRestClient implements JournalpostSender {
    private static final String DEFAULT_TILBAKE_BASE_URI = "http://fptilbake";
    private static final String JOURNALPOST_PATH = "/fptilbake/api/fordel/journalpost";
    private static final Logger LOG = LoggerFactory.getLogger(JerseyTilbakekrevingRestKlient.class);

    private final URI endpoint;

    @Inject
    public JerseyTilbakekrevingRestKlient(@KonfigVerdi(value = "fptilbake.base.url", defaultVerdi = DEFAULT_TILBAKE_BASE_URI) URI endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public void send(JournalpostMottakDto journalpostMottakDto) {
        LOG.info("Sender journalpost");
        client.target(endpoint)
                .path(JOURNALPOST_PATH)
                .request(APPLICATION_JSON_TYPE)
                .buildPost(json(journalpostMottakDto))
                .invoke(Response.class);
        LOG.info("Sendt journalpost OK");
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [endpoint=" + endpoint + "]";
    }
}
