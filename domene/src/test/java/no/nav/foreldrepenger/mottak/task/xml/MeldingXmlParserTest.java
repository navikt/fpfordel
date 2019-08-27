package no.nav.foreldrepenger.mottak.task.xml;

import static org.assertj.core.api.Assertions.assertThat;

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

import org.junit.Test;

import no.nav.foreldrepenger.mottak.domene.EngangsstønadSøknad;
import no.nav.foreldrepenger.mottak.domene.MottattStrukturertDokument;
import no.nav.vedtak.exception.VLException;

public class MeldingXmlParserTest {

    @Test
    public void skal_hente_ut_namespace_fra_soeknadsskjema_xml() throws Exception {
        final String s = readFile("testsoknader/foedsel-mor.xml");

        assertThat(retrieveNameSpaceOfXML(s)).isEqualTo("http://nav.no/foreldrepenger/soeknadsskjema/engangsstoenad/v1");
    }

    @Test
    public void skal_returnere_representativ_wrapper_klasse_fra_parser() throws Exception {
        String xml = readFile("testsoknader/foedsel-mor.xml");

        final Object soeknadsskjema = MeldingXmlParser.unmarshallXml(xml);

        assertThat(soeknadsskjema).isInstanceOf(MottattStrukturertDokument.class);
        assertThat(soeknadsskjema).isInstanceOf(EngangsstønadSøknad.class);
    }

    @Test(expected = VLException.class)
    public void skal_gi_exception_ved_ukjent_namespace() throws Exception {
        String xml = readFile("testsoknader/foedsel-mor-ukjent-namespace.xml");

        MeldingXmlParser.unmarshallXml(xml);
    }

    private String readFile(String filename) throws URISyntaxException, IOException {
        Path path = Paths.get(getClass().getClassLoader().getResource(filename).toURI());
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
    

    public static String retrieveNameSpaceOfXML(Source xmlSource) throws XMLStreamException {
        XMLInputFactory xmlif = XMLInputFactory.newInstance();
        XMLStreamReader xmlStreamReader = xmlif.createXMLStreamReader(xmlSource);
        while (!xmlStreamReader.isStartElement()) {
            xmlStreamReader.next();
        }
        return xmlStreamReader.getNamespaceURI();
    }

    public static String retrieveNameSpaceOfXML(String xml) throws XMLStreamException {
        try (final StringReader reader = new StringReader(xml)) {
            return retrieveNameSpaceOfXML(new StreamSource(reader));
        }
    }
}
