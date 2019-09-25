package no.nav.foreldrepenger.mottak.task.xml;

import static no.nav.vedtak.felles.xml.XmlUtils.retrieveNameSpaceOfXML;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.xml.sax.SAXException;

import no.nav.foreldrepenger.mottak.domene.MottattStrukturertDokument;
import no.nav.vedtak.felles.integrasjon.felles.ws.JaxbHelper;
import no.seres.xsd.nav.inntektsmelding_m._201809.InntektsmeldingConstants;

public final class MeldingXmlParser {

    private static final Map<String, UnmarshallFunction> UNMARSHALL_FUNCTIONS;

    static {
        Map<String, UnmarshallFunction> map = new HashMap<>();

//        TODO: Legge inn og erstatt eksisterende innslag
//              slik at søknadsXML blir validert ved parseing.
        map.put(no.nav.foreldrepenger.søknad.v3.SøknadConstants.NAMESPACE, (xml) -> JaxbHelper.unmarshalXMLWithStAX(no.nav.foreldrepenger.søknad.v3.SøknadConstants.JAXB_CLASS,
                xml,
                no.nav.foreldrepenger.søknad.v3.SøknadConstants.ADDITIONAL_CLASSES));

        map.put(InntektsmeldingConstants.NAMESPACE,
                (xml) -> JaxbHelper.unmarshalAndValidateXMLWithStAX(InntektsmeldingConstants.JAXB_CLASS, xml,
                        InntektsmeldingConstants.XSD_LOCATION));

        map.put(no.seres.xsd.nav.inntektsmelding_m._201812.InntektsmeldingConstants.NAMESPACE,
                (xml) -> JaxbHelper.unmarshalAndValidateXMLWithStAX(no.seres.xsd.nav.inntektsmelding_m._201812.InntektsmeldingConstants.JAXB_CLASS, xml,
                        no.seres.xsd.nav.inntektsmelding_m._201812.InntektsmeldingConstants.XSD_LOCATION));



        UNMARSHALL_FUNCTIONS = Collections.unmodifiableMap(map);
    }

    private MeldingXmlParser() {
    }

    @SuppressWarnings("rawtypes")
    public static MottattStrukturertDokument unmarshallXml(String xml) {
        String nameSpaceOfXML = "ukjent";
        try {
            nameSpaceOfXML = retrieveNameSpaceOfXML(xml);
            UnmarshallFunction unmarshallFunction = UNMARSHALL_FUNCTIONS.get(nameSpaceOfXML);
            if (unmarshallFunction != null) {
                Object jaxbObjekt = unmarshallFunction.parse(xml);
                return MottattStrukturertDokument.toXmlWrapper(jaxbObjekt);
            } else {
                throw MeldingXmlParserFeil.FACTORY.ukjentNamespace(nameSpaceOfXML, new IllegalStateException())
                        .toException();
            }
        } catch (XMLStreamException | JAXBException | SAXException e) {
            throw MeldingXmlParserFeil.FACTORY.uventetFeilVedParsingAvXml(nameSpaceOfXML, e).toException();
        }
    }

    private interface UnmarshallFunction {
        Object parse(String xml) throws JAXBException, SAXException, XMLStreamException;
    }
}
