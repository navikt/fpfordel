package no.nav.foreldrepenger.mottak.journal;

import no.nav.foreldrepenger.fordel.kodeverk.DokumentTypeId;

public class JournalDokument<T extends DokumentTypeId> {

    private JournalMetadata<T> metadata;
    private String xml; //XML på formatet SoeknadsskjemaEngangsstoenad. Se xsd\no\nav\melding\virksomhet\soeknadsskjemaEngangsstoenad\v1\v1.xsd

    public JournalDokument(JournalMetadata<T> metadata, String xml) {
        this.metadata = metadata;
        this.xml = xml;
    }

    public JournalMetadata<T> getMetadata() {
        return metadata;
    }

    public String getXml() {
        return xml;
    }
}
