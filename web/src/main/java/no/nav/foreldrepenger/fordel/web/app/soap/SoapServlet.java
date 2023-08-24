package no.nav.foreldrepenger.fordel.web.app.soap;

import no.nav.tjeneste.virksomhet.behandledokumentforsendelse.v1.binding.BehandleDokumentforsendelseV1;
import no.nav.vedtak.felles.integrasjon.felles.ws.AbstractSoapServlet;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.servlet.annotation.WebServlet;

@ApplicationScoped
@WebServlet(urlPatterns = {"/tjenester", "/tjenester/", "/tjenester/*"}, loadOnStartup = 1)
public class SoapServlet extends AbstractSoapServlet { //NOSONAR

    @Inject
    public void publishBehandleDokumentService(BehandleDokumentforsendelseV1 behandleDokumentService) {
        publish(behandleDokumentService);
    }
}
