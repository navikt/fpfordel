package no.nav.foreldrepenger.mottak.journal.saf;

import static javax.ws.rs.client.Entity.json;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

import java.net.URI;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import javax.inject.Inject;

import no.nav.foreldrepenger.fordel.ReadFileFromClassPathHelper;
import no.nav.foreldrepenger.mottak.journal.saf.graphql.GraphQlError;
import no.nav.foreldrepenger.mottak.journal.saf.graphql.GraphQlRequest;
import no.nav.foreldrepenger.mottak.journal.saf.graphql.GraphQlResponse;
import no.nav.foreldrepenger.mottak.journal.saf.graphql.GraphQlTilknyttetRequest;
import no.nav.foreldrepenger.mottak.journal.saf.graphql.GraphQlTilknyttetResponse;
import no.nav.foreldrepenger.mottak.journal.saf.graphql.TilknyttetVariables;
import no.nav.foreldrepenger.mottak.journal.saf.graphql.Variables;
import no.nav.foreldrepenger.mottak.journal.saf.model.Journalpost;
import no.nav.foreldrepenger.mottak.journal.saf.model.VariantFormat;
import no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyOidcRestClient;
import no.nav.vedtak.konfig.KonfigVerdi;

// @ApplicationScoped
public class JerseySafTjeneste extends AbstractJerseyOidcRestClient implements SafTjeneste {

    private static final String GRAPHQL_PATH = "graphql";
    private static final String DOKUMENT_PATH = "/rest/hentdokument/{journalpostId}/{dokumentInfoId}/{variantFormat}";
    private static final String DEFAULT_URI = "http://saf.default";

    private URI base;
    private String query;
    private String tilknyttedeQuery;

    JerseySafTjeneste() {
        // CDI
    }

    @Inject
    public JerseySafTjeneste(@KonfigVerdi(value = "saf.base.url", defaultVerdi = DEFAULT_URI) URI base) {
        this.base = base;
        this.query = ReadFileFromClassPathHelper.hent("saf/journalpostQuery.graphql");
        this.tilknyttedeQuery = ReadFileFromClassPathHelper.hent("saf/tilknyttedeJournalposterQuery.graphql");
    }

    @Override
    public Journalpost hentJournalpostInfo(String journalpostId) {
        try {
            var req = new GraphQlRequest(query, new Variables(journalpostId));
            var res = client.target(base)
                    .path(GRAPHQL_PATH)
                    .request(APPLICATION_JSON_TYPE)
                    .buildPost(json(req))
                    .invoke(GraphQlResponse.class);
            if ((res.getData() == null) || (res.getData().getJournalpost() == null)) {
                var errors = res.getErrors().stream().map(GraphQlError::getMessage).collect(Collectors.toList());
                var stringJoiner = new StringJoiner(System.lineSeparator());
                stringJoiner.add("Journalpost for journalpostId " + journalpostId + " er null.");
                errors.forEach(stringJoiner::add);
                throw new JounalpostIsNullException(errors.toString());
            }
            return res.getData().getJournalpost();
        } catch (Exception e) {
            throw new SafException("Kunne ikke hente journalpost for journalpostId " + journalpostId, e);
        }
    }

    @Override
    public String hentDokument(String journalpostId, String dokumentInfoId, VariantFormat variantFormat) {
        try {
            return client.target(base)
                    .path(DOKUMENT_PATH)
                    .resolveTemplate("journalpostId", journalpostId)
                    .resolveTemplate("dokumentInfoId", dokumentInfoId)
                    .resolveTemplate("variantFormat", variantFormat.name())
                    .request(APPLICATION_JSON_TYPE)
                    .get(String.class);
        } catch (Exception e) {
            throw new SafException("Kunne ikke hente dokument " + dokumentInfoId + " for journalpost " + journalpostId, e);
        }
    }

    @Override
    public List<Journalpost> hentEksternReferanseId(String dokumentInfoId) {
        try {
            var req = new GraphQlTilknyttetRequest(tilknyttedeQuery, new TilknyttetVariables(dokumentInfoId));
            var res = client.target(base)
                    .path(GRAPHQL_PATH)
                    .request(APPLICATION_JSON_TYPE)
                    .buildPost(json(req))
                    .invoke(GraphQlTilknyttetResponse.class);
            if ((res.getData() == null) || (res.getData().getJournalposter() == null)) {
                var errors = res.getErrors().stream().map(GraphQlError::getMessage).collect(Collectors.toList());
                var stringJoiner = new StringJoiner(System.lineSeparator());
                stringJoiner.add("Journalposter for dokumentInfoId " + dokumentInfoId + " er null.");
                errors.forEach(stringJoiner::add);
                throw new JounalpostIsNullException(errors.toString());
            }
            return res.getData().getJournalposter();
        } catch (Exception e) {
            throw new SafException("Kunne ikke hente tilknyttede journalposter for dokumentInfoId " + dokumentInfoId, e);
        }
    }
}
