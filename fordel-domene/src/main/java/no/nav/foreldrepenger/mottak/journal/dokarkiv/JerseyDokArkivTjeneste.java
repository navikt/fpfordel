package no.nav.foreldrepenger.mottak.journal.dokarkiv;

import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.UriBuilder.fromUri;

import java.net.URI;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.mottak.journal.dokarkiv.model.FerdigstillJournalpostRequest;
import no.nav.foreldrepenger.mottak.journal.dokarkiv.model.OppdaterJournalpostRequest;
import no.nav.foreldrepenger.mottak.journal.dokarkiv.model.OpprettJournalpostRequest;
import no.nav.foreldrepenger.mottak.journal.dokarkiv.model.OpprettJournalpostResponse;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyOidcRestClient;
import no.nav.vedtak.felles.integrasjon.rest.jersey.Jersey;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
@Jersey
public class JerseyDokArkivTjeneste extends AbstractJerseyOidcRestClient implements DokArkiv {

    static final String FERDIGSTILL = "forsoekFerdigstill";
    private static final String DEFAULT_URI = "http://dokarkiv.default/rest/journalpostapi/v1/journalpost";
    private static final String OPPDATER_PATH = "/{journalpostId}";
    private static final String FERDIGSTILL_PATH = OPPDATER_PATH + "/ferdigstill";
    private static final Logger LOG = LoggerFactory.getLogger(JerseyDokArkivTjeneste.class);

    private URI base;

    JerseyDokArkivTjeneste() {
    }

    @Inject
    public JerseyDokArkivTjeneste(@KonfigVerdi(value = "dokarkiv.base.url", defaultVerdi = DEFAULT_URI) URI base) {
        this.base = base;
    }

    @Override
    public OpprettJournalpostResponse opprettJournalpost(OpprettJournalpostRequest req, boolean ferdigstill) {
        try {
            var target = client.target(base).queryParam(FERDIGSTILL, ferdigstill);
            LOG.info("Oppretter journalpost {} på {}", ferdigstill, target.getUri());
            var res = target
                    .request(APPLICATION_JSON_TYPE)
                    .buildPost(json(req))
                    .invoke(OpprettJournalpostResponse.class);
            LOG.info("Opprettet journalpost {} OK ({})", res.journalpostId(), target.getUri());
            return res;
        } catch (Exception e) {
            LOG.warn("Opprett journalpost feilet", e);
            throw new TekniskException("F-999999", base.toString(), e);
        }
    }

    @Override
    public boolean oppdaterJournalpost(String journalpostId, OppdaterJournalpostRequest req) {
        try {
            var target = client.target(base)
                    .path(OPPDATER_PATH)
                    .resolveTemplate("journalpostId", journalpostId);
            LOG.info("Oppdaterer journalpos på {}", target.getUri());
            target
                    .request(APPLICATION_JSON_TYPE)
                    .buildPut(json(req))
                    .invoke(Response.class);
            LOG.info("Oppdatert journalpost OK ({})", target.getUri());
            return true;
        } catch (Exception e) {
            LOG.warn("Oppdatering journalpost {} feilet", journalpostId, e);
            return false;
        }
    }

    @Override
    public boolean ferdigstillJournalpost(String journalpostId, String enhet) {
        try {
            URI uri = fromUri(base).path(FERDIGSTILL_PATH).build(journalpostId);
            LOG.info("Ferdigstiller journalpost {} enhet {} på {}", journalpostId, enhet, uri);
            patch(uri, new FerdigstillJournalpostRequest(enhet));
            LOG.info("Ferdigstillt journalpost OK ({})", uri);
            return true;
        } catch (Exception e) {
            LOG.warn("Ferdigstilling journalpost {} feilet for {}", journalpostId, enhet, e);
            return false;
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [base=" + base + "]";
    }
}
