package no.nav.foreldrepenger.mottak.journal;

import java.time.LocalDateTime;
import java.util.Optional;

public class JournalPost {

    private final String journalpostId;

    private String innhold;
    private String arkivSakId;
    private String arkivSakSystem;
    private LocalDateTime forsendelseInnsendt;
    private String tema;
    private String aktørId;
    private String avsenderAktørId;

    public JournalPost(String journalpostId) {
        this.journalpostId = journalpostId;
    }

    public String getJournalpostId() {
        return journalpostId;
    }

    public String getInnhold() {
        return innhold;
    }

    public void setInnhold(String innhold) {
        this.innhold = innhold;
    }

    public String getArkivSakId() {
        return arkivSakId;
    }

    public void setArkivSakId(String arkivSakId) {
        this.arkivSakId = arkivSakId;
    }

    public Optional<String> getArkivSakSystem() {
        return Optional.ofNullable(arkivSakSystem);
    }

    public void setArkivSakSystem(String arkivSakSystem) {
        this.arkivSakSystem = arkivSakSystem;
    }

    public LocalDateTime getForsendelseInnsendt() {
        return forsendelseInnsendt;
    }

    public void setForsendelseInnsendt(LocalDateTime forsendelseInnsendt) {
        this.forsendelseInnsendt = forsendelseInnsendt;
    }

    public String getTema() {
        return tema;
    }

    public void setTema(String tema) {
        this.tema = tema;
    }

    public String getAktørId() {
        return aktørId;
    }

    public void setAktørId(String aktørId) {
        this.aktørId = aktørId;
    }

    public String getAvsenderAktørId() {
        return avsenderAktørId;
    }

    public void setAvsenderAktørId(String avsenderAktørId) {
        this.avsenderAktørId = avsenderAktørId;
    }
}
