package no.nav.foreldrepenger.fordel.web.server.abac;


import static no.nav.vedtak.sikkerhet.abac.NavAbacCommonAttributter.ENVIRONMENT_FELLES_OIDC_TOKEN_BODY;
import static no.nav.vedtak.sikkerhet.abac.NavAbacCommonAttributter.ENVIRONMENT_FELLES_SAML_TOKEN;
import static no.nav.vedtak.sikkerhet.abac.NavAbacCommonAttributter.RESOURCE_FELLES_DOMENE;
import static no.nav.vedtak.sikkerhet.abac.NavAbacCommonAttributter.RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE;
import static no.nav.vedtak.sikkerhet.abac.NavAbacCommonAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.vedtak.sikkerhet.abac.NavAbacCommonAttributter.XACML10_ACTION_ACTION_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import no.nav.vedtak.sikkerhet.abac.AbacIdToken;
import no.nav.vedtak.sikkerhet.abac.BeskyttetRessursActionAttributt;
import no.nav.vedtak.sikkerhet.abac.PdpKlient;
import no.nav.vedtak.sikkerhet.abac.PdpRequest;
import no.nav.vedtak.sikkerhet.pdp.PdpConsumer;
import no.nav.vedtak.sikkerhet.pdp.PdpKlientImpl;
import no.nav.vedtak.sikkerhet.pdp.xacml.XacmlRequestBuilder;
import no.nav.vedtak.sikkerhet.pdp.xacml.XacmlResponseWrapper;

public class AppXacmlRequestBuilderTjenesteImplTest {

    public static final String JWT_TOKEN = "eyAidHlwIjogIkpXVCIsICJraWQiOiAiU0gxSWVSU2sxT1VGSDNzd1orRXVVcTE5VHZRPSIsICJhbGciOiAiUlMyNTYiIH0.eyAiYXRfaGFzaCI6ICIyb2c1RGk5ZW9LeFhOa3VPd0dvVUdBIiwgInN1YiI6ICJzMTQyNDQzIiwgImF1ZGl0VHJhY2tpbmdJZCI6ICI1NTM0ZmQ4ZS03MmE2LTRhMWQtOWU5YS1iZmEzYThhMTljMDUtNjE2NjA2NyIsICJpc3MiOiAiaHR0cHM6Ly9pc3NvLXQuYWRlby5ubzo0NDMvaXNzby9vYXV0aDIiLCAidG9rZW5OYW1lIjogImlkX3Rva2VuIiwgImF1ZCI6ICJPSURDIiwgImNfaGFzaCI6ICJiVWYzcU5CN3dTdi0wVlN0bjhXLURnIiwgIm9yZy5mb3JnZXJvY2sub3BlbmlkY29ubmVjdC5vcHMiOiAiMTdhOGZiMzYtMGI0Ny00YzRkLWE4YWYtZWM4Nzc3Y2MyZmIyIiwgImF6cCI6ICJPSURDIiwgImF1dGhfdGltZSI6IDE0OTgwMzk5MTQsICJyZWFsbSI6ICIvIiwgImV4cCI6IDE0OTgwNDM1MTUsICJ0b2tlblR5cGUiOiAiSldUVG9rZW4iLCAiaWF0IjogMTQ5ODAzOTkxNSB9.S2DKQweQWZIfjaAT2UP9_dxrK5zqpXj8IgtjDLt5PVfLYfZqpWGaX-ckXG0GlztDVBlRK4ylmIYacTmEAUV_bRa_qWKRNxF83SlQRgHDSiE82SGv5WHOGEcAxf2w_d50XsgA2KDBCyv0bFIp9bCiKzP11uWPW0v4uIkyw2xVxMVPMCuiMUtYFh80sMDf9T4FuQcFd0LxoYcSFDEDlwCdRiF3ufw73qtMYBlNIMbTGHx-DZWkZV7CgukmCee79gwQIvGwdLrgaDrHFCJUDCbB1FFEaE3p3_BZbj0T54fCvL69aHyWm1zEd9Pys15yZdSh3oSSr4yVNIxhoF-nQ7gY-g;";
    private PdpKlientImpl pdpKlient;
    private PdpConsumer pdpConsumerMock;
    private AppXacmlRequestBuilderTjenesteImpl xamlRequestBuilderTjeneste;

    @BeforeEach
    public void setUp() {
        pdpConsumerMock = mock(PdpConsumer.class);
        xamlRequestBuilderTjeneste = new AppXacmlRequestBuilderTjenesteImpl();
        pdpKlient = new PdpKlientImpl(pdpConsumerMock, xamlRequestBuilderTjeneste);
    }

