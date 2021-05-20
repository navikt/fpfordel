package no.nav.foreldrepenger.mottak.klient;

import java.net.URI;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.vedtak.felles.integrasjon.rest.jersey.Jersey;

@Dependent
@Jersey("dokument")
public class JerseyDokumentmottakKlient extends AbstractRetryableJournalpostSender {
    private static final String DEFAULT_FPSAK_BASE_URI = "http://fpsak";
    private static final String FPSAK_MOTTAK_JOURNALPOST_PATH = "/fpsak/api/fordel/journalpost";

    @Inject
    public JerseyDokumentmottakKlient(@KonfigVerdi(value = "fpsak.base.url", defaultVerdi = DEFAULT_FPSAK_BASE_URI) URI endpoint) {
        super(endpoint);
    }

    @Override
    protected String path() {
        return FPSAK_MOTTAK_JOURNALPOST_PATH;
    }
}
