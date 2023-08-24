package no.nav.foreldrepenger.mottak.journal.saf;

import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;
import no.nav.vedtak.felles.integrasjon.saf.AbstractSafKlient;

import jakarta.enterprise.context.Dependent;

@Dependent
@RestClientConfig(tokenConfig = TokenFlow.ADAPTIVE, endpointProperty = "saf.base.url", endpointDefault = "http://saf.teamdokumenthandtering", scopesProperty = "saf.scopes", scopesDefault = "api://prod-fss.teamdokumenthandtering.saf/.default")
public class SafKlient extends AbstractSafKlient {
    public SafKlient() {
        super();
    }
}