    @Test
    public void kallPdpMedSamlTokenNårIdTokenErSamlToken() throws Exception {
        AbacIdToken idToken = AbacIdToken.withSamlToken("SAML");
        XacmlResponseWrapper responseWrapper = createResponse("testdata/xacmlresponse.json");
        ArgumentCaptor<XacmlRequestBuilder> captor = ArgumentCaptor.forClass(XacmlRequestBuilder.class);

        when(pdpConsumerMock.evaluate(captor.capture())).thenReturn(responseWrapper);
        PdpRequest pdpRequest = lagPdpRequest();
        pdpRequest.put(RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE, Collections.singleton("9000000000009"));
        pdpRequest.put(PdpKlient.ENVIRONMENT_AUTH_TOKEN, idToken);
        pdpKlient.forespørTilgang(pdpRequest);

        JsonObject request = captor.getValue().build();
        assertThat(request.toString().contains(ENVIRONMENT_FELLES_SAML_TOKEN)).isTrue();
        assertThat(request.toString().contains(RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE)).isTrue();
    }

    @Test
    public void kallPdpUtenFnrResourceHvisPersonlisteErTom() throws FileNotFoundException {
        AbacIdToken idToken = AbacIdToken.withOidcToken(JWT_TOKEN);
        XacmlResponseWrapper responseWrapper = createResponse("testdata/xacmlresponse.json");
        ArgumentCaptor<XacmlRequestBuilder> captor = ArgumentCaptor.forClass(XacmlRequestBuilder.class);

        when(pdpConsumerMock.evaluate(captor.capture())).thenReturn(responseWrapper);

        PdpRequest pdpRequest = lagPdpRequest();
        pdpRequest.put(RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE, Collections.emptySet());
        pdpRequest.put(PdpKlient.ENVIRONMENT_AUTH_TOKEN, idToken);
        pdpKlient.forespørTilgang(pdpRequest);

        assertThat(captor.getValue().build().toString().contains(RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE)).isFalse();
    }

    @Test
    public void kallPdpMedJwtTokenBodyNårIdTokenErJwtToken() throws Exception {
        AbacIdToken idToken = AbacIdToken.withOidcToken(JWT_TOKEN);
        XacmlResponseWrapper responseWrapper = createResponse("testdata/xacmlresponse.json");
        ArgumentCaptor<XacmlRequestBuilder> captor = ArgumentCaptor.forClass(XacmlRequestBuilder.class);

        when(pdpConsumerMock.evaluate(captor.capture())).thenReturn(responseWrapper);

        PdpRequest pdpRequest = lagPdpRequest();
        pdpRequest.put(RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE, Collections.singleton("9000000000009"));
        pdpRequest.put(PdpKlient.ENVIRONMENT_AUTH_TOKEN, idToken);
        pdpKlient.forespørTilgang(pdpRequest);

        assertThat(captor.getValue().build().toString().contains(ENVIRONMENT_FELLES_OIDC_TOKEN_BODY)).isTrue();
    }

    @Test
    public void kallPdpMedFlereAttributtSettNårPersonlisteStørreEnn1() throws FileNotFoundException {
        AbacIdToken idToken = AbacIdToken.withOidcToken(JWT_TOKEN);
        XacmlResponseWrapper responseWrapper = createResponse("testdata/xacml3response.json");
        ArgumentCaptor<XacmlRequestBuilder> captor = ArgumentCaptor.forClass(XacmlRequestBuilder.class);

        when(pdpConsumerMock.evaluate(captor.capture())).thenReturn(responseWrapper);
        Set<String> personnr = new HashSet<>();
        personnr.add("9000000000009");
        personnr.add("9000000000008");
        personnr.add("9000000000007");

        PdpRequest pdpRequest = lagPdpRequest();
        pdpRequest.put(RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE, personnr);
        pdpRequest.put(PdpKlient.ENVIRONMENT_AUTH_TOKEN, idToken);
        pdpKlient.forespørTilgang(pdpRequest);

        String xacmlRequestString = captor.getValue().build().toString();

        assertThat(xacmlRequestString.contains("9000000000009")).isTrue();
        assertThat(xacmlRequestString.contains("9000000000008")).isTrue();
        assertThat(xacmlRequestString.contains("9000000000007")).isTrue();
    }

    private static PdpRequest lagPdpRequest() {
        PdpRequest request = new PdpRequest();
        request.put(RESOURCE_FELLES_DOMENE, "foreldrepenger");
        request.put(XACML10_ACTION_ACTION_ID, BeskyttetRessursActionAttributt.READ.getEksternKode());
        request.put(RESOURCE_FELLES_RESOURCE_TYPE, BeskyttetRessursAttributt.FAGSAK);
        return request;
    }

    private XacmlResponseWrapper createResponse(String jsonFile) throws FileNotFoundException {
        File file = new File(getClass().getClassLoader().getResource(jsonFile).getFile());
        JsonReader reader = Json.createReader(new FileReader(file));
        JsonObject jo = (JsonObject) reader.read();
        reader.close();
        return new XacmlResponseWrapper(jo);

    }
}
