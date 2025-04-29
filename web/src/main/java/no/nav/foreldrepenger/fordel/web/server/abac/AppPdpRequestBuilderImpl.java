package no.nav.foreldrepenger.fordel.web.server.abac;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;
import no.nav.foreldrepenger.pip.PipRepository;
import no.nav.vedtak.sikkerhet.abac.AbacDataAttributter;
import no.nav.vedtak.sikkerhet.abac.PdpRequestBuilder;
import no.nav.vedtak.sikkerhet.abac.pdp.AppRessursData;
import no.nav.vedtak.sikkerhet.abac.pipdata.PipBehandlingStatus;
import no.nav.vedtak.sikkerhet.abac.pipdata.PipFagsakStatus;

/**
 * Implementasjon av PDP request for denne applikasjonen.
 */

@Dependent
@Alternative
@Priority(2)
public class AppPdpRequestBuilderImpl implements PdpRequestBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(AppPdpRequestBuilderImpl.class);

    private PipRepository pipRepository;

    @Inject
    public AppPdpRequestBuilderImpl(PipRepository pipRepository) {
        this.pipRepository = pipRepository;
        LOG.info("Konstruert");
    }

    @Override
    public AppRessursData lagAppRessursData(AbacDataAttributter dataAttributter) {
        // Kall til fpfordel skal i utgangspunktet kun gjelde 1 person (innsending, oppgave). Setter derfor ikke auditIdent her.
        return minimalbuilder()
            .leggTilIdenter(dataAttributter.getVerdier(AppAbacAttributtType.AKTØR_ID))
            .leggTilIdenter(dataAttributter.getVerdier(AppAbacAttributtType.FNR))
            .leggTilIdenter(pipRepository.hentAktørIdForForsendelser(dataAttributter.getVerdier(AppAbacAttributtType.FORSENDELSE_UUID)))
            .leggTilIdenter(pipRepository.hentAktørIdForOppgave(dataAttributter.getVerdier(AppAbacAttributtType.JOURNALPOST_ID)))
            .build();

    }

    @Override
    public AppRessursData lagAppRessursDataForSystembruker(AbacDataAttributter dataAttributter) {
        return minimalbuilder().build();
    }

    private static AppRessursData.Builder minimalbuilder() {
        return AppRessursData.builder()
            .medFagsakStatus(PipFagsakStatus.UNDER_BEHANDLING)
            .medBehandlingStatus(PipBehandlingStatus.UTREDES);
    }

}
