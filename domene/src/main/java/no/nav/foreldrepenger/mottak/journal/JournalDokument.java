package no.nav.foreldrepenger.mottak.journal;

public class JournalDokument {

    private JournalMetadata metadata;
    private String xml; // XML på formatet SoeknadsskjemaEngangsstoenad. Se
                        // xsd\no\nav\melding\virksomhet\soeknadsskjemaEngangsstoenad\v1\v1.xsd

    public JournalDokument(JournalMetadata metadata, String xml) {
        this.metadata = metadata;
        this.xml = xml;
    }

    public JournalMetadata getMetadata() {
        return metadata;
    }

    public String getXml() {
        return xml;
    }
}
