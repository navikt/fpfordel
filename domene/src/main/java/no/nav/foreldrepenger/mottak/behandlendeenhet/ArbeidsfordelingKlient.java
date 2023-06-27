package no.nav.foreldrepenger.mottak.behandlendeenhet;

import javax.enterprise.context.Dependent;

import no.nav.vedtak.felles.integrasjon.arbeidsfordeling.AbstractArbeidsfordelingKlient;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;

@Dependent
@RestClientConfig(tokenConfig = TokenFlow.NO_AUTH_NEEDED, endpointProperty = "arbeidsfordeling.rs.url", endpointDefault = "http://norg2.org/norg2/api/v1/arbeidsfordeling/enheter")
public class ArbeidsfordelingKlient extends AbstractArbeidsfordelingKlient {

    public ArbeidsfordelingKlient() {
        super();
    }
}
