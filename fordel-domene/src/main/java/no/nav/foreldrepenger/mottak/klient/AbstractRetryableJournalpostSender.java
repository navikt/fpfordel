package no.nav.foreldrepenger.mottak.klient;

import java.net.SocketTimeoutException;
import java.net.URI;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyOidcRestClient;

class AbstractRetryableJournalpostSender extends AbstractJerseyOidcRestClient {
    protected static final Logger LOG = LoggerFactory.getLogger(AbstractRetryableJournalpostSender.class);

    protected final RetryRegistry registry;
    protected final URI endpoint;

    public AbstractRetryableJournalpostSender(URI endpoint) {
        this.endpoint = endpoint;
        this.registry = registry();
    }

    private static RetryRegistry registry() {
        var registry = RetryRegistry.of(RetryConfig.custom()
                .maxAttempts(2)
                .waitDuration(Duration.ofMillis(1000))
                .retryExceptions(SocketTimeoutException.class)
                .failAfterMaxAttempts(true)
                .build());
        registry.getEventPublisher().onEvent(e -> LOG.info(e.toString()));
        return registry;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [endpoint=" + endpoint + ", registry=" + registry + "]";
    }

}
