package no.nav.foreldrepenger.fordel.web.server.abac;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Priority;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Alternative;

import no.nav.abac.xacml.CommonAttributter;
import no.nav.vedtak.sikkerhet.abac.PdpRequest;
import no.nav.vedtak.sikkerhet.pdp.XacmlRequestBuilderTjeneste;
import no.nav.vedtak.sikkerhet.pdp.xacml.XacmlAttributeSet;
import no.nav.vedtak.sikkerhet.pdp.xacml.XacmlRequestBuilder;
import no.nav.vedtak.util.Tuple;

@Dependent
@Alternative
@Priority(2)
public class XacmlRequestBuilderTjenesteImpl implements XacmlRequestBuilderTjeneste {

    public XacmlRequestBuilderTjenesteImpl() {
    }

    @Override
    public XacmlRequestBuilder lagXacmlRequestBuilder(PdpRequest pdpRequest) {
        XacmlRequestBuilder xacmlBuilder = new XacmlRequestBuilder();

        XacmlAttributeSet actionAttributeSet = new XacmlAttributeSet();
        actionAttributeSet.addAttribute(CommonAttributter.XACML_1_0_ACTION_ACTION_ID, pdpRequest.getString(CommonAttributter.XACML_1_0_ACTION_ACTION_ID));
        xacmlBuilder.addActionAttributeSet(actionAttributeSet);

        List<Tuple<String, String>> identer = hentIdenter(pdpRequest, CommonAttributter.RESOURCE_FELLES_PERSON_FNR, CommonAttributter.RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE);

        if (identer.isEmpty()) {
            xacmlBuilder.addResourceAttributeSet(byggRessursAttributter(pdpRequest, null));
        } else {
            for (Tuple<String, String> ident : identer) {
                xacmlBuilder.addResourceAttributeSet(byggRessursAttributter(pdpRequest, ident));
            }
        }
        return xacmlBuilder;
    }

    private XacmlAttributeSet byggRessursAttributter(PdpRequest pdpRequest, Tuple<String, String> ident) {
        XacmlAttributeSet resourceAttributeSet = new XacmlAttributeSet();
        resourceAttributeSet.addAttribute(CommonAttributter.RESOURCE_FELLES_DOMENE, pdpRequest.getString(CommonAttributter.RESOURCE_FELLES_DOMENE));
        resourceAttributeSet.addAttribute(CommonAttributter.RESOURCE_FELLES_RESOURCE_TYPE, pdpRequest.getString(CommonAttributter.RESOURCE_FELLES_RESOURCE_TYPE));
        if (ident != null) {
            resourceAttributeSet.addAttribute(ident.getElement1(), ident.getElement2());
        }
        return resourceAttributeSet;
    }

    private List<Tuple<String, String>> hentIdenter(PdpRequest pdpRequest, String... identNøkler) {
        List<Tuple<String, String>> identer = new ArrayList<>();
        for (String key : identNøkler) {
            identer.addAll(pdpRequest.getListOfString(key).stream().map(it -> new Tuple<>(key, it)).collect(Collectors.toList()));
        }
        return identer;
    }
}
