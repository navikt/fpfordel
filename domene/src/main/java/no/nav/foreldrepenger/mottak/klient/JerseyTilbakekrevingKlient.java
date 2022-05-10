package no.nav.foreldrepenger.mottak.klient;

import java.net.URI;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import no.nav.foreldrepenger.konfig.KonfigVerdi;
import no.nav.vedtak.felles.integrasjon.rest.jersey.Jersey;

@Dependent
@Jersey("tilbake")
public class JerseyTilbakekrevingKlient extends AbstractRetryableJournalpostSender {
    private static final String DEFAULT_TILBAKE_BASE_URI = "http://fptilbake";
    private static final String JOURNALPOST_PATH = "/fptilbake/api/fordel/journalpost";

    @Inject
    public JerseyTilbakekrevingKlient(@KonfigVerdi(value = "fptilbake.base.url", defaultVerdi = DEFAULT_TILBAKE_BASE_URI) URI endpoint) {
        super(endpoint);
    }

    @Override
    protected String path() {
        return JOURNALPOST_PATH;
    }
}
