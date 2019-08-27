package no.nav.foreldrepenger.mottak.domene.dokument;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinFormula;

import no.nav.foreldrepenger.fordel.kodeverk.ArkivFilType;
import no.nav.foreldrepenger.fordel.kodeverk.DokumentTypeId;
import no.nav.vedtak.felles.jpa.BaseEntitet;

@Entity(name = "Dokument")
@Table(name = "DOKUMENT")
public class Dokument extends BaseEntitet {

    @Id
    @SequenceGenerator(name = "dokumentsekvens", sequenceName = "SEQ_DOKUMENT")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dokumentsekvens")
    private Long id;

    @Column(name = "FORSENDELSE_ID")
    private UUID forsendelseId;

    @ManyToOne(optional = false)
    @JoinColumnOrFormula(column = @JoinColumn(name = "dokument_type_id", referencedColumnName = "kode", nullable = false))
    @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + DokumentTypeId.DISCRIMINATOR + "'"))
    private DokumentTypeId dokumentTypeId = DokumentTypeId.UDEFINERT;

    @Lob
    @Column(name = "BLOB", nullable = false)
    private byte[] blob;

    @Column(name = "HOVED_DOKUMENT")
    private Boolean hovedDokument;

    @ManyToOne
    @JoinColumnOrFormula(column = @JoinColumn(name = "arkiv_filtype", referencedColumnName = "kode", nullable = false))
    @JoinColumnOrFormula(formula = @JoinFormula(referencedColumnName = "kodeverk", value = "'" + ArkivFilType.DISCRIMINATOR + "'"))
    private ArkivFilType arkivFilType;

    private Dokument() {
    }

    public Long getId() {
        return id;
    }

    public UUID getForsendelseId() {
        return forsendelseId;
    }

    public DokumentTypeId getDokumentTypeId() {
        return dokumentTypeId;
    }

    public String getKlartekstDokument() {
        if (!ArkivFilType.erKlartekstType(this.arkivFilType)) {
            throw new IllegalStateException("Utviklerfeil: prøver å hente klartekst av binærdokument");
        }
        return new String(blob, Charset.forName("UTF-8"));
    }

    public String getBase64EncodetDokument() {
        return Base64.getEncoder().encodeToString(blob);
    }

    public Boolean erHovedDokument() {
        return hovedDokument;
    }

    public ArkivFilType getArkivFilType() {
        return arkivFilType;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private DokumentTypeId dokumentTypeId;
        private byte[] blob;
        private Boolean hovedDokument;
        private UUID forsendelseId;
        private ArkivFilType arkivFilType;


        public Dokument.Builder setDokumentTypeId(DokumentTypeId dokumentTypeId) {
            this.dokumentTypeId = dokumentTypeId;
            return this;
        }

        public Dokument.Builder setDokumentInnhold(byte[] innhold, ArkivFilType arkivFilType) {
            this.blob = innhold != null ? Arrays.copyOf(innhold, innhold.length) : null;
            this.arkivFilType = arkivFilType;
            return this;
        }

        public Dokument.Builder setForsendelseId(UUID forsendelseId) {
            this.forsendelseId = forsendelseId;
            return this;
        }

        public Dokument.Builder setHovedDokument(boolean hovedDokument) {
            this.hovedDokument = hovedDokument;
            return this;
        }

        public Dokument build() {
            verifyStateForBuild();
            Dokument dokument = new Dokument();
            dokument.dokumentTypeId = dokumentTypeId;
            dokument.forsendelseId = forsendelseId;
            dokument.blob = blob;
            dokument.hovedDokument = hovedDokument;
            dokument.arkivFilType = arkivFilType;
            return dokument;
        }

        private void verifyStateForBuild() {
            //TODO humle vurder denne når vi har full oversikt over hva som er påkrevd
            Objects.requireNonNull(blob);
            Objects.requireNonNull(dokumentTypeId);
            Objects.requireNonNull(hovedDokument);
            Objects.requireNonNull(forsendelseId);
            Objects.requireNonNull(arkivFilType);
        }
    }
}
