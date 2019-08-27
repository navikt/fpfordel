package no.nav.foreldrepenger.mottak.task.xml;

import static no.nav.vedtak.feil.LogLevel.ERROR;
import static no.nav.vedtak.feil.LogLevel.WARN;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

interface MeldingXmlParserFeil extends DeklarerteFeil {

    MeldingXmlParserFeil FACTORY = FeilFactory.create(MeldingXmlParserFeil.class);

    @TekniskFeil(feilkode = "FP-958723", feilmelding = "Fant ikke xsd for namespacet '%s'", logLevel = WARN)
    Feil ukjentNamespace(String namespace, IllegalStateException e);

    @TekniskFeil(feilkode = "FP-312345", feilmelding = "Feil ved parsing av ukjent journaldokument-type med namespace '%s'", logLevel = ERROR)
    Feil uventetFeilVedParsingAvXml(String namespace, Exception e);
}
