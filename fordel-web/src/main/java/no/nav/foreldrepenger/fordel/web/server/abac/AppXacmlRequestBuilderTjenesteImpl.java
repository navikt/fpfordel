package no.nav.foreldrepenger.fordel.web.server.abac;

import static no.nav.vedtak.sikkerhet.abac.NavAbacCommonAttributter.RESOURCE_FELLES_DOMENE;
import static no.nav.vedtak.sikkerhet.abac.NavAbacCommonAttributter.RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE;
import static no.nav.vedtak.sikkerhet.abac.NavAbacCommonAttributter.RESOURCE_FELLES_PERSON_FNR;
import static no.nav.vedtak.sikkerhet.abac.NavAbacCommonAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.vedtak.sikkerhet.abac.NavAbacCommonAttributter.SUBJECT_LEVEL;
import static no.nav.vedtak.sikkerhet.abac.NavAbacCommonAttributter.SUBJECT_TYPE;
import static no.nav.vedtak.sikkerhet.abac.NavAbacCommonAttributter.XACML10_ACTION_ACTION_ID;
import static no.nav.vedtak.sikkerhet.abac.NavAbacCommonAttributter.XACML10_SUBJECT_ID;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Priority;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Alternative;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.sikkerhet.abac.PdpRequest;
import no.nav.vedtak.sikkerhet.pdp.XacmlRequestBuilderTjeneste;
import no.nav.vedtak.sikkerhet.pdp.xacml.XacmlAttributeSet;
import no.nav.vedtak.sikkerhet.pdp.xacml.XacmlRequestBuilder;

@Dependent
@Alternative
@Priority(2)
public class AppXacmlRequestBuilderTjenesteImpl implements XacmlRequestBuilderTjeneste {

    private static final Logger LOG = LoggerFactory.getLogger(AppXacmlRequestBuilderTjenesteImpl.class);

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
        populerSubjects(pdpRequest, xacmlBuilder);

        return xacmlBuilder;
    }

    private static XacmlAttributeSet byggRessursAttributter(PdpRequest pdpRequest, IdentKey ident) {
        var resourceAttributeSet = new XacmlAttributeSet();
        resourceAttributeSet.addAttribute(RESOURCE_FELLES_DOMENE,
                pdpRequest.getString(RESOURCE_FELLES_DOMENE));
        resourceAttributeSet.addAttribute(RESOURCE_FELLES_RESOURCE_TYPE,
                pdpRequest.getString(RESOURCE_FELLES_RESOURCE_TYPE));
        if (ident != null) {
            resourceAttributeSet.addAttribute(ident.key(), ident.ident());
        }
        return resourceAttributeSet;
    }

    private static List<IdentKey> hentIdenter(PdpRequest pdpRequest, String... identNøkler) {
        List<IdentKey> identer = new ArrayList<>();
        for (String key : identNøkler) {
            identer.addAll(pdpRequest.getListOfString(key).stream()
                    .map(it -> new IdentKey(key, it))
                    .collect(Collectors.toList()));
        }
        return identer;
    }

    private void populerSubjects(PdpRequest pdpRequest, XacmlRequestBuilder xacmlBuilder) {
        var attrs = new XacmlAttributeSet();
        var found = false;

        if (pdpRequest.get(XACML10_SUBJECT_ID) != null) {
            attrs.addAttribute(XACML10_SUBJECT_ID, pdpRequest.getString(XACML10_SUBJECT_ID));
            found = true;
        }
        if (pdpRequest.get(SUBJECT_TYPE) != null) {
            attrs.addAttribute(SUBJECT_TYPE, pdpRequest.getString(SUBJECT_TYPE));
            attrs.addAttribute(SUBJECT_LEVEL, Integer.valueOf(pdpRequest.getString(SUBJECT_LEVEL)));
            found = true;
        }

        if (found) {
            LOG.trace("Legger til subject attributter {}", attrs);
            xacmlBuilder.addSubjectAttributeSet(attrs);
        } else {
            LOG.trace("Legger IKKE til suject attributter");
        }
    }
    private static record IdentKey(String key, String ident) {}
}
