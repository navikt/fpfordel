package no.nav.foreldrepenger.mottak.klient;

import static io.github.resilience4j.retry.Retry.decorateFunction;
import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

import java.net.SocketTimeoutException;
import java.net.URI;
import java.time.Duration;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.foreldrepenger.kontrakter.fordel.JournalpostMottakDto;
import no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyOidcRestClient;
import no.nav.vedtak.felles.integrasjon.rest.jersey.Jersey;

@Dependent
@Jersey("tilbake")
public class JerseyTilbakekrevingKlient extends AbstractJerseyOidcRestClient implements JournalpostSender {
    private static final String DEFAULT_TILBAKE_BASE_URI = "http://fptilbake";
    private static final String JOURNALPOST_PATH = "/fptilbake/api/fordel/journalpost";
    private static final Logger LOG = LoggerFactory.getLogger(JerseyTilbakekrevingKlient.class);

    private final URI endpoint;
    private final RetryRegistry registry;

    @Inject
    public JerseyTilbakekrevingKlient(@KonfigVerdi(value = "fptilbake.base.url", defaultVerdi = DEFAULT_TILBAKE_BASE_URI) URI endpoint) {
        this.endpoint = endpoint;
        this.registry = registry();
    }

    @Override
    public void send(JournalpostMottakDto journalpostMottakDto) {
        decorateFunction(registry.retry("journalpost"), (JournalpostMottakDto dto) -> {
            LOG.info("Sender journalpost");
            client.target(endpoint)
                    .path(JOURNALPOST_PATH)
                    .request(APPLICATION_JSON_TYPE)
                    .buildPost(json(dto))
                    .invoke(Response.class);
            LOG.info("Sendt journalpost OK");
            return null;
        }).apply(journalpostMottakDto);
    }

    private static RetryRegistry registry() {
        var registry = RetryRegistry.of(RetryConfig.custom()
                .maxAttempts(2)
                .waitDuration(Duration.ofMillis(1000))
                .retryExceptions(SocketTimeoutException.class)
                .failAfterMaxAttempts(true)
                .build());
        registry.getEventPublisher().onEntryAdded(a -> {
            a.getAddedEntry().getEventPublisher()
                    .onEvent(e -> LOG.info(e.toString()));
        });
        return registry;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [endpoint=" + endpoint + ", registry=" + registry + "]";
    }

}
