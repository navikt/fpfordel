package no.nav.foreldrepenger.mottak.journal.dokarkiv;

import javax.enterprise.context.Dependent;

import no.nav.vedtak.felles.integrasjon.dokarkiv.AbstractDokArkivKlient;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;

@Dependent
@RestClientConfig(tokenConfig = TokenFlow.ADAPTIVE, endpointProperty = "dokarkiv.base.url", endpointDefault = "http://dokarkiv.teamdokumenthandtering/rest/journalpostapi/v1/journalpost", scopesProperty = "dokarkiv.scopes", scopesDefault = "api://prod-fss.teamdokumenthandtering.dokarkiv/.default")
public class DokArkivKlient extends AbstractDokArkivKlient {

    protected DokArkivKlient() {
        super();
    }
}
