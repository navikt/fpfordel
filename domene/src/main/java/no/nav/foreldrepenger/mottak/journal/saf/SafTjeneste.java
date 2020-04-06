package no.nav.foreldrepenger.mottak.journal.saf;


import java.net.URI;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import no.nav.foreldrepenger.fordel.ReadFileFromClassPathHelper;
import no.nav.foreldrepenger.mottak.journal.saf.graphql.GraphQlError;
import no.nav.foreldrepenger.mottak.journal.saf.graphql.GraphQlRequest;
import no.nav.foreldrepenger.mottak.journal.saf.graphql.GraphQlResponse;
import no.nav.foreldrepenger.mottak.journal.saf.graphql.Variables;
import no.nav.foreldrepenger.mottak.journal.saf.model.Journalpost;
import no.nav.foreldrepenger.mottak.journal.saf.model.VariantFormat;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClientResponseHandler;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
public class SafTjeneste {

    private static final String DEFAULT_URI = "http://saf.default";

    private URI graphqlEndpoint;
    private URI hentDokumentEndpoint;
    private OidcRestClient restKlient;
    private String query;

    SafTjeneste() {
        // CDI
    }

    @Inject
    public SafTjeneste(@KonfigVerdi(value = "SAF_BASE_URL", defaultVerdi = DEFAULT_URI) URI endpoint, OidcRestClient restKlient) {
        this.graphqlEndpoint = URI.create(endpoint.toString() + "/graphql");
        this.hentDokumentEndpoint = URI.create(endpoint.toString() + "/rest/hentdokument");
        this.restKlient = restKlient;
        this.query = ReadFileFromClassPathHelper.hent("saf/journalpostQuery.graphql");
    }

    public Journalpost hentJournalpostInfo(String journalpostId) {
        try {
            GraphQlRequest graphQlRequest = new GraphQlRequest(query, new Variables(journalpostId));
            GraphQlResponse graphQlResponse = restKlient.post(graphqlEndpoint, graphQlRequest, GraphQlResponse.class);
            if (graphQlResponse.getData() == null || graphQlResponse.getData().getJournalpost() == null) {
                List<String> errorMessageList = graphQlResponse.getErrors().stream().map(GraphQlError::getMessage).collect(Collectors.toList());
                StringJoiner stringJoiner = new StringJoiner(System.lineSeparator());
                stringJoiner.add("Journalpost for journalpostId " + journalpostId + " er null.");
                errorMessageList.forEach(stringJoiner::add);
                String errors = errorMessageList.toString();
                throw new JounalpostIsNullException(errors);
            }
            return graphQlResponse.getData().getJournalpost();
        } catch (Exception e) {
            throw new SafException("Kunne ikke hente journalpost for journalpostId " + journalpostId, e);
        }
    }


    private <T> T utførHentDokument(HttpGet request, OidcRestClientResponseHandler<T> responseHandler) {
        try (var httpResponse = restKlient.execute(request)) {
            int responseCode = httpResponse.getStatusLine().getStatusCode();
            if (responseCode == HttpStatus.SC_OK) {
                return responseHandler.handleResponse(httpResponse);
            } else {
                if (responseCode == HttpStatus.SC_NO_CONTENT) {
                    return null;
                }
                if (responseCode == HttpStatus.SC_ACCEPTED) {
                    return null;
                }
                String responseBody = EntityUtils.toString(httpResponse.getEntity());
                String feilmelding = "Kunne ikke hente informasjon om journalpost: " + request.getURI()
                        + ", HTTP status=" + httpResponse.getStatusLine() + ". HTTP Errormessage=" + responseBody;
                throw new SafException(feilmelding);
            }
        } catch (Exception e) {
            throw new SafException("Kunne ikke hente journalpost", e);
        }
    }

    public String hentDokument(String journalpostId, String dokumentInfoId, VariantFormat variantFormat) {
        var uri = URI.create(hentDokumentEndpoint.toString() + String.format("/%s/%s/%s", journalpostId, dokumentInfoId, variantFormat.name()));
        var getRequest = new HttpGet(uri);

        return utførHentDokument(getRequest, new OidcRestClientResponseHandler.StringResponseHandler(uri));

    }
}
