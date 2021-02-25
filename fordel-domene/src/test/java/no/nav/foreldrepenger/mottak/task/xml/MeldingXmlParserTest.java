package no.nav.foreldrepenger.mottak.task.xml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.junit.jupiter.api.Test;

import no.nav.vedtak.exception.VLException;

class MeldingXmlParserTest {

    @Test
    void skal_hente_ut_namespace_fra_xml() throws Exception {
        final String s = readFile("testsoknader/inntektsmelding-far.xml");

        assertThat(retrieveNameSpaceOfXML(s)).isEqualTo("http://seres.no/xsd/NAV/Inntektsmelding_M/20180924");
    }

    @Test
    void skal_gi_exception_ved_ukjent_namespace() throws Exception {
        String xml = readFile("testsoknader/foedsel-mor-ukjent-namespace.xml");
        assertThrows(VLException.class, () -> MeldingXmlParser.unmarshallXml(xml));
    }

    private String readFile(String filename) throws URISyntaxException, IOException {
        Path path = Paths.get(getClass().getClassLoader().getResource(filename).toURI());
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }

    private static String retrieveNameSpaceOfXML(Source xmlSource) throws XMLStreamException {
        XMLInputFactory xmlif = XMLInputFactory.newInstance();
        XMLStreamReader xmlStreamReader = xmlif.createXMLStreamReader(xmlSource);
        while (!xmlStreamReader.isStartElement()) {
            xmlStreamReader.next();
        }
        return xmlStreamReader.getNamespaceURI();
    }

    private static String retrieveNameSpaceOfXML(String xml) throws XMLStreamException {
        try (final StringReader reader = new StringReader(xml)) {
            return retrieveNameSpaceOfXML(new StreamSource(reader));
        }
    }
}
