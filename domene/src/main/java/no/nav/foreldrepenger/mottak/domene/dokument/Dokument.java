package no.nav.foreldrepenger.mottak.domene.dokument;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import no.nav.foreldrepenger.fordel.BaseEntitet;
import no.nav.foreldrepenger.fordel.kodeverdi.ArkivFilType;
import no.nav.foreldrepenger.fordel.kodeverdi.DokumentTypeId;

@Entity(name = "Dokument")
@Table(name = "DOKUMENT")
public class Dokument extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_DOKUMENT")
    private Long id;

    @Column(name = "FORSENDELSE_ID")
    private UUID forsendelseId;

    @Convert(converter = DokumentTypeId.KodeverdiConverter.class)
    @Column(name = "dokument_type_id", nullable = false)
    private DokumentTypeId dokumentTypeId = DokumentTypeId.UDEFINERT;

    @Lob
    @Column(name = "BLOB", nullable = false)
    private byte[] blob;

    @Column(name = "HOVED_DOKUMENT")
    private Boolean hovedDokument;

    @Enumerated(EnumType.STRING)
    @Column(name = "arkiv_filtype")
    private ArkivFilType arkivFilType;

    @Column(name = "BESKRIVELSE")
    private String beskrivelse;

    private Dokument() {
    }

    public static Builder builder() {
        return new Builder();
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

    public void setDokumentTypeId(DokumentTypeId dokumentTypeId) {
        this.dokumentTypeId = dokumentTypeId;
    }

    public String getKlartekstDokument() {
        if (!ArkivFilType.erKlartekstType(this.arkivFilType)) {
            throw new IllegalStateException("Utviklerfeil: prøver å hente klartekst av binærdokument");
        }
        return new String(blob, StandardCharsets.UTF_8);
    }

    public String getBase64EncodetDokument() {
        return Base64.getEncoder().encodeToString(blob);
    }

    public byte[] getByteArrayDokument() {
        return blob;
    }

    public Boolean erHovedDokument() {
        return hovedDokument;
    }

    public ArkivFilType getArkivFilType() {
        return arkivFilType;
    }

    public String getBeskrivelse() {
        return beskrivelse;
    }

    public static class Builder {

        private DokumentTypeId dokumentTypeId;
        private byte[] blob;
        private Boolean hovedDokument;
        private UUID forsendelseId;
        private ArkivFilType arkivFilType;
        private String beskrivelse;

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

        public Dokument.Builder setBeskrivelse(String beskrivelse) {
            if ((beskrivelse != null) && (beskrivelse.length() > 150)) {
                this.beskrivelse = beskrivelse.substring(0, 149);
            } else {
                this.beskrivelse = beskrivelse;
            }
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
            dokument.beskrivelse = beskrivelse;
            return dokument;
        }

        private void verifyStateForBuild() {
            // TODO humle vurder denne når vi har full oversikt over hva som er påkrevd
            Objects.requireNonNull(blob);
            Objects.requireNonNull(dokumentTypeId);
            Objects.requireNonNull(hovedDokument);
            Objects.requireNonNull(forsendelseId);
            Objects.requireNonNull(arkivFilType);
        }
    }
}
