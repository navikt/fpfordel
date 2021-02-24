package no.nav.foreldrepenger.mottak.journal.dokarkiv;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static no.nav.vedtak.felles.integrasjon.rest.DefaultJsonMapper.mapper;
import static no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyRestClient.DEFAULT_NAV_CALLID;
import static no.nav.vedtak.felles.integrasjon.rest.jersey.AbstractJerseyRestClient.OIDC_AUTH_HEADER_PREFIX;
import static no.nav.vedtak.log.mdc.MDCOperations.generateCallId;
import static no.nav.vedtak.log.mdc.MDCOperations.putCallId;
import static org.apache.http.HttpHeaders.ACCEPT;
import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.HttpStatus.SC_OK;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;

import no.nav.foreldrepenger.mottak.journal.dokarkiv.model.AvsenderMottaker;
import no.nav.foreldrepenger.mottak.journal.dokarkiv.model.AvsenderMottakerIdType;
import no.nav.foreldrepenger.mottak.journal.dokarkiv.model.Bruker;
import no.nav.foreldrepenger.mottak.journal.dokarkiv.model.DokumentInfoOppdater;
import no.nav.foreldrepenger.mottak.journal.dokarkiv.model.DokumentInfoOpprett;
import no.nav.foreldrepenger.mottak.journal.dokarkiv.model.DokumentInfoResponse;
import no.nav.foreldrepenger.mottak.journal.dokarkiv.model.Dokumentvariant;
import no.nav.foreldrepenger.mottak.journal.dokarkiv.model.JournalpostType;
import no.nav.foreldrepenger.mottak.journal.dokarkiv.model.OppdaterJournalpostRequest;
import no.nav.foreldrepenger.mottak.journal.dokarkiv.model.OpprettJournalpostRequest;
import no.nav.foreldrepenger.mottak.journal.dokarkiv.model.OpprettJournalpostResponse;
import no.nav.foreldrepenger.mottak.journal.dokarkiv.model.Sak;
import no.nav.foreldrepenger.mottak.journal.dokarkiv.model.Variantformat;
import no.nav.foreldrepenger.mottak.journal.saf.model.BrukerIdType;
import no.nav.vedtak.sikkerhet.context.SubjectHandler;

@ExtendWith(MockitoExtension.class)
@Disabled
class TestDokarkiv {
    private static final String CALLID = generateCallId();

    private static final String TOKEN = "TOKEN";
    private static final DokArkiv CLIENT = new JerseyDokArkivTjeneste(URI.create("http://localhost:8080"));
    private static WireMockServer wireMockServer;

    @Mock
    private SubjectHandler subjectHandler;

    @BeforeAll
    static void startServer() {
        putCallId(CALLID);
        wireMockServer = new WireMockServer(8080);
        wireMockServer.start();
    }

    @AfterAll
    static void stopServer() {
        wireMockServer.stop();
    }

    @BeforeEach
    void beforeEach() {
        doReturn(TOKEN).when(subjectHandler).getInternSsoToken();
    }

    @Test
    void testFerdigstill() throws Exception {
        stubFor(headers(patch(urlPathEqualTo("/42/ferdigstill")))
                .willReturn(emptyResponse()));
        try (var s = mockStatic(SubjectHandler.class)) {
            s.when(SubjectHandler::getSubjectHandler).thenReturn(subjectHandler);
            CLIENT.ferdigstillJournalpost("42", "666");
        }
    }

    @Test
    void testOppdater() throws Exception {
        stubFor(headers(put(urlPathEqualTo("/42")))
                .willReturn(emptyResponse()));
        try (var s = mockStatic(SubjectHandler.class)) {
            s.when(SubjectHandler::getSubjectHandler).thenReturn(subjectHandler);
            CLIENT.oppdaterJournalpost("42", oppdaterReq());
        }
    }

    @Test
    void testOpprett() throws Exception {
        stubFor(headers(post(urlPathMatching("/.*"))
                .withQueryParam("forsoekFerdigstill", equalTo("true")))
                        .willReturn(aResponse()
                                .withStatus(SC_OK)
                                .withHeader(CONTENT_TYPE, APPLICATION_JSON)
                                .withBody(mapper.writeValueAsString(opprettetRes()))));
        try (var s = mockStatic(SubjectHandler.class)) {
            s.when(SubjectHandler::getSubjectHandler).thenReturn(subjectHandler);
            var opprettet = CLIENT.opprettJournalpost(opprettReq(), true);
            System.out.println(opprettet);
        }
    }

    private static OpprettJournalpostResponse opprettetRes() {
        return new OpprettJournalpostResponse("123", true, List.of(infoResponse()));

    }

    private OpprettJournalpostRequest opprettReq() {
        var r = OpprettJournalpostRequest.nyInngående();
        r.setAvsenderMottaker(avmot());
        r.setBehandlingstema("tema");
        r.setBruker(bruker());
        r.setDatoMottatt(LocalDate.now());
        r.setDokumenter(infoOpprett());
        r.setEksternReferanseId("ref");
        r.setJournalfoerendeEnhet("enhet");
        r.setJournalposttype(JournalpostType.INNGAAENDE);
        r.setKanal("kanal");
        r.setSak(sak());
        r.setTema("tema");
        r.setTittel("tittel");
        return r;
    }

    private static DokumentInfoResponse infoResponse() {
        return new DokumentInfoResponse("123");
    }

    private static OppdaterJournalpostRequest oppdaterReq() {
        return new OppdaterJournalpostRequest("tittel", "tema", "behtema", bruker(), avmot(), sak(), infoOppdater());
    }

    private static List<DokumentInfoOppdater> infoOppdater() {
        return List.of(new DokumentInfoOppdater("id", "tittel", "kode"));
    }

    private static List<DokumentInfoOpprett> infoOpprett() {
        return List.of(new DokumentInfoOpprett("id", "tittel", "kode", List.of(new Dokumentvariant(Variantformat.ARKIV, "1", "2"))));
    }

    private static AvsenderMottaker avmot() {
        return new AvsenderMottaker("id", AvsenderMottakerIdType.FNR, "navn");
    }

    private static Sak sak() {
        return new Sak("1", "2", "3", "4", "5");
    }

    private static Bruker bruker() {
        return new Bruker("id", BrukerIdType.FNR);
    }

    private static ResponseDefinitionBuilder emptyResponse() {
        return aResponse()
                .withStatus(SC_OK)
                .withHeader(CONTENT_TYPE, APPLICATION_JSON);
    }

    private static MappingBuilder headers(MappingBuilder p) {
        return p.withHeader(ACCEPT, equalTo(APPLICATION_JSON))
                .withHeader(AUTHORIZATION, containing(OIDC_AUTH_HEADER_PREFIX))
                .withHeader(AUTHORIZATION, containing(TOKEN))
                .withHeader(DEFAULT_NAV_CALLID, equalTo(CALLID));
    }
}
