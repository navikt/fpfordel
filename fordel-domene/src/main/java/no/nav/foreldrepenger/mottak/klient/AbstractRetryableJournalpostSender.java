package no.nav.foreldrepenger.mottak.klient;

import static io.github.resilience4j.retry.Retry.decorateFunction;
import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

import java.net.SocketTimeoutException;
import java.net.URI;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import no.nav.foreldrepenger.kontrakter.fordel.JournalpostMottakDto;
import no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyOidcRestClient;

abstract class AbstractRetryableJournalpostSender extends AbstractJerseyOidcRestClient implements JournalpostSender {
    protected static final Logger LOG = LoggerFactory.getLogger(AbstractRetryableJournalpostSender.class);

    protected final RetryRegistry registry;
    protected final URI endpoint;

    abstract protected String path();

    public AbstractRetryableJournalpostSender(URI endpoint) {
        this.endpoint = endpoint;
        this.registry = registry();
    }

    @Override
    public void send(JournalpostMottakDto journalpost) {
        decorateFunction(registry.retry(getClass().getSimpleName()), (JournalpostMottakDto dto) -> {
            LOG.info("Sender journalpost for {}", getClass().getSimpleName());
            client.target(endpoint)
                    .path(path())
                    .request(APPLICATION_JSON_TYPE)
                    .buildPost(json(dto))
                    .invoke();
            LOG.info("Sendt journalpost OK for {}", getClass().getSimpleName());
            return null;
        }).apply(journalpost);
    }

    private static RetryRegistry registry() {
        var registry = RetryRegistry.of(RetryConfig.custom()
                .maxAttempts(2)
                .waitDuration(Duration.ofMillis(1000))
                .retryExceptions(SocketTimeoutException.class)
                .failAfterMaxAttempts(true)
                .build());
        registry.getEventPublisher()
                .onEntryAdded(entryAddedEvent -> {
                    entryAddedEvent.getAddedEntry().getEventPublisher().onEvent(e -> LOG.info("Added {}", e));
                })
                .onEntryRemoved(entryRemovedEvent -> {
                    entryRemovedEvent.getRemovedEntry().getEventPublisher().onEvent(e -> LOG.info("Removed {}"));
                });
        return registry;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [endpoint=" + endpoint + ", registry=" + registry + "]";
    }

}
