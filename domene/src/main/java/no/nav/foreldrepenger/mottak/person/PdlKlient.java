package no.nav.foreldrepenger.mottak.person;

import no.nav.vedtak.felles.integrasjon.person.AbstractPersonKlient;
import no.nav.vedtak.felles.integrasjon.person.Tema;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;

import jakarta.enterprise.context.Dependent;

@RestClientConfig(tokenConfig = TokenFlow.ADAPTIVE, endpointProperty = "pdl.base.url", endpointDefault = "http://pdl-api.pdl/graphql", scopesProperty = "pdl.scopes", scopesDefault = "api://prod-fss.pdl.pdl-api/.default")
@Dependent
public class PdlKlient extends AbstractPersonKlient {

    public PdlKlient() {
        super(RestClient.client(), Tema.FOR);
    }

}
