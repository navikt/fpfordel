package no.nav.foreldrepenger.mottak.domene.dokument;

import java.time.LocalDateTime;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;

@Entity(name = "Journalpost")
@Table(name = "JOURNALPOST")
public class Journalpost {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_JOURNALPOST")
    private Long id;

    @Column(name = "JOURNALPOST_ID", nullable = false)
    private String journalpostId;

    @Column(name = "TILSTAND")
    private String tilstand;

    @Column(name = "KANAL")
    private String kanal;

    @Column(name = "REFERANSE")
    private String referanse;

    @Column(name = "opprettet_av", nullable = false)
    private String opprettetAv;

    @Column(name = "opprettet_tid", nullable = false)
    private LocalDateTime opprettetTidspunkt;

    @PrePersist
    protected void onCreate() {
        this.opprettetTidspunkt = LocalDateTime.now();
    }

    public Journalpost(String journalpostId, String tilstand, String kanal, String referanse, String opprettetAv) {
        Objects.requireNonNull(journalpostId);
        Objects.requireNonNull(opprettetAv);
        this.journalpostId = journalpostId;
        this.tilstand = tilstand;
        this.kanal = kanal;
        this.referanse = referanse;
        this.opprettetAv = opprettetAv;
    }

    public Journalpost() {
        // Hibernate
    }

    public Long getId() {
        return id;
    }

    public String getJournalpostId() {
        return journalpostId;
    }

    public String getTilstand() {
        return tilstand;
    }

    public String getKanal() {
        return kanal;
    }

    public String getReferanse() {
        return referanse;
    }

    public String getOpprettetAv() {
        return opprettetAv;
    }

    public LocalDateTime getOpprettetTidspunkt() {
        return opprettetTidspunkt;
    }
}
