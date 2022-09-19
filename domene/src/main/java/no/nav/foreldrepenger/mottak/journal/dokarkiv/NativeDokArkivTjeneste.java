package no.nav.foreldrepenger.mottak.journal.dokarkiv;

import java.net.URI;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.mottak.journal.dokarkiv.model.FerdigstillJournalpostRequest;
import no.nav.foreldrepenger.mottak.journal.dokarkiv.model.OppdaterJournalpostRequest;
import no.nav.foreldrepenger.mottak.journal.dokarkiv.model.OpprettJournalpostRequest;
import no.nav.foreldrepenger.mottak.journal.dokarkiv.model.OpprettJournalpostResponse;
import no.nav.vedtak.felles.integrasjon.rest.NativeClient;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;

@NativeClient
@RestClientConfig(tokenConfig = TokenFlow.STS_CC, endpointProperty = "dokarkiv.base.url", endpointDefault = "http://dokarkiv.default/rest/journalpostapi/v1/journalpost")
@ApplicationScoped
class NativeDokArkivTjeneste implements DokArkiv {

    private static final Logger LOG = LoggerFactory.getLogger(NativeDokArkivTjeneste.class);

    private final URI dokarkiv;
    private final String uriString;
    private final RestClient restKlient;

    @Inject
    public NativeDokArkivTjeneste(RestClient restKlient) {
        this.dokarkiv = RestConfig.endpointFromAnnotation(NativeDokArkivTjeneste.class);
        this.uriString = dokarkiv.toString();
        this.restKlient = restKlient;
    }

    @Override
    public OpprettJournalpostResponse opprettJournalpost(OpprettJournalpostRequest request, boolean ferdigstill) {
        try {
            LOG.info("Oppretter journalpost");
            var opprett = ferdigstill ? UriBuilder.fromUri(dokarkiv).queryParam("forsoekFerdigstill", "true").build() : dokarkiv;
            var rrequest = RestRequest.newPOSTJson(request, opprett, NativeDokArkivTjeneste.class);
            var res = restKlient.sendExpectConflict(rrequest, OpprettJournalpostResponse.class);
            LOG.info("Opprettet journalpost OK");
            return res;
        } catch (Exception e) {
            LOG.info("FPFORDEL DOKARKIV OPPRETT feilet for {}", request, e);
            return null;
        }
    }

    @Override
    public boolean oppdaterJournalpost(String journalpostId, OppdaterJournalpostRequest request) {
        try {
            LOG.info("Oppdaterer journalpost");
            var oppdater = URI.create(uriString + String.format("/%s", journalpostId));
            var method = new RestRequest.Method(RestRequest.WebMethod.PUT, RestRequest.jsonPublisher(request));
            var rrequest = RestRequest.newRequest(method, oppdater, NativeDokArkivTjeneste.class);
            restKlient.send(rrequest, String.class);
            LOG.info("Oppdatert journalpost OK");
            return true;
        } catch (Exception e) {
            LOG.info("FPFORDEL DOKARKIV OPPDATER {} feilet for {}", journalpostId, request, e);
            return false;
        }
    }

    @Override
    public boolean ferdigstillJournalpost(String journalpostId, String enhet) {
        try {
            LOG.info("Ferdigstiller journalpost");
            var ferdigstill = URI.create(uriString + String.format("/%s/ferdigstill", journalpostId));
            var method = new RestRequest.Method(RestRequest.WebMethod.PATCH, RestRequest.jsonPublisher(new FerdigstillJournalpostRequest(enhet)));
            var rrequest = RestRequest.newRequest(method, ferdigstill, NativeDokArkivTjeneste.class);
            restKlient.send(rrequest, String.class);
            LOG.info("Ferdigstilt journalpost OK");
            return true;
        } catch (Exception e) {
            LOG.info("FPFORDEL DOKARKIV FERDIGSTILL {} feilet for {}", journalpostId, enhet, e);
            return false;
        }
    }
}
