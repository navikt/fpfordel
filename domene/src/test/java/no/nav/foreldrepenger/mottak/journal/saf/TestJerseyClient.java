package no.nav.foreldrepenger.mottak.journal.saf;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.foreldrepenger.mottak.journal.saf.JerseySafTjeneste.GRAPHQL_PATH;
import static no.nav.foreldrepenger.mottak.journal.saf.model.VariantFormat.ARKIV;
import static no.nav.vedtak.felles.integrasjon.rest.DefaultJsonMapper.mapper;
import static no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyRestClient.DEFAULT_NAV_CALLID;
import static no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyRestClient.OIDC_AUTH_HEADER_PREFIX;
import static no.nav.vedtak.log.mdc.MDCOperations.generateCallId;
import static no.nav.vedtak.log.mdc.MDCOperations.putCallId;
import static org.apache.http.HttpHeaders.ACCEPT;
import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.HttpStatus.SC_OK;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;

import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;

import no.nav.foreldrepenger.mottak.journal.saf.graphql.GraphQlError;
import no.nav.foreldrepenger.mottak.journal.saf.graphql.GraphQlResponse;
import no.nav.vedtak.felles.integrasjon.rest.jersey.OidcTokenRequestFilter;

@ExtendWith(MockitoExtension.class)
public class TestJerseyClient {

    private static final String TOKEN = "TOKEN";
    private static final String CALLID = generateCallId();

    private SafTjeneste client;
    @Spy
    private OidcTokenRequestFilter filter;
    private static WireMockServer wireMockServer;

    @BeforeAll
    public static void startServer() {
        putCallId(CALLID);
        wireMockServer = new WireMockServer(8080);
        wireMockServer.start();
    }

    @AfterAll
    public static void stopServer() {
        wireMockServer.stop();
    }

    @BeforeEach
    public void beforeEach() {
        doReturn(TOKEN).when(filter).accessToken();
        client = new JerseySafTjeneste("http://localhost:8080", filter);
    }

    @Test
    public void testHentDokument() {
        stubFor(headers(get(urlPathEqualTo("/rest/hentdokument/1/2/ARKIV")))
                .willReturn(aResponse()
                        .withStatus(SC_OK)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(body("sak"))));
        client.hentDokument("1", "2", ARKIV);
    }

    @Test
    public void testHentJournalpostFeiler() throws Exception {
        stubFor(headers(post(urlPathEqualTo(GRAPHQL_PATH)))
                .willReturn(aResponse()
                        .withStatus(SC_OK)
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                        .withBody(body(feil()))));
        assertThrows(SafException.class, () -> client.hentJournalpostInfo("1"));
    }

    private static MappingBuilder headers(MappingBuilder p) {
        return p.withHeader(ACCEPT, equalTo(APPLICATION_JSON))
                .withHeader(AUTHORIZATION, containing(OIDC_AUTH_HEADER_PREFIX))
                .withHeader(AUTHORIZATION, containing(TOKEN))
                .withHeader(DEFAULT_NAV_CALLID, equalTo(CALLID));
    }

    private static String body(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static GraphQlResponse feil() {
        return new GraphQlResponse(null, List.of(new GraphQlError("Oops", null, "1", "1", null)));
    }
}
