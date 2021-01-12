package no.nav.foreldrepenger.mottak.klient;

import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

import java.net.URI;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.kontrakter.fordel.JournalpostMottakDto;
import no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyOidcRestClient;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
@Fagsak
public class JerseyDokumentmottakRestKlient extends AbstractJerseyOidcRestClient implements JournalpostSender {
    private static final String DEFAULT_FPSAK_BASE_URI = "http://fpsak";
    private static final String FPSAK_MOTTAK_JOURNALPOST_PATH = "/fpsak/api/fordel/journalpost";

    private URI endpoint;
    private static final Logger LOG = LoggerFactory.getLogger(JerseyDokumentmottakRestKlient.class);

    public JerseyDokumentmottakRestKlient() {
    }

    @Inject
    public JerseyDokumentmottakRestKlient(@KonfigVerdi(value = "fpsak.base.url", defaultVerdi = DEFAULT_FPSAK_BASE_URI) URI endpoint) {
        this(endpoint, new ClientRequestFilter[0]);
    }

    JerseyDokumentmottakRestKlient(String endpoint, ClientRequestFilter... filters) {
        this(URI.create(endpoint), filters);
    }

    public JerseyDokumentmottakRestKlient(URI endpoint, ClientRequestFilter... filters) {
        super(filters);
        this.endpoint = endpoint;
    }

    @Override
    public void send(JournalpostMottakDto journalpostMottakDto) {
        LOG.info("Sender journalpost");
        client.target(endpoint)
                .path(FPSAK_MOTTAK_JOURNALPOST_PATH)
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
