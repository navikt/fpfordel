package no.nav.foreldrepenger.journalføring.oppgave.lager;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

import org.hibernate.annotations.NaturalId;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import no.nav.foreldrepenger.fordel.BaseEntitet;

@Entity(name = "Oppgave")
@Table(name = "OPPGAVE")
public class OppgaveEntitet extends BaseEntitet implements Serializable {
    private static final long serialVersionUID = 1345122041950251207L;

    @Id
    @NaturalId
    @Column(name = "JOURNALPOST_ID")
    private String journalpostId;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false)
    private Status status;

    @Column(name = "ENHET", nullable = false, length = 10)
    private String enhet;

    @Column(name = "FRIST", nullable = false)
    private LocalDate frist;

    @Embedded
    private AktørId brukerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "YTELSE_TYPE")
    private YtelseType ytelseType;

    @Column(name = "BESKRIVELSE", length = 200)
    private String beskrivelse;

    @Column(name = "RESERVERT_AV")
    private String reservertAv;

    @Version
    @Column(name = "VERSJON", columnDefinition = "INTEGER default 0", nullable = false)
    private int versjon = 0;

    private OppgaveEntitet() {
    }

    public static OppgaveEntitet.Builder builder() {
        return new OppgaveEntitet.Builder();
    }

    public String getJournalpostId() {
        return journalpostId;
    }

    public Status getStatus() {
        return status;
    }

    public String getEnhet() {
        return enhet;
    }

    public LocalDate getFrist() {
        return frist;
    }

    public AktørId getBrukerId() {
        return brukerId;
    }

    public YtelseType getYtelseType() {
        return ytelseType;
    }

    public String getBeskrivelse() {
        return beskrivelse;
    }

    public String getReservertAv() {
        return reservertAv;
    }

    public void setJournalpostId(String journalpostId) {
        this.journalpostId = journalpostId;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setEnhet(String enhet) {
        this.enhet = enhet;
    }

    public void setFrist(LocalDate frist) {
        this.frist = frist;
    }

    public void setBrukerId(AktørId brukerId) {
        this.brukerId = brukerId;
    }

    public void setYtelseType(YtelseType ytelseType) {
        this.ytelseType = ytelseType;
    }

    public void setBeskrivelse(String beskrivelse) {
        this.beskrivelse = beskrivelse;
    }

    public void setReservertAv(String reservertAv) {
        this.reservertAv = reservertAv;
    }

    public static class Builder {
        private String journalpostId;
        private Status status;
        private String enhet;
        private LocalDate frist;
        private AktørId brukerId;
        private YtelseType ytelseType;
        private String beskrivelse;
        private String reservertAv;

        public Builder medJournalpostId(String journalpostId) {
            this.journalpostId = journalpostId;
            return this;
        }

        public Builder medStatus(Status status) {
            this.status = status;
            return this;
        }

        public Builder medEnhet(String enhet) {
            this.enhet = enhet;
            return this;
        }

        public Builder medFrist(LocalDate frist) {
            this.frist = frist;
            return this;
        }

        public Builder medBrukerId(AktørId brukerId) {
            this.brukerId = brukerId;
            return this;
        }

        public Builder medBrukerId(String brukerId) {
            this.brukerId = new AktørId(brukerId);
            return this;
        }

        public Builder medYtelseType(YtelseType ytelseType) {
            this.ytelseType = ytelseType;
            return this;
        }

        public Builder medBeskrivelse(String beskrivelse) {
            this.beskrivelse = beskrivelse;
            return this;
        }

        public Builder medReservertAv(String reservertAv) {
            this.reservertAv = reservertAv;
            return this;
        }

        public OppgaveEntitet build() {
            verifiser();
            OppgaveEntitet oppgave = new OppgaveEntitet();
            oppgave.setJournalpostId(journalpostId);
            oppgave.setYtelseType(ytelseType);
            oppgave.setStatus(status);
            oppgave.setBrukerId(brukerId);
            oppgave.setEnhet(enhet);
            oppgave.setBeskrivelse(beskrivelse);
            oppgave.setFrist(frist);
            oppgave.setReservertAv(reservertAv);
            return oppgave;
        }

        private void verifiser() {
            Objects.requireNonNull(journalpostId);
            Objects.requireNonNull(ytelseType);
            Objects.requireNonNull(status);
            Objects.requireNonNull(enhet);
            Objects.requireNonNull(frist);
        }
    }
}
