package no.nav.foreldrepenger.fordel.web.app.forvaltning.migrering;

import jakarta.enterprise.context.Dependent;
import no.nav.vedtak.felles.integrasjon.oppgave.v1.AbstractOppgaveKlient;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;

@Dependent
@RestClientConfig(tokenConfig = TokenFlow.AZUREAD_CC, endpointProperty = "oppgave.rs.uri", endpointDefault = "http://oppgave.oppgavehandtering/api/v1/oppgaver", scopesProperty = "oppgave.scopes", scopesDefault = "api://prod-fss.oppgavehandtering.oppgave/.default")
class OppgaveSystemKlient extends AbstractOppgaveKlient {

    public OppgaveSystemKlient() {
        super();
    }
}
