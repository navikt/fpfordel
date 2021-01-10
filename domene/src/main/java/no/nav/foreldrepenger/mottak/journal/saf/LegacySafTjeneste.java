package no.nav.foreldrepenger.mottak.journal.saf;

import java.net.URI;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
public class LegacySafTjeneste implements SafTjeneste {

    private static final String DEFAULT_URI = "http://saf.default";
    private static final Logger LOG = LoggerFactory.getLogger(LegacySafTjeneste.class);

    private URI graphqlEndpoint;
    private URI hentDokumentEndpoint;
    private OidcRestClient restKlient;
    private String query;
    private String tilknyttedeQuery;

    LegacySafTjeneste() {
        // CDI
    }

    @Inject
    public LegacySafTjeneste(@KonfigVerdi(value = "saf.base.url", defaultVerdi = DEFAULT_URI) URI endpoint, OidcRestClient restKlient) {
        this.graphqlEndpoint = URI.create(endpoint.toString() + "/graphql");
        this.hentDokumentEndpoint = URI.create(endpoint.toString() + "/rest/hentdokument");
        this.restKlient = restKlient;
        this.query = ReadFileFromClassPathHelper.hent("saf/journalpostQuery.graphql");
        this.tilknyttedeQuery = ReadFileFromClassPathHelper.hent("saf/tilknyttedeJournalposterQuery.graphql");
    }

    @Override
    public Journalpost hentJournalpostInfo(String journalpostId) {
        try {
            LOG.info("Henter journalpost {}", journalpostId);
            GraphQlRequest graphQlRequest = new GraphQlRequest(query, new Variables(journalpostId));
            GraphQlResponse graphQlResponse = restKlient.post(graphqlEndpoint, graphQlRequest, GraphQlResponse.class);
            if ((graphQlResponse.getData() == null) || (graphQlResponse.getData().getJournalpost() == null)) {
                List<String> errorMessageList = graphQlResponse.getErrors().stream().map(GraphQlError::getMessage).collect(Collectors.toList());
                StringJoiner stringJoiner = new StringJoiner(System.lineSeparator());
                stringJoiner.add("Journalpost for journalpostId " + journalpostId + " er null.");
                errorMessageList.forEach(stringJoiner::add);
                String errors = errorMessageList.toString();
                throw new JounalpostIsNullException(errors);
            }
            LOG.info("Hentet journalpost OK");
            return graphQlResponse.getData().getJournalpost();
        } catch (Exception e) {
            throw new SafException("Kunne ikke hente journalpost for journalpostId " + journalpostId, e);
        }
    }

    @Override
    public String hentDokument(String journalpostId, String dokumentInfoId, VariantFormat variantFormat) {
        try {
            var uri = URI.create(hentDokumentEndpoint.toString() + String.format("/%s/%s/%s", journalpostId, dokumentInfoId, variantFormat.name()));
            return restKlient.get(uri);
        } catch (Exception e) {
            throw new SafException("Kunne ikke hente dokument " + dokumentInfoId + " for journalpost " + journalpostId, e);
        }
    }

    @Override
    public List<Journalpost> hentEksternReferanseId(String dokumentInfoId) {
        try {
            GraphQlTilknyttetRequest graphQlRequest = new GraphQlTilknyttetRequest(tilknyttedeQuery, new TilknyttetVariables(dokumentInfoId));
            GraphQlTilknyttetResponse graphQlResponse = restKlient.post(graphqlEndpoint, graphQlRequest, GraphQlTilknyttetResponse.class);
            if ((graphQlResponse.getData() == null) || (graphQlResponse.getData().getJournalposter() == null)) {
                List<String> errorMessageList = graphQlResponse.getErrors().stream().map(GraphQlError::getMessage).collect(Collectors.toList());
                StringJoiner stringJoiner = new StringJoiner(System.lineSeparator());
                stringJoiner.add("Journalposter for dokumentInfoId " + dokumentInfoId + " er null.");
                errorMessageList.forEach(stringJoiner::add);
                String errors = errorMessageList.toString();
                throw new JounalpostIsNullException(errors);
            }
            return graphQlResponse.getData().getJournalposter();
        } catch (Exception e) {
            throw new SafException("Kunne ikke hente tilknyttede journalposter for dokumentInfoId " + dokumentInfoId, e);
        }
    }
}
