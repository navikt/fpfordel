package no.nav.foreldrepenger.mottak.journal.dokarkiv.model;

import java.util.ArrayList;
import java.util.List;

import no.nav.foreldrepenger.fordel.kodeverdi.Fagsystem;
import no.nav.foreldrepenger.mottak.journal.saf.model.BrukerIdType;

public class OppdaterJournalpostRequest {

    private String tittel;
    private String tema;
    private String behandlingstema;
    private Sak sak;
    private Bruker bruker;
    private AvsenderMottaker avsenderMottaker;
    private List<DokumentInfoOppdater> dokumenter;

    public OppdaterJournalpostRequest(String tittel,
            String tema,
            String behandlingstema,
            Bruker bruker,
            AvsenderMottaker avsenderMottaker,
            Sak sak,
            List<DokumentInfoOppdater> dokumenter) {
        this.tittel = tittel;
        this.tema = tema;
        this.behandlingstema = behandlingstema;
        this.sak = sak;
        this.bruker = bruker;
        this.avsenderMottaker = avsenderMottaker;
        this.dokumenter = dokumenter;
    }

    private OppdaterJournalpostRequest() {

    }

    public String getTittel() {
        return tittel;
    }

    public String getTema() {
        return tema;
    }

    public String getBehandlingstema() {
        return behandlingstema;
    }

    public Sak getSak() {
        return sak;
    }

    public Bruker getBruker() {
        return bruker;
    }

    public AvsenderMottaker getAvsenderMottaker() {
        return avsenderMottaker;
    }

    public List<DokumentInfoOppdater> getDokumenter() {
        return dokumenter;
    }

    public static Builder ny() {
        return new Builder();
    }

    @Override
    public String toString() {
        return "OppdaterJournalpostRequest{" +
                "tittel='" + tittel + '\'' +
                ", tema='" + tema + '\'' +
                ", behandlingstema='" + behandlingstema + '\'' +
                ", sak=" + sak +
                ", bruker=" + bruker +
                ", avsenderMottaker=" + avsenderMottaker +
                ", dokumenter=" + dokumenter +
                '}';
    }

    public static class Builder {
        private OppdaterJournalpostRequest request;

        Builder() {
            request = new OppdaterJournalpostRequest();
        }

        public Builder medTittel(String tittel) {
            this.request.tittel = tittel;
            return this;
        }

        public Builder medTema(String tema) {
            this.request.tema = tema;
            return this;
        }

        public Builder medBehandlingstema(String behandlingstema) {
            this.request.behandlingstema = behandlingstema;
            return this;
        }

        public Builder medBruker(String aktørId) {
            this.request.bruker = new Bruker(aktørId, BrukerIdType.AKTOERID);
            return this;
        }

        public Builder medAvsender(String fnr, String navn) {
            this.request.avsenderMottaker = new AvsenderMottaker(fnr, AvsenderMottakerIdType.FNR, navn);
            return this;
        }

        public Builder medSak(Sak sak) {
            this.request.sak = sak;
            return this;
        }

        public Builder medArkivSak(String arkivSakID) {

            return this;
        }

        public Builder leggTilDokument(DokumentInfoOppdater dokument) {
            if (this.request.dokumenter == null) {
                this.request.dokumenter = new ArrayList<>();
            }
            this.request.dokumenter.add(dokument);
            return this;
        }

        public OppdaterJournalpostRequest build() {
            return this.request;
        }
    }
}
