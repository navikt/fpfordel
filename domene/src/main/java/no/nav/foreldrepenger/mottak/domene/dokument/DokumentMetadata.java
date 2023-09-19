package no.nav.foreldrepenger.mottak.domene.dokument;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import no.nav.foreldrepenger.fordel.BaseEntitet;
import no.nav.foreldrepenger.mottak.tjeneste.dokumentforsendelse.dto.ForsendelseStatus;

@Entity(name = "DokumentMetadata")
@Table(name = "DOKUMENT_METADATA")
public class DokumentMetadata extends BaseEntitet {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_DOKUMENT_METADATA")
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

    public static Builder builder() {
        return new Builder();
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

    public void setSaksnummer(String saksnummer) {
        this.saksnummer = saksnummer;
    }

    public Optional<String> getArkivId() {
        return Optional.ofNullable(arkivId);
    }

    public void setArkivId(String arkivId) {
        this.arkivId = arkivId;
    }

    public ForsendelseStatus getStatus() {
        return ForsendelseStatus.asEnumValue(status);
    }

    public void setStatus(ForsendelseStatus status) {
        this.status = status.name();
    }

    public LocalDateTime getForsendelseMottatt() {
        return forsendelseMottatt;
    }

    public static class Builder {
        private UUID forsendelseId;
        private String brukerId;
        private String saksnummer;
        private String arkivId;
        private ForsendelseStatus status = ForsendelseStatus.PENDING;
        private LocalDateTime forsendelseMottatt;

        public Builder setForsendelseId(UUID forsendelseId) {
            this.forsendelseId = Objects.requireNonNull(forsendelseId);
            return this;
        }

        public Builder setBrukerId(String brukerId) {
            this.brukerId = Objects.requireNonNull(brukerId);
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
            this.forsendelseMottatt = Objects.requireNonNull(forsendelseMottatt);
            return this;
        }

        public DokumentMetadata build() {
            DokumentMetadata dokumentMetadata = new DokumentMetadata();
            dokumentMetadata.brukerId = brukerId;
            dokumentMetadata.arkivId = arkivId;
            dokumentMetadata.saksnummer = saksnummer;
            dokumentMetadata.forsendelseId = forsendelseId;
            dokumentMetadata.forsendelseMottatt = forsendelseMottatt;
            dokumentMetadata.status = status.name();
            return dokumentMetadata;
        }
    }
}
