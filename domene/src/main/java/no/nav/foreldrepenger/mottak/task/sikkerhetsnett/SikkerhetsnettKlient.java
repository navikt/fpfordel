package no.nav.foreldrepenger.mottak.task.sikkerhetsnett;

import java.util.List;

import jakarta.enterprise.context.Dependent;
import jakarta.ws.rs.core.UriBuilder;
import no.nav.foreldrepenger.fordel.kodeverdi.Tema;
import no.nav.vedtak.felles.integrasjon.rest.RestClient;
import no.nav.vedtak.felles.integrasjon.rest.RestClientConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestConfig;
import no.nav.vedtak.felles.integrasjon.rest.RestRequest;
import no.nav.vedtak.felles.integrasjon.rest.TokenFlow;

@Dependent
@RestClientConfig(tokenConfig = TokenFlow.ADAPTIVE, endpointProperty = "sikkerhetsnett.url",
    endpointDefault = "http://dokarkiv.teamdokumenthandtering/rest/journalpostapi/v1/finnMottatteJournalposter",
    scopesProperty = "dokarkiv.scopes", scopesDefault = "api://prod-fss.teamdokumenthandtering.dokarkiv/.default")
public class SikkerhetsnettKlient {

    private final RestClient restKlient;
    private final RestConfig restConfig;

    protected SikkerhetsnettKlient() {
        this(RestClient.client());
    }

    protected SikkerhetsnettKlient(RestClient client) {
        this.restKlient = client;
        this.restConfig = RestConfig.forClient(this.getClass());
    }


    public List<SikkerhetsnettJournalpost> hentÅpneJournalposterEldreEnn(int antallDagerGamle) {
        var opprett = UriBuilder.fromUri(restConfig.endpoint())
            .queryParam("tema", Tema.FORELDRE_OG_SVANGERSKAPSPENGER.getOffisiellKode())
            .queryParam("antallDagerGamle", String.valueOf(antallDagerGamle))
            .build();
        var restRequest = RestRequest.newGET(opprett, restConfig);
        var respons = restKlient.send(restRequest, SikkerhetsnettResponse.class);
        return respons.journalposter();
    }

    public record SikkerhetsnettResponse(List<SikkerhetsnettJournalpost> journalposter) {

    }

}
