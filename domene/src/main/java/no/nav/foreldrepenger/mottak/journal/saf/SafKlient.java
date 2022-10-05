package no.nav.foreldrepenger.mottak.journal.saf;

import javax.enterprise.context.Dependent;

import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;
import no.nav.vedtak.felles.integrasjon.saf.AbstractSafKlient;

@Dependent
@RestClientConfig(tokenConfig = TokenFlow.ADAPTIVE, endpointProperty = "saf.base.url", endpointDefault = "https://saf.nais.adeo.no",
    scopesProperty = "saf.scopes", scopesDefault = "api://prod-fss.teamdokumenthandtering.saf/.default")
public class SafKlient extends AbstractSafKlient {
    public SafKlient() {
        super();
    }
}
