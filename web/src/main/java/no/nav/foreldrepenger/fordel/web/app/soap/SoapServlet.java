package no.nav.foreldrepenger.fordel.web.app.soap;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.annotation.WebServlet;

import no.nav.tjeneste.virksomhet.behandledokumentforsendelse.v1.BehandleDokumentforsendelseV1;
import no.nav.vedtak.felles.integrasjon.felles.ws.AbstractSoapServlet;

@ApplicationScoped
@WebServlet(urlPatterns = { "/tjenester", "/tjenester/", "/tjenester/*" }, loadOnStartup = 1)
public class SoapServlet extends AbstractSoapServlet {

    @Inject
    public void publishBehandleDokumentService(BehandleDokumentforsendelseV1 behandleDokumentService) {
        publish(behandleDokumentService);
    }

}
