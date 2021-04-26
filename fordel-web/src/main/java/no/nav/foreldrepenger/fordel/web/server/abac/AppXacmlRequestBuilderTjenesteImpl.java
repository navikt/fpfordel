package no.nav.foreldrepenger.fordel.web.server.abac;

import static no.nav.vedtak.sikkerhet.abac.NavAbacCommonAttributter.RESOURCE_FELLES_DOMENE;
import static no.nav.vedtak.sikkerhet.abac.NavAbacCommonAttributter.RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE;
import static no.nav.vedtak.sikkerhet.abac.NavAbacCommonAttributter.RESOURCE_FELLES_PERSON_FNR;
import static no.nav.vedtak.sikkerhet.abac.NavAbacCommonAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.vedtak.sikkerhet.abac.NavAbacCommonAttributter.XACML10_ACTION_ACTION_ID;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Priority;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Alternative;

import no.nav.vedtak.sikkerhet.abac.PdpRequest;
import no.nav.vedtak.sikkerhet.pdp.XacmlRequestBuilderTjeneste;
import no.nav.vedtak.sikkerhet.pdp.xacml.XacmlAttributeSet;
import no.nav.vedtak.sikkerhet.pdp.xacml.XacmlRequestBuilder;
import no.nav.vedtak.util.Tuple;

@Dependent
@Alternative
@Priority(2)
public class AppXacmlRequestBuilderTjenesteImpl implements XacmlRequestBuilderTjeneste {

    public AppXacmlRequestBuilderTjenesteImpl() {
    }

    @Override
    public XacmlRequestBuilder lagXacmlRequestBuilder(PdpRequest pdpRequest) {
        XacmlRequestBuilder xacmlBuilder = new XacmlRequestBuilder();

        XacmlAttributeSet actionAttributeSet = new XacmlAttributeSet();
        actionAttributeSet.addAttribute(XACML10_ACTION_ACTION_ID, pdpRequest.getString(XACML10_ACTION_ACTION_ID));
        xacmlBuilder.addActionAttributeSet(actionAttributeSet);

        var identer = hentIdenter(pdpRequest, RESOURCE_FELLES_PERSON_FNR, RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE);

        if (identer.isEmpty()) {
            xacmlBuilder.addResourceAttributeSet(byggRessursAttributter(pdpRequest, null));
        } else {
            for (var ident : identer) {
                xacmlBuilder.addResourceAttributeSet(byggRessursAttributter(pdpRequest, ident));
            }
        }
        return xacmlBuilder;
    }

    private static XacmlAttributeSet byggRessursAttributter(PdpRequest pdpRequest, Tuple<String, String> ident) {
        var resourceAttributeSet = new XacmlAttributeSet();
        resourceAttributeSet.addAttribute(RESOURCE_FELLES_DOMENE,
                pdpRequest.getString(RESOURCE_FELLES_DOMENE));
        resourceAttributeSet.addAttribute(RESOURCE_FELLES_RESOURCE_TYPE,
                pdpRequest.getString(RESOURCE_FELLES_RESOURCE_TYPE));
        if (ident != null) {
            resourceAttributeSet.addAttribute(ident.getElement1(), ident.getElement2());
        }
        return resourceAttributeSet;
    }

    private static List<Tuple<String, String>> hentIdenter(PdpRequest pdpRequest, String... identNøkler) {
        List<Tuple<String, String>> identer = new ArrayList<>();
        for (String key : identNøkler) {
            identer.addAll(pdpRequest.getListOfString(key).stream()
                    .map(it -> new Tuple<>(key, it))
                    .collect(Collectors.toList()));
        }
        return identer;
    }
}
