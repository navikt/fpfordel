package no.nav.foreldrepenger.mottak.behandlendeenhet;

import javax.enterprise.context.Dependent;

import no.nav.vedtak.felles.integrasjon.oppgave.v1.AbstractOppgaveKlient;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;

@Dependent
@RestClientConfig(tokenConfig = TokenFlow.ADAPTIVE, endpointProperty = "oppgave.rs.uri", endpointDefault = "http://oppgave.default/api/v1/oppgaver",
    scopesProperty = "oppgave.scopes", scopesDefault = "api://prod-fss.oppgavehandtering.oppgave/.default")
public class OppgaveKlient extends AbstractOppgaveKlient {

    public OppgaveKlient() {
        super();
    }
}
