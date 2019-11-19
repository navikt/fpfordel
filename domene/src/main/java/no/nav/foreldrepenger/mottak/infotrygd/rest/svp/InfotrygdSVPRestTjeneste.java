package no.nav.foreldrepenger.mottak.infotrygd.rest.svp;

import static no.nav.foreldrepenger.fordel.kodeverk.RelatertYtelseBehandlingstema.SVANGERSKAPSPENGER_BEHANDLINGSTEMA;

import java.net.URI;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import no.nav.foreldrepenger.mottak.infotrygd.rest.InfotrygdRESTTjeneste;
import no.nav.foreldrepenger.mottak.infotrygd.rest.InfotrygdRestResponseMapper;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;
import no.nav.vedtak.konfig.KonfigVerdi;

@ApplicationScoped
@SVP
public class InfotrygdSVPRestTjeneste extends InfotrygdRESTTjeneste {

    private static final String DEFAULT_URI = "http://infotrygd-svangerskapspenger.default/saker";

    InfotrygdSVPRestTjeneste() {
        super();
    }

    @Inject
    public InfotrygdSVPRestTjeneste(OidcRestClient restClient,
            @KonfigVerdi(value = "fpfordel.it.svp.url", defaultVerdi = DEFAULT_URI) URI uri) {
        super(restClient, uri, new InfotrygdRestResponseMapper(SVANGERSKAPSPENGER_BEHANDLINGSTEMA));
    }
}