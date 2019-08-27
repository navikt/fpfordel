package no.nav.foreldrepenger.fordel.web.app.selftest.checks;

import java.io.IOException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
public class OpenAmIssoHealthCheck extends ExtHealthCheck {

    @Inject
    @KonfigVerdi("OpenIdConnect.issoHost")
    private String issoHostUrl;  // NOSONAR

    OpenAmIssoHealthCheck() {
        // for CDI proxy
    }

    @Override
    protected String getDescription() {
        return "Test av OpenAM ISSO";
    }

    @Override
    protected String getEndpoint() {
        // TODO (rune) dette er midlertidig. venter på at denne url'en kan fåes fra app-config
        return issoHostUrl + "/../isAlive.jsp";
    }

    @Override
    protected InternalResult performCheck() {

        InternalResult intTestRes = new InternalResult();

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(getEndpoint());
            try (CloseableHttpResponse httpResponse = httpClient.execute(httpGet)) {
                int responseCode = httpResponse.getStatusLine().getStatusCode();
                if (responseCode != HttpStatus.SC_OK) {
                    intTestRes.setMessage("Fikk uventet HTTP respons-kode: " + responseCode);
                    return intTestRes;
                }
            }
        } catch (IOException e) {
            intTestRes.setException(e);
            return intTestRes;
        }

        intTestRes.noteResponseTime();
        intTestRes.setOk(true);
        return intTestRes;
    }

    @Override
    public boolean erKritiskTjeneste() {
        return false;
    }
}
