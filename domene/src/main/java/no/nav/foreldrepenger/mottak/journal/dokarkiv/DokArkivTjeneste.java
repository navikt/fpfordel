package no.nav.foreldrepenger.mottak.journal.dokarkiv;


import java.net.URI;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
public class DokArkivTjeneste {

    private static final String DEFAULT_URI = "http://dokarkiv.default/rest/journalpostapi/v1/journalpost";

    private static final Logger LOG = LoggerFactory.getLogger(DokArkivTjeneste.class);

    private URI dokarkiv;
    private OidcRestClient restKlient;

    DokArkivTjeneste() {
        // CDI
    }

    @Inject
    public DokArkivTjeneste(@KonfigVerdi(value = "DOKARKIV_BASE_URL", defaultVerdi = DEFAULT_URI) URI endpoint, OidcRestClient restKlient) {
        this.dokarkiv = endpoint;
        this.restKlient = restKlient;
    }

    public OpprettJournalpostResponse opprettJournalpost(OpprettJournalpostRequest request) {
        try {
            return restKlient.post(dokarkiv, request, OpprettJournalpostResponse.class);
        } catch (Exception e) {
            LOG.info("FPFORDEL DOKARKIV OPPRETT feilet for {}", request, e);
            return null;
        }
    }

    public void oppdaterJournalpost(String journalpostId, OppdaterJournalpostRequest request) {
        try {
            var oppdater = URI.create(dokarkiv.toString() + String.format("/%s", journalpostId));
            restKlient.put(oppdater, request);
        } catch (Exception e) {
            LOG.info("FPFORDEL DOKARKIV OPPDATER {} feilet for {}", journalpostId, request, e);
        }
    }

    public void ferdigstillJournalpost(String journalpostId, String enhet) {
        try {
            var ferdigstill = URI.create(dokarkiv.toString() + String.format("/%s/ferdigstill", journalpostId));
            restKlient.patch(ferdigstill, new FerdigstillJournalpostRequest(enhet));
        } catch (Exception e) {
            LOG.info("FPFORDEL DOKARKIV FERDIGSTILL {} feilet for {}", journalpostId, enhet, e);
        }
    }

}
