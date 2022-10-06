package no.nav.foreldrepenger.mottak.journal.dokarkiv;

import java.net.URI;

import javax.enterprise.context.Dependent;
import javax.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.felles.integrasjon.dokarkiv.DokArkiv;
import no.nav.vedtak.felles.integrasjon.dokarkiv.dto.FerdigstillJournalpostRequest;
import no.nav.vedtak.felles.integrasjon.dokarkiv.dto.OppdaterJournalpostRequest;
import no.nav.vedtak.felles.integrasjon.dokarkiv.dto.OpprettJournalpostRequest;
import no.nav.vedtak.felles.integrasjon.dokarkiv.dto.OpprettJournalpostResponse;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;

@Dependent
@RestClientConfig(tokenConfig = TokenFlow.STS_CC, endpointProperty = "dokarkiv.base.url", endpointDefault = "http://dokarkiv.default/rest/journalpostapi/v1/journalpost")
public class DokArkivKlient implements DokArkiv {

    private static final Logger LOG = LoggerFactory.getLogger(DokArkivKlient.class);

    private final RestClient restKlient;
    private final RestConfig restConfig;

    protected DokArkivKlient() {
        this.restKlient = RestClient.client();
        this.restConfig = RestConfig.forClient(this.getClass());
    }


    @Override
    public OpprettJournalpostResponse opprettJournalpost(OpprettJournalpostRequest request, boolean ferdigstill) {
        try {
            LOG.info("DOKARKIV Oppretter journalpost");
            var opprett = ferdigstill ?
                UriBuilder.fromUri(restConfig.endpoint()).queryParam("forsoekFerdigstill", "true").build() :
                restConfig.endpoint();
            var restRequest = RestRequest.newPOSTJson(request, opprett, restConfig);
            var res = restKlient.sendExpectConflict(restRequest, OpprettJournalpostResponse.class);
            LOG.info("DOKARKIV Opprettet journalpost OK");
            return res;
        } catch (Exception e) {
            LOG.info("DOKARKIV OPPRETT feilet for {}", request, e);
            return null;
        }
    }

    @Override
    public boolean oppdaterJournalpost(String journalpostId, OppdaterJournalpostRequest request) {
        try {
            LOG.info("DOKARKIV Oppdaterer journalpost");
            var oppdater = URI.create(restConfig.endpoint().toString() + String.format("/%s", journalpostId));
            var method = new RestRequest.Method(RestRequest.WebMethod.PUT, RestRequest.jsonPublisher(request));
            var restRequest = RestRequest.newRequest(method, oppdater, restConfig);
            restKlient.send(restRequest, String.class);
            LOG.info("DOKARKIV Oppdatert journalpost OK");
            return true;
        } catch (Exception e) {
            LOG.info("DOKARKIV OPPDATER {} feilet for {}", journalpostId, request, e);
            return false;
        }
    }

    @Override
    public boolean ferdigstillJournalpost(String journalpostId, String enhet) {
        try {
            LOG.info("DOKARKIV Ferdigstiller journalpost");
            var ferdigstill = URI.create(restConfig.endpoint().toString() + String.format("/%s/ferdigstill", journalpostId));
            var method = new RestRequest.Method(RestRequest.WebMethod.PATCH, RestRequest.jsonPublisher(new FerdigstillJournalpostRequest(enhet)));
            var request = RestRequest.newRequest(method, ferdigstill, restConfig);
            restKlient.send(request, String.class);
            LOG.info("DOKARKIV Ferdigstilt journalpost OK");
            return true;
        } catch (Exception e) {
            LOG.info("DOKARKIV FERDIGSTILL {} feilet for {}", journalpostId, enhet, e);
            return false;
        }
    }
}
