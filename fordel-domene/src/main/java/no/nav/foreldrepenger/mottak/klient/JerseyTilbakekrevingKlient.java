package no.nav.foreldrepenger.mottak.klient;

import static io.github.resilience4j.retry.Retry.decorateFunction;
import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static no.nav.foreldrepenger.mottak.klient.AbstractRetryableJournalpostSender.LOG;

import java.net.URI;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.foreldrepenger.kontrakter.fordel.JournalpostMottakDto;
import no.nav.vedtak.felles.integrasjon.rest.jersey.Jersey;

@Dependent
@Jersey("tilbake")
public class JerseyTilbakekrevingKlient extends AbstractRetryableJournalpostSender implements JournalpostSender {
    private static final String DEFAULT_TILBAKE_BASE_URI = "http://fptilbake";
    private static final String JOURNALPOST_PATH = "/fptilbake/api/fordel/journalpost";

    @Inject
    public JerseyTilbakekrevingKlient(@KonfigVerdi(value = "fptilbake.base.url", defaultVerdi = DEFAULT_TILBAKE_BASE_URI) URI endpoint) {
        super(endpoint);
    }

    @Override
    public void send(JournalpostMottakDto journalpostDto) {
        decorateFunction(registry.retry("journalpostTilbakekreving"), (JournalpostMottakDto dto) -> {
            LOG.info("Sender journalpost");
            client.target(endpoint)
                    .path(JOURNALPOST_PATH)
                    .request(APPLICATION_JSON_TYPE)
                    .buildPost(json(dto))
                    .invoke(Response.class);
            LOG.info("Sendt journalpost OK");
            return null;
        }).apply(journalpostDto);
    }

}
