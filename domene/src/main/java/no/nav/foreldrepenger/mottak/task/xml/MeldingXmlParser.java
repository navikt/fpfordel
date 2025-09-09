package no.nav.foreldrepenger.mottak.task.xml;

import static no.nav.foreldrepenger.xmlutils.JaxbHelper.unmarshalAndValidateXMLWithStAX;
import static no.nav.foreldrepenger.xmlutils.JaxbHelper.unmarshalXMLWithStAX;
import static no.nav.foreldrepenger.xmlutils.XmlUtils.retrieveNameSpaceOfXML;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import javax.xml.stream.XMLStreamException;

import no.nav.foreldrepenger.mottak.domene.MottattStrukturertDokument;
import no.nav.vedtak.exception.TekniskException;
import no.seres.xsd.nav.inntektsmelding_m._201809.InntektsmeldingConstants;

public final class MeldingXmlParser {

    public static final Pattern INNSENDING_MINUTT = Pattern.compile(".*T\\d{2}:\\d{2}</innsendingstidspunkt.*");

    private static final Map<String, UnmarshallFunction> UNMARSHALL_FUNCTIONS = Map.of(

        //        TODO: Legge inn og erstatt eksisterende innslag
        //              slik at søknadsXML blir validert ved parsing.
        no.nav.foreldrepenger.søknad.v3.SøknadConstants.NAMESPACE,
        xml -> unmarshalXMLWithStAX(no.nav.foreldrepenger.søknad.v3.SøknadConstants.JAXB_CLASS, xml,
            no.nav.foreldrepenger.søknad.v3.SøknadConstants.ADDITIONAL_CLASSES),

        InntektsmeldingConstants.NAMESPACE,
        xml -> unmarshalAndValidateXMLWithStAX(InntektsmeldingConstants.JAXB_CLASS, xml, InntektsmeldingConstants.XSD_LOCATION),

        no.seres.xsd.nav.inntektsmelding_m._201812.InntektsmeldingConstants.NAMESPACE,
        xml -> unmarshalAndValidateXMLWithStAX(no.seres.xsd.nav.inntektsmelding_m._201812.InntektsmeldingConstants.JAXB_CLASS, xml,
            no.seres.xsd.nav.inntektsmelding_m._201812.InntektsmeldingConstants.XSD_LOCATION));

    private MeldingXmlParser() {
    }

    public static boolean erXmlMedKjentNamespace(String xml) {
        var nsName = ns(xml);
        return nsName != null && UNMARSHALL_FUNCTIONS.get(nsName) != null;
    }

    public static MottattStrukturertDokument<?> unmarshallXml(String xml) {
        return Optional.ofNullable(UNMARSHALL_FUNCTIONS.get(ns(xml)))
            .map(f -> {
                try {
                    return f.parse(xml);
                } catch (Exception e) {
                    throw new TekniskException("FP-312345",
                        String.format("Feil ved parsing av ukjent journaldokument-type med namespace %s av %s", ns(xml), xml), e);
                }
            })
            .map(MottattStrukturertDokument::toXmlWrapper)
            .orElseThrow(
                () -> new TekniskException("FP-958723", String.format("Fant ikke xsd for namespacet '%s'", ns(xml)), new IllegalStateException()));

    }

    private static String ns(String xml) {
        try {
            return retrieveNameSpaceOfXML(xml);
        } catch (XMLStreamException e) {
            return null;
        }
    }

    private interface UnmarshallFunction {
        Object parse(String xml) throws Exception;
    }

    public static String normaliserInntektsmelding(String xml) {
        if (INNSENDING_MINUTT.matcher(xml).find()) {
            return xml.replace("</innsendingstidspunkt>", ":00</innsendingstidspunkt>");
        } else {
            return xml;
        }
    }
}
