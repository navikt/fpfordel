package no.nav.foreldrepenger.mottak.journal;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import no.nav.foreldrepenger.fordel.kodeverdi.ArkivFilType;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentKategori;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverdi.VariantFormat;

public class JournalMetadata {

    public enum Journaltilstand {
        MIDLERTIDIG,
        UTGAAR,
        ENDELIG
    }

    private String journalpostId;
    private String dokumentId;
    private VariantFormat variantFormat;
    private Journaltilstand journaltilstand;
    private DokumentTypeId dokumentTypeId;
    private DokumentKategori dokumentKategori;
    private ArkivFilType arkivFilType;
    private boolean erHoveddokument;
    private LocalDate forsendelseMottatt;
    private LocalDateTime forsendelseMottattTidspunkt;
    private List<String> brukerIdentListe;
    private String kanalReferanseId;
    private String mottaksKanal;
    private String journalEnhet;

    public static Builder builder() {
        return new Builder();
    }

    public String getJournalpostId() {
        return journalpostId;
    }

    public String getDokumentId() {
        return dokumentId;
    }

    public VariantFormat getVariantFormat() {
        return variantFormat;
    }

    public DokumentTypeId getDokumentTypeId() {
        return dokumentTypeId;
    }

    public Optional<DokumentKategori> getDokumentKategori() {
        return Optional.ofNullable(dokumentKategori);
    }

    public ArkivFilType getArkivFilType() {
        return arkivFilType;
    }

    public Journaltilstand getJournaltilstand() {
        return journaltilstand;
    }

    public boolean getErHoveddokument() {
        return erHoveddokument;
    }

    public List<String> getBrukerIdentListe() {
        if (brukerIdentListe == null) {
            brukerIdentListe = new ArrayList<>();
        }
        return brukerIdentListe;
    }

    public LocalDate getForsendelseMottatt() {
        return forsendelseMottatt;
    }

    public LocalDateTime getForsendelseMottattTidspunkt() {
        return forsendelseMottattTidspunkt;
    }

    public String getJournalførendeEnhet() {
        return journalEnhet;
    }

    public String getKanalReferanseId() {
        return kanalReferanseId;
    }

    public String getMottaksKanal() {
        return mottaksKanal;
    }

    public static class Builder {
        private String journalpostId;
        private String dokumentId;
        private VariantFormat variantFormat;
        private DokumentTypeId dokumentTypeId;
        private DokumentKategori dokumentKategori;
        private ArkivFilType arkivFilType;
        private Journaltilstand journaltilstand;
        private boolean erHoveddokument;
        private LocalDate forsendelseMottatt;
        private LocalDateTime forsendelseMottattTidspunkt;
        private List<String> brukerIdentListe;
        private String kanalReferanseId;
        private String mottaksKanal;
        private String journalEnhet;

        public Builder medJournalpostId(String journalpostId) {
            this.journalpostId = journalpostId;
            return this;
        }

        public Builder medDokumentId(String dokumentId) {
            this.dokumentId = dokumentId;
            return this;
        }

        public Builder medVariantFormat(VariantFormat variantFormat) {
            this.variantFormat = variantFormat;
            return this;
        }

        public Builder medDokumentType(DokumentTypeId dokumentTypeId) {
            this.dokumentTypeId = dokumentTypeId;
            return this;
        }

        public Builder medDokumentKategori(DokumentKategori dokumentKategori) {
            this.dokumentKategori = dokumentKategori;
            return this;
        }

        public Builder medJournalførendeEnhet(String enhet) {
            this.journalEnhet = enhet;
            return this;
        }

        public Builder medArkivFilType(ArkivFilType arkivFilType) {
            this.arkivFilType = arkivFilType;
            return this;
        }

        public Builder medJournaltilstand(Journaltilstand journaltilstand) {
            this.journaltilstand = journaltilstand;
            return this;
        }

        public Builder medErHoveddokument(boolean erHoveddokument) {
            this.erHoveddokument = erHoveddokument;
            return this;
        }

        public Builder medForsendelseMottatt(LocalDate forsendelseMottatt) {
            this.forsendelseMottatt = forsendelseMottatt;
            return this;
        }

        public Builder medForsendelseMottattTidspunkt(LocalDateTime forsendelseMottattTidspunkt) {
            this.forsendelseMottattTidspunkt = forsendelseMottattTidspunkt;
            return this;
        }

        public Builder medBrukerIdentListe(List<String> brukerIdentListe) {
            this.brukerIdentListe = brukerIdentListe;
            return this;
        }

        public Builder medKanalReferanseId(String kanalReferanseId) {
            this.kanalReferanseId = kanalReferanseId;
            return this;
        }

        public Builder medMottaksKanal(String mottaksKanal) {
            this.mottaksKanal = mottaksKanal;
            return this;
        }

        public JournalMetadata build() {
            JournalMetadata jmd = new JournalMetadata();
            jmd.journalpostId = this.journalpostId;
            jmd.dokumentId = this.dokumentId;
            jmd.variantFormat = this.variantFormat;
            jmd.dokumentTypeId = this.dokumentTypeId;
            jmd.dokumentKategori = this.dokumentKategori;
            jmd.arkivFilType = this.arkivFilType;
            jmd.journaltilstand = this.journaltilstand;
            jmd.erHoveddokument = this.erHoveddokument;
            jmd.forsendelseMottatt = this.forsendelseMottatt;
            jmd.forsendelseMottattTidspunkt = this.forsendelseMottattTidspunkt;
            jmd.brukerIdentListe = this.brukerIdentListe;
            jmd.kanalReferanseId = this.kanalReferanseId;
            jmd.mottaksKanal = this.mottaksKanal;
            jmd.journalEnhet = this.journalEnhet;
            return jmd;
        }
    }
}
