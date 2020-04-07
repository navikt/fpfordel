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
    private String fnr;
    private String avsenderFnr;
    private String avsenderNavn;
    private String hovedDokumentId;
    private String hovedDokumentTittel;

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

    public String getFnr() {
        return fnr;
    }

    public void setFnr(String fnr) {
        this.fnr = fnr;
    }

    public String getAvsenderFnr() {
        return avsenderFnr;
    }

    public void setAvsenderFnr(String avsenderFnr) {
        this.avsenderFnr = avsenderFnr;
    }

    public String getAvsenderNavn() {
        return avsenderNavn;
    }

    public void setAvsenderNavn(String avsenderNavn) {
        this.avsenderNavn = avsenderNavn;
    }

    public String getHovedDokumentId() {
        return hovedDokumentId;
    }

    public void setHovedDokumentId(String hovedDokumentId) {
        this.hovedDokumentId = hovedDokumentId;
    }

    public String getHovedDokumentTittel() {
        return hovedDokumentTittel;
    }

    public void setHovedDokumentTittel(String hovedDokumentTittel) {
        this.hovedDokumentTittel = hovedDokumentTittel;
    }
}
