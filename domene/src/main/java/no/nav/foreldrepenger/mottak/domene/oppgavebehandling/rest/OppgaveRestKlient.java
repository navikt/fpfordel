package no.nav.foreldrepenger.mottak.domene.oppgavebehandling.rest;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.http.Header;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicHeader;

import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;
import no.nav.vedtak.konfig.KonfigVerdi;
import no.nav.vedtak.log.mdc.MDCOperations;

@ApplicationScoped
public class OppgaveRestKlient {

    private static final String ENDPOINT_KEY = "oppgave.rs.uri";
    private static final String DEFAULT_URI = "http://oppgave.default/api/v1/oppgaver";
    private static final String HEADER_CORRELATION_ID = "X-Correlation-ID";

    private OidcRestClient oidcRestClient;
    private URI endpoint;

    public OppgaveRestKlient() {
    }

    @Inject
    public OppgaveRestKlient(OidcRestClient oidcRestClient,
                             @KonfigVerdi(value = ENDPOINT_KEY, defaultVerdi = DEFAULT_URI) URI endpoint) {
        this.oidcRestClient = oidcRestClient ;
        this.endpoint = endpoint;
    }

    public Oppgave opprettetOppgave(OpprettOppgave.Builder requestBuilder) {
        return oidcRestClient.post(endpoint, requestBuilder.build(), lagHeader(), Oppgave.class);
    }

    public List<Oppgave> finnOppgaver(String aktørId, String tema, List<String> oppgaveTyper) throws Exception {
        var builder = new URIBuilder(endpoint).addParameter("aktoerId", aktørId);
        if (tema != null)
            builder.addParameter("tema", tema);
        oppgaveTyper.forEach(ot -> builder.addParameter("oppgavetype", ot));
        return oidcRestClient.get(builder.build(), lagHeader(), FinnOppgaveResponse.class).getOppgaver();
    }

    public void ferdigstillOppgave(String oppgaveId) {
        var oppgave = hentOppgave(oppgaveId);
        var patch = new PatchOppgave(oppgave.getId(), oppgave.getVersjon(), Oppgavestatus.FERDIGSTILT);
        oidcRestClient.patch(endpoint, patch, lagHeader());
    }

    public void feilregistrerOppgave(String oppgaveId) {
        oidcRestClient.get(endpoint, lagHeader(), FinnOppgaveResponse.class).getOppgaver();
    }

    private Oppgave hentOppgave(String oppgaveId) {
        return oidcRestClient.get(getEndpointForOppgaveId(oppgaveId), lagHeader(), Oppgave.class);
    }

    private URI getEndpointForOppgaveId(String oppgaveId) {
        return URI.create(endpoint.toString() + "/" + oppgaveId);
    }

    private Set<Header> lagHeader() {
        return Collections.singleton(new BasicHeader(HEADER_CORRELATION_ID, MDCOperations.getCallId()));
    }

}
