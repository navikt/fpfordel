package no.nav.foreldrepenger.mottak.behandlendeenhet;

import java.util.Set;

import jakarta.enterprise.context.Dependent;
import no.nav.vedtak.felles.integrasjon.rest.FpApplication;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;
import no.nav.vedtak.felles.integrasjon.tilgangfilter.AbstractTilgangFilterKlient;
import no.nav.vedtak.sikkerhet.kontekst.KontekstHolder;
import no.nav.vedtak.sikkerhet.kontekst.RequestKontekst;

@Dependent
@RestClientConfig(tokenConfig = TokenFlow.AZUREAD_CC, application = FpApplication.FPTILGANG)
public class FilterKlient extends AbstractTilgangFilterKlient {

    public FilterKlient() {
        super();
    }

    public Set<String> filterIdenter(Set<String> identer) {
        var ansattOid = KontekstHolder.getKontekst() instanceof RequestKontekst rk ? rk.getOid() : null;
        return ansattOid != null ? super.filterIdenter(ansattOid, identer) : Set.of();
    }
}
