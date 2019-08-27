package no.nav.foreldrepenger.fordel.web.app.tjenester;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.MediaType.TEXT_HTML;
import static javax.ws.rs.core.MediaType.TEXT_HTML_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;

import no.nav.foreldrepenger.fordel.web.app.selftest.SelftestService;

@SuppressWarnings("resource")
public class SelftestRestTjenesteTest {

    private static final String RESPONSE_ENCODING = "UTF-8";

    private SelftestRestTjeneste restTjeneste;

    private SelftestService selftestServiceMock = mock(SelftestService.class);

    @Before
    public void setup() {
        restTjeneste = new SelftestRestTjeneste(selftestServiceMock);
    }

    @Test
    public void test_doSelftest_med_json_type() {
        when(selftestServiceMock.doSelftest(any(), anyBoolean())).thenReturn(lagJsonTypeResponse());

        Response response = restTjeneste.doSelftest(APPLICATION_JSON, false);

        verify(selftestServiceMock).doSelftest(any(), anyBoolean());
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(response.getMediaType()).isEqualTo(APPLICATION_JSON_TYPE);
    }

    @Test
    public void test_doSelftest_med_html_type() {
        when(selftestServiceMock.doSelftest(any(), anyBoolean())).thenReturn(lagHtmlTypeResponse());

        Response response = restTjeneste.doSelftest(TEXT_HTML, false);

        verify(selftestServiceMock).doSelftest(TEXT_HTML, false);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(response.getMediaType()).isEqualTo(TEXT_HTML_TYPE);
    }

    @Test
    public void test_doSelftest_med_html_type_json_format() {
        when(selftestServiceMock.doSelftest(any(), anyBoolean())).thenReturn(lagHtmlTypeResponse());

        @SuppressWarnings("resource")
        Response response = restTjeneste.doSelftest(TEXT_HTML, true);

        verify(selftestServiceMock).doSelftest(TEXT_HTML, true);
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        assertThat(response.getMediaType()).isEqualTo(TEXT_HTML_TYPE);
    }

    private Response lagJsonTypeResponse() {
        byte[] s = {'a', 'b'};

        return Response.ok()
                .encoding(RESPONSE_ENCODING)
                .type(APPLICATION_JSON)
                .entity(s)
                .build();
    }

    private Response lagHtmlTypeResponse() {
        byte[] s = {'a','b'};
        return Response.ok()
                .encoding(RESPONSE_ENCODING)
                .type(TEXT_HTML)
                .entity(s)
                .build();
    }
}