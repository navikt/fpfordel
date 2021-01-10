package no.nav.foreldrepenger.mottak.journal.dokarkiv;

import java.net.URI;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.mottak.journal.dokarkiv.model.FerdigstillJournalpostRequest;
import no.nav.foreldrepenger.mottak.journal.dokarkiv.model.OppdaterJournalpostRequest;
import no.nav.foreldrepenger.mottak.journal.dokarkiv.model.OpprettJournalpostRequest;
import no.nav.foreldrepenger.mottak.journal.dokarkiv.model.OpprettJournalpostResponse;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClientFeil;
import no.nav.vedtak.felles.integrasjon.rest.jersey.OidcTokenRequestFilter;
import no.nav.vedtak.isso.SystemUserIdTokenProvider;
import no.nav.vedtak.konfig.KonfigVerdi;
import no.nav.vedtak.sikkerhet.context.SubjectHandler;
import no.nav.vedtak.sikkerhet.domene.SAMLAssertionCredential;

@ApplicationScoped
public class LegacyDokArkivTjeneste implements DokArkiv {

    private static final String DEFAULT_URI = "http://dokarkiv.default/rest/journalpostapi/v1/journalpost";

    private static final Logger LOG = LoggerFactory.getLogger(LegacyDokArkivTjeneste.class);

    private URI dokarkiv;
    private String uriString;
    private OidcRestClient restKlient;

    LegacyDokArkivTjeneste() {
        // CDI
    }

    @Inject
    public LegacyDokArkivTjeneste(@KonfigVerdi(value = "dokarkiv.base.url", defaultVerdi = DEFAULT_URI) URI endpoint, OidcRestClient restKlient) {
        this.dokarkiv = endpoint;
        this.uriString = endpoint.toString();
        this.restKlient = restKlient;
    }

    @Override
    public OpprettJournalpostResponse opprettJournalpost(OpprettJournalpostRequest request, boolean ferdigstill) {
        try {
            LOG.info("Oppretter journalpost");
            test();
            var opprett = ferdigstill ? new URIBuilder(dokarkiv).addParameter("forsoekFerdigstill", "true").build() : dokarkiv;
            return restKlient.post(opprett, request, OpprettJournalpostResponse.class);
        } catch (Exception e) {
            LOG.info("FPFORDEL DOKARKIV OPPRETT feilet for {}", request, e);
            return null;
        }
    }

    private static void test() {
        try {
            String token = new OidcTokenRequestFilter().accessToken();
            LOG.info("TEST " + token);
        } catch (Exception e) {
            LOG.info("TEST", e);
            LOG.info("TEST1 ", test1());

        }
    }

    private static String test1() {
        try {
            String oidcToken = SubjectHandler.getSubjectHandler().getInternSsoToken();
            if (oidcToken != null) {
                LOG.trace("TEST Internal token OK");
                return oidcToken;
            }
            var samlToken = SubjectHandler.getSubjectHandler().getSamlToken();
            if (samlToken != null) {
                LOG.trace("TEST SAML token OK");
                var t = veksleSamlTokenTilOIDCToken(samlToken);
                LOG.info("TEST " + t);
                return t;
            }
            throw OidcRestClientFeil.FACTORY.klarteIkkeSkaffeOIDCToken().toException();
        } catch (Exception e) {
            LOG.info("TEST", e);
            return null;
        }
    }

    private static String veksleSamlTokenTilOIDCToken(@SuppressWarnings("unused") SAMLAssertionCredential samlToken) {
        var t = SystemUserIdTokenProvider.getSystemUserIdToken().getToken();
        LOG.trace("SAML token null :" + (t != null));
        return t;
    }

    @Override
    public boolean oppdaterJournalpost(String journalpostId, OppdaterJournalpostRequest request) {
        try {
            LOG.info("Oppdaterer journalpost");
            var oppdater = URI.create(uriString + String.format("/%s", journalpostId));
            restKlient.put(oppdater, request);
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
            restKlient.patch(ferdigstill, new FerdigstillJournalpostRequest(enhet));
            return true;
        } catch (Exception e) {
            LOG.info("FPFORDEL DOKARKIV FERDIGSTILL {} feilet for {}", journalpostId, enhet, e);
            return false;
        }
    }

}
