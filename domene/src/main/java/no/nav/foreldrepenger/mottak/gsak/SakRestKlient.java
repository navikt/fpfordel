package no.nav.foreldrepenger.mottak.gsak;

import static java.util.Collections.emptyList;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.http.Header;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.foreldrepenger.fordel.kodeverdi.Fagsystem;
import no.nav.foreldrepenger.fordel.kodeverdi.Tema;
import no.nav.vedtak.felles.integrasjon.rest.OidcRestClient;
import no.nav.vedtak.konfig.KonfigVerdi;
import no.nav.vedtak.log.mdc.MDCOperations;

@ApplicationScoped
public class SakRestKlient {
    private static final Logger LOG = LoggerFactory.getLogger(SakRestKlient.class);
    private static final String HEADER_CORRELATION_ID = "X-Correlation-ID";

    private OidcRestClient sakConsumer;
    private URI uri;

    private static final String DEFAULT_URI = "http://sak.default/api/v1/saker";

    @Inject
    public SakRestKlient(OidcRestClient restClient, @KonfigVerdi(value = "sak.rs.url", defaultVerdi = DEFAULT_URI) URI uri) {
        this.sakConsumer = restClient;
        this.uri = uri;
    }

    public SakRestKlient() {
        // NOSONAR
    }

    public List<GsakSak> finnSakListe(String aktørId, String fagsystem, String tema) {
        try {
            var request = new URIBuilder(uri)
                    .addParameter("aktoerId", aktørId)
                    .addParameter("tema", tema)
                    .addParameter("applikasjon", fagsystem);
            var respons = sakConsumer.get(request.build(), lagHeader(), SakResponse[].class);
            LOG.info("fpfordel sak rest. Fikk saker {}", Arrays.asList(respons));
            return Arrays.stream(respons).map(this::transformer).collect(Collectors.toList());
        } catch (Exception e) {
            LOG.info("fpfordel sak rest. Feil ved oppslag mot {}, returnerer ingen saker", uri, e);
            return emptyList();
        }
    }

    private GsakSak transformer(SakResponse sak) {
        Tema tema = Tema.fraOffisiellKode(sak.getTema());
        Fagsystem fagsystem = Fagsystem.fraKodeDefaultUdefinert(sak.getApplikasjon());
        return new GsakSak(sak.getId().toString(), sak.getId().toString(), tema, fagsystem, sak.getOpprettetTidspunkt());
    }

    private Set<Header> lagHeader() {
        return Collections.singleton(new BasicHeader(HEADER_CORRELATION_ID, MDCOperations.getCallId()));
    }

}
