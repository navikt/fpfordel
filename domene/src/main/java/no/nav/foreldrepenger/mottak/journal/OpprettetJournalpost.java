package no.nav.foreldrepenger.mottak.journal;

public class OpprettetJournalpost {

    private String journalpostId;
    private boolean ferdigstilt;

    public OpprettetJournalpost(String journalpostId, boolean ferdigstilt) {
        this.journalpostId = journalpostId;
        this.ferdigstilt = ferdigstilt;
    }

    public String getJournalpostId() {
        return journalpostId;
    }

    public boolean isFerdigstilt() {
        return ferdigstilt;
    }

    @Override
    public String toString() {
        return "OpprettetJournalpost{" +
                "journalpostId='" + journalpostId + '\'' +
                ", ferdigstilt=" + ferdigstilt +
                '}';
    }
}
