package no.nav.foreldrepenger.mottak.behandlendeenhet;

import jakarta.enterprise.context.Dependent;
import no.nav.vedtak.felles.integrasjon.rest.FpApplication;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;
import no.nav.vedtak.felles.integrasjon.ruting.AbstractRutingKlient;

@Dependent
@RestClientConfig(tokenConfig = TokenFlow.AZUREAD_CC, application = FpApplication.FPTILGANG)
public class RutingKlient extends AbstractRutingKlient {

    public RutingKlient() {
        super();
    }
}
