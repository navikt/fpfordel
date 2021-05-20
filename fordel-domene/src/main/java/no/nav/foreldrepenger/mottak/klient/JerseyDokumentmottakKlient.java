package no.nav.foreldrepenger.mottak.klient;

import static io.github.resilience4j.retry.Retry.decorateFunction;
import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

import java.net.URI;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.foreldrepenger.kontrakter.fordel.JournalpostMottakDto;
import no.nav.vedtak.felles.integrasjon.rest.jersey.Jersey;

@Dependent
@Jersey("dokument")
public class JerseyDokumentmottakKlient extends AbstractRetryableJournalpostSender implements JournalpostSender {
    private static final String DEFAULT_FPSAK_BASE_URI = "http://fpsak";
    private static final String FPSAK_MOTTAK_JOURNALPOST_PATH = "/fpsak/api/fordel/journalpost";

    @Inject
    public JerseyDokumentmottakKlient(@KonfigVerdi(value = "fpsak.base.url", defaultVerdi = DEFAULT_FPSAK_BASE_URI) URI endpoint) {
        super(endpoint);
    }

    @Override
    public void send(JournalpostMottakDto journalpost) {
        decorateFunction(registry.retry("journalpostDokument"), (JournalpostMottakDto dto) -> {
            LOG.info("Sender journalpost");
            client.target(endpoint)
                    .path(FPSAK_MOTTAK_JOURNALPOST_PATH)
                    .request(APPLICATION_JSON_TYPE)
                    .buildPost(json(dto))
                    .invoke(Response.class);
            LOG.info("Sendt journalpost OK");
            return null;
        }).apply(journalpost);
    }
}
