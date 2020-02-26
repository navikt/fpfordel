package no.nav.foreldrepenger.mottak.felles.kafka;

import java.util.Optional;
import java.util.UUID;

public class SøknadFordeltOgJournalførtHendelse {
    private final String journalpostId;
    private final UUID forsendelseId;
    private final String fnr;
    private final String saksnr;

    public SøknadFordeltOgJournalførtHendelse(String journalpostId, Optional<UUID> forsendelseId, Optional<String> fnr,
            Optional<String> saksnummer) {
        this.journalpostId = journalpostId;
        this.forsendelseId = forsendelseId.isPresent() ? forsendelseId.get() : null;
        this.saksnr = saksnummer.isPresent() ? saksnummer.get() : null;
        this.fnr = fnr.isPresent() ? fnr.get() : null;
    }

    public String getJournalpostId() {
        return journalpostId;
    }

    public UUID getForsendelseId() {
        return forsendelseId;
    }

    public String getFnr() {
        return fnr;
    }

    public String getSaksnr() {
        return saksnr;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[journalpostId=" + journalpostId + ", forsendelseId=" + forsendelseId
                + ", fnr=" + fnr + ", saksnr=" + saksnr + "]";
    }

}
