package no.nav.foreldrepenger.mottak.klient;

import java.net.URI;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.kontrakter.fordel.JournalpostMottakDto;
import no.nav.vedtak.felles.integrasjon.rest.FpApplication;
import no.nav.vedtak.felles.integrasjon.rest.NativeClient;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;

@ApplicationScoped
@NativeClient("tilbake")
@RestClientConfig(tokenConfig = TokenFlow.STS_CC, application = FpApplication.FPTILBAKE)
public class NativeTilbakekrevingKlient implements JournalpostSender {

    protected static final Logger LOG = LoggerFactory.getLogger(NativeTilbakekrevingKlient.class);

    private static final String MOTTAK_JOURNALPOST_PATH = "/api/fordel/journalpost";

    private RestClient klient;
    private URI endpoint;

    @Inject
    public NativeTilbakekrevingKlient(RestClient klient) {
        this.klient = klient;
        this.endpoint = URI.create(RestConfig.endpointFromAnnotation(NativeTilbakekrevingKlient.class).toString() + MOTTAK_JOURNALPOST_PATH);
    }

    @Override
    public void send(JournalpostMottakDto journalpost) {
        LOG.info("Sender journalpost for {}", getClass().getSimpleName());
        klient.sendReturnOptional(RestRequest.newPOSTJson(journalpost, endpoint, NativeDokumentmottakKlient.class), String.class);
        LOG.info("Sendt journalpost OK for {}", getClass().getSimpleName());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [endpoint=" + endpoint + "]";
    }
}
