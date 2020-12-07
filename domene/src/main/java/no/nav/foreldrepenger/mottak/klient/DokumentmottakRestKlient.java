package no.nav.foreldrepenger.mottak.klient;

import java.net.URI;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.kontrakter.fordel.JournalpostMottakDto;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
public class DokumentmottakRestKlient {
    private static final String DEFAULT_FPSAK_BASE_URI = "http://fpsak";
    private static final String DEFAULT_MOTTAK_PATH = "/fpsak/api/fordel/journalpost";

    private OidcRestClient oidcRestClient;
    private URI endpoint;

    public DokumentmottakRestKlient() {
    }

    @Inject
    public DokumentmottakRestKlient(OidcRestClient oidcRestClient,
                                    @KonfigVerdi(value = "fpsak.base.url", defaultVerdi = DEFAULT_FPSAK_BASE_URI) URI endpoint) {
        this.oidcRestClient = oidcRestClient;
        this.endpoint = URI.create(endpoint.toString() + DEFAULT_MOTTAK_PATH);
    }

    public void send(JournalpostMottakDto journalpostMottakDto) {
        oidcRestClient.post(endpoint, journalpostMottakDto);
    }

}
