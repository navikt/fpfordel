package no.nav.foreldrepenger.mottak.domene.dokument;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseStatus;

@Entity(name = "DokumentMetadata")
@Table(name = "DOKUMENT_METADATA")
public class DokumentMetadata {
    public static final String UNIQUE_FORSENDELSE_ID_CONSTRAINT = "CHK_UNIQUE_FORS_DOKUMENT_MT";

    @Id
    @SequenceGenerator(name = "dokumentMetadataSekvens", sequenceName = "SEQ_DOKUMENT_METADATA")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dokumentMetadataSekvens")
    private Long id;

    @Column(name = "FORSENDELSE_ID")
    private UUID forsendelseId;

    @Column(name = "BRUKER_ID")
    private String brukerId;

    @Column(name = "SAKSNUMMER")
    private String saksnummer;

    @Column(name = "ARKIV_ID")
    private String arkivId;

    @Column(name = "FORSENDELSE_STATUS")
    private String status;
    
    @Column(name = "FORSENDELSE_MOTTATT")
    private LocalDateTime forsendelseMottatt;

    private DokumentMetadata() {
    }

    public Long getId() {
        return id;
    }

    public UUID getForsendelseId() {
        return forsendelseId;
    }

    public String getBrukerId() {
        return brukerId;
    }

    public Optional<String> getSaksnummer() {
        return Optional.ofNullable(saksnummer);
    }

    public Optional<String> getArkivId() {
        return Optional.ofNullable(arkivId);
    }

    public ForsendelseStatus getStatus() {
        return ForsendelseStatus.asEnumValue(status);
    }
    
    public LocalDateTime getForsendelseMottatt() {
        return forsendelseMottatt;
    }

    public void setSaksnummer(String saksnummer) {
        this.saksnummer = saksnummer;
    }

    public void setArkivId(String arkivId) {
        this.arkivId = arkivId;
    }

    public void setStatus(ForsendelseStatus status) {
        this.status = status.name();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID forsendelseId;
        private String brukerId;
        private String saksnummer;
        private String arkivId;
        private ForsendelseStatus status = ForsendelseStatus.PENDING;
        private LocalDateTime forsendelseMottatt;

        public Builder setForsendelseId(UUID forsendelseId) {
            this.forsendelseId = forsendelseId;
            return this;
        }

        public Builder setBrukerId(String brukerId) {
            this.brukerId = brukerId;
            return this;
        }

        public Builder setSaksnummer(String saksnummer) {
            this.saksnummer = saksnummer;
            return this;
        }

        public Builder setArkivId(String arkivId) {
            this.arkivId = arkivId;
            return this;
        }

        public Builder setStatus(ForsendelseStatus status) {
            this.status = status;
            return this;
        }
        
        public Builder setForsendelseMottatt(LocalDateTime forsendelseMottatt) {
            this.forsendelseMottatt = forsendelseMottatt;
            return this;
        }

        public DokumentMetadata build() {
            verifyStateForBuild();
            DokumentMetadata dokumentMetadata = new DokumentMetadata();
            dokumentMetadata.brukerId = brukerId;
            dokumentMetadata.arkivId = arkivId;
            dokumentMetadata.saksnummer = saksnummer;
            dokumentMetadata.forsendelseId = forsendelseId;
            dokumentMetadata.forsendelseMottatt = forsendelseMottatt;
            dokumentMetadata.status = status.name();
            return dokumentMetadata;
        }

        private void verifyStateForBuild() {
            Objects.requireNonNull(brukerId);
            Objects.requireNonNull(forsendelseId);
            Objects.requireNonNull(forsendelseMottatt);
        }
    }
}
