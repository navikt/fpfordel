package no.nav.foreldrepenger.fordel.web.server.abac;



import static no.nav.vedtak.sikkerhet.abac.NavAbacCommonAttributter.RESOURCE_FELLES_DOMENE;
import static no.nav.vedtak.sikkerhet.abac.NavAbacCommonAttributter.RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE;
import static no.nav.vedtak.sikkerhet.abac.NavAbacCommonAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.vedtak.sikkerhet.abac.NavAbacCommonAttributter.XACML10_ACTION_ACTION_ID;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Priority;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;

import no.nav.foreldrepenger.pip.PipRepository;
import no.nav.vedtak.sikkerhet.abac.AbacAttributtSamling;
import no.nav.vedtak.sikkerhet.abac.PdpKlient;
import no.nav.vedtak.sikkerhet.abac.PdpRequest;
import no.nav.vedtak.sikkerhet.abac.PdpRequestBuilder;

/**
 * Implementasjon av PDP request for denne applikasjonen.
 */

@Dependent
@Alternative
@Priority(2)
public class AppPdpRequestBuilderImpl implements PdpRequestBuilder {
    public static final String ABAC_DOMAIN = "foreldrepenger";
    private PipRepository pipRepository;

    public AppPdpRequestBuilderImpl() {
    }

    @Inject
    public AppPdpRequestBuilderImpl(PipRepository pipRepository) {
        this.pipRepository = pipRepository;
    }

    @Override
    public PdpRequest lagPdpRequest(AbacAttributtSamling attributter) {
        PdpRequest pdpRequest = new PdpRequest();
        pdpRequest.put(RESOURCE_FELLES_DOMENE, ABAC_DOMAIN);
        pdpRequest.put(PdpKlient.ENVIRONMENT_AUTH_TOKEN, attributter.getIdToken());
        pdpRequest.put(XACML10_ACTION_ACTION_ID, attributter.getActionType().getEksternKode());
        pdpRequest.put(RESOURCE_FELLES_RESOURCE_TYPE, attributter.getResource());

        // Potensielt noen saksnummer i AbacAttributtSamling - overlater til fpsak å
        // håndheve disse
        Set<String> aktørIder = utledAktørIder(attributter);
        pdpRequest.put(RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE, aktørIder);
        return pdpRequest;
    }

    private Set<String> utledAktørIder(AbacAttributtSamling attributter) {
        Set<String> aktørIder = new HashSet<>(attributter.getVerdier(AppAbacAttributtType.AKTØR_ID));
        aktørIder.addAll(pipRepository
                .hentAktørIdForForsendelser(attributter.getVerdier(AppAbacAttributtType.FORSENDELSE_UUID)));
        return aktørIder;
    }
}
