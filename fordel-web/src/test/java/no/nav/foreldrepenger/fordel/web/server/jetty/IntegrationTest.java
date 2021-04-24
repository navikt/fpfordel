package no.nav.foreldrepenger.fordel.web.server.jetty;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.tomakehurst.wiremock.WireMockServer;

@ExtendWith(MockitoExtension.class)
@Tag("wiremock")
class IntegrationTest {
    private static WireMockServer server;

    @BeforeAll
    static void startServer() throws Exception {
        server = new WireMockServer(0);
        server.start();
        configureFor(server.port());
    }
}
