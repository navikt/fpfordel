package no.nav.foreldrepenger.mottak.task.xml;

import no.nav.vedtak.exception.TekniskException;

class MeldingXmlParserFeil {

    static TekniskException ukjentNamespace(String ns, Exception e) {
        return new TekniskException("FP-958723", String.format("Fant ikke xsd for namespacet '%s'", ns), e);
    }

    static TekniskException uventetFeilVedParsingAvXml(String ns, String xml, Exception e) {
        return new TekniskException("FP-312345", String.format("Feil ved parsing av ukjent journaldokument-type med namespace %s av %s", ns, xml), e);

    }
}
