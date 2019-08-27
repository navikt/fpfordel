package no.nav.foreldrepenger.mottak.journal;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import no.nav.foreldrepenger.fordel.kodeverk.ArkivFilType;
import no.nav.foreldrepenger.fordel.kodeverk.DokumentKategori;
import no.nav.foreldrepenger.fordel.kodeverk.DokumentTypeId;
import no.nav.foreldrepenger.fordel.kodeverk.MottakKanal;
import no.nav.foreldrepenger.fordel.kodeverk.VariantFormat;

public class JournalMetadata<T extends DokumentTypeId> {

    public enum Journaltilstand {
        MIDLERTIDIG,
        UTGAAR,
        ENDELIG
    }

    private String journalpostId;
    private String dokumentId;
    private VariantFormat variantFormat;
    private MottakKanal mottakKanal;
    private Journaltilstand journaltilstand;
    private T dokumentTypeId;
    private DokumentKategori dokumentKategori;
    private ArkivFilType arkivFilType;
    private boolean erHoveddokument;
    private LocalDate forsendelseMottatt;
    private LocalDateTime forsendelseMottattTidspunkt;
    private List<String> brukerIdentListe;
    private String kanalReferanseId;
    private String journalEnhet;

    private JournalMetadata() {
        // skjult
    }

    public static <T extends DokumentTypeId> Builder<T> builder() {
        return new Builder<>();
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

    public MottakKanal getMottakKanal() {
        return mottakKanal;
    }

    public T getDokumentTypeId() {
        return dokumentTypeId;
    }

    public DokumentKategori getDokumentKategori() {
        return dokumentKategori;
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

    public static class Builder<T extends DokumentTypeId> {
        private String journalpostId;
        private String dokumentId;
        private VariantFormat variantFormat;
        private MottakKanal mottakKanal;
        private T dokumentTypeId;
        private DokumentKategori dokumentKategori;
        private ArkivFilType arkivFilType;
        private Journaltilstand journaltilstand;
        private boolean erHoveddokument;
        private LocalDate forsendelseMottatt;
        private LocalDateTime forsendelseMottattTidspunkt;
        private List<String> brukerIdentListe;
        private String kanalReferanseId;
        private String journalEnhet;

        public Builder<T> medJournalpostId(String journalpostId) {
            this.journalpostId = journalpostId;
            return this;
        }

        public Builder<T> medDokumentId(String dokumentId) {
            this.dokumentId = dokumentId;
            return this;
        }

        public Builder<T> medVariantFormat(VariantFormat variantFormat) {
            this.variantFormat = variantFormat;
            return this;
        }

        public Builder<T> medMottakKanal(MottakKanal mottakKanal) {
            this.mottakKanal = mottakKanal;
            return this;
        }

        public Builder<T> medDokumentType(T dokumentTypeId) {
            this.dokumentTypeId = dokumentTypeId;
            return this;
        }

        public Builder<T> medDokumentKategori(DokumentKategori dokumentKategori) {
            this.dokumentKategori = dokumentKategori;
            return this;
        }

        public Builder<T> medJournalførendeEnhet(String enhet) {
            this.journalEnhet = enhet;
            return this;
        }


        public Builder<T> medArkivFilType(ArkivFilType arkivFilType) {
            this.arkivFilType = arkivFilType;
            return this;
        }

        public Builder<T> medJournaltilstand(Journaltilstand journaltilstand) {
            this.journaltilstand = journaltilstand;
            return this;
        }

        public Builder<T> medErHoveddokument(boolean erHoveddokument) {
            this.erHoveddokument = erHoveddokument;
            return this;
        }

        public Builder<T> medForsendelseMottatt(LocalDate forsendelseMottatt) {
            this.forsendelseMottatt = forsendelseMottatt;
            return this;
        }

        public Builder<T> medForsendelseMottattTidspunkt(LocalDateTime forsendelseMottattTidspunkt) {
            this.forsendelseMottattTidspunkt = forsendelseMottattTidspunkt;
            return this;
        }

        public Builder<T> medBrukerIdentListe(List<String> brukerIdentListe) {
            this.brukerIdentListe = brukerIdentListe;
            return this;
        }

        public Builder<T> medKanalReferanseId(String kanalReferanseId) {
            this.kanalReferanseId = kanalReferanseId;
            return this;
        }

        public JournalMetadata<T> build() {
            JournalMetadata<T> jmd = new JournalMetadata<>();
            jmd.journalpostId = this.journalpostId;
            jmd.dokumentId = this.dokumentId;
            jmd.variantFormat = this.variantFormat;
            jmd.mottakKanal = this.mottakKanal;
            jmd.dokumentTypeId = this.dokumentTypeId;
            jmd.dokumentKategori = this.dokumentKategori;
            jmd.arkivFilType = this.arkivFilType;
            jmd.journaltilstand = this.journaltilstand;
            jmd.erHoveddokument = this.erHoveddokument;
            jmd.forsendelseMottatt = this.forsendelseMottatt;
            jmd.forsendelseMottattTidspunkt = this.forsendelseMottattTidspunkt;
            jmd.brukerIdentListe = this.brukerIdentListe;
            jmd.kanalReferanseId = this.kanalReferanseId;
            jmd.journalEnhet = this.journalEnhet;
            return jmd;
        }
    }
}
