package no.nav.foreldrepenger.mottak.klient;

import java.net.URI;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.kontrakter.fordel.JournalpostMottakDto;
import no.nav.vedtak.felles.integrasjon.rest.FpApplication;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;

@ApplicationScoped
@RestClientConfig(tokenConfig = TokenFlow.ADAPTIVE, application = FpApplication.FPSAK)
public class DokumentmottakKlient {

    protected static final Logger LOG = LoggerFactory.getLogger(DokumentmottakKlient.class);

    private static final String MOTTAK_JOURNALPOST_PATH = "/api/fordel/journalpost";

    private final RestClient klient;
    private final RestConfig restConfig;
    private final URI endpoint;

    public DokumentmottakKlient() {
        this.klient = RestClient.client();
        this.restConfig = RestConfig.forClient(this.getClass());
        this.endpoint = UriBuilder.fromUri(restConfig.fpContextPath()).path(MOTTAK_JOURNALPOST_PATH).build();
    }


    public void send(JournalpostMottakDto journalpost) {
        LOG.info("Sender journalpost for {}", getClass().getSimpleName());
        klient.sendReturnOptional(RestRequest.newPOSTJson(journalpost, endpoint, restConfig), String.class);
        LOG.info("Sendt journalpost OK for {}", getClass().getSimpleName());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [endpoint=" + endpoint + "]";
    }
}
