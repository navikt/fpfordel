package no.nav.foreldrepenger.mottak.behandlendeenhet;

import javax.enterprise.context.Dependent;

import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;
import no.nav.vedtak.felles.integrasjon.skjerming.AbstractSkjermetPersonKlient;

/*
 * Klient for å sjekke om person er skjermet.
 * Grensesnitt se #skjermingsløsningen
 */
@Dependent
@RestClientConfig(tokenConfig = TokenFlow.AZUREAD_CC, endpointProperty = "skjermet.person.rs.url", endpointDefault = "https://skjermede-personer-pip.intern.nav.no/skjermet",
    scopesProperty = "skjermet.person.rs.azure.scope", scopesDefault = "api://prod-gcp.nom.skjermede-personer-pip/.default")
public class SkjermetPersonKlient extends AbstractSkjermetPersonKlient {

    public SkjermetPersonKlient() {
        super();
    }
}
