package no.nav.foreldrepenger.mottak.journal.dokumentforsendelse;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DokumentforsendelseResponse {

    private String journalpostId;

    private JournalTilstand journalTilstand;

    private List<String> dokumentIdListe = new ArrayList<>();

    private DokumentforsendelseResponse() {

    }

    public String getJournalpostId() {
        return journalpostId;
    }

    public JournalTilstand getJournalTilstand() {
        return journalTilstand;
    }

    public List<String> getDokumentIdListe() {
        return dokumentIdListe;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String journalpostId;
        private JournalTilstand journalTilstand;
        private List<String> dokumentIdListe;

        private Builder() {
            dokumentIdListe = new ArrayList<>();
        }

        public Builder medJournalpostId(String journalpostId) {
            this.journalpostId = journalpostId;
            return this;
        }

        public Builder medJournalTilstand(JournalTilstand journalTilstand) {
            this.journalTilstand = journalTilstand;
            return this;
        }

        public Builder medDokumentIdListe(List<String> dokumentIdListe) {
            this.dokumentIdListe = dokumentIdListe;
            return this;
        }

        public DokumentforsendelseResponse build() {
            verifyStateForBuild();
            DokumentforsendelseResponse response = new DokumentforsendelseResponse();
            response.journalpostId = this.journalpostId;
            response.journalTilstand = this.journalTilstand;
            response.dokumentIdListe = this.dokumentIdListe;
            return response;
        }

        private void verifyStateForBuild() {
            Objects.requireNonNull(journalpostId);
            Objects.requireNonNull(journalTilstand);
        }
    }
}
