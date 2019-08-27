package no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse;

import java.util.Map;
import java.util.UUID;

import no.nav.foreldrepenger.mottak.domene.dokument.DokumentMetadata;

public class Dokumentforsendelse {

    private DokumentMetadata metadata;
    private Map<String, FilMetadata> filMetadataMap;

    public Dokumentforsendelse(DokumentMetadata metadata, Map<String, FilMetadata> filMetadataMap) {
        this.metadata = metadata;
        this.filMetadataMap = filMetadataMap;
    }

    public DokumentMetadata getMetadata() {
        return metadata;
    }

    public UUID getForsendelsesId() {
        return metadata.getForsendelseId();
    }

    public FilMetadata håndter(String contentId) {
        return filMetadataMap.remove(contentId);
    }

    public boolean harHåndtertAlleFiler() {
        return filMetadataMap.isEmpty();
    }
}